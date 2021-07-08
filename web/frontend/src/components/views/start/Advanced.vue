<template>
  <v-container>
    <v-card class="mx-auto">
      <v-list>
        <v-list-item>
          <v-list-item-title class="title">
            Customized Exploration
          </v-list-item-title>
        </v-list-item>
        <v-list-item>
          <div class="v-card__subtitle">
            Create a specified starting network by selecting the nodes and edge types it should contain.
            Apply filters or set options in the "TOOLS" panel by clicking onto the meta-entities in the network.
          </div>
        </v-list-item>
      </v-list>
      <v-divider></v-divider>
      <v-container>
        <v-row>
          <v-col cols="2">
            <v-list v-model="nodeModel" ref="nodeSelector">
              <v-card-title>Nodes</v-card-title>
              <v-list-item v-for="item in nodes.filter(n=>!n.external)" :key="item.index">
                <v-chip outlined v-on:click="toggleNode(item.index)"
                        :color="nodeModel.indexOf(item.index)===-1?'gray':'primary'"
                        :text-color="nodeModel.indexOf(item.index)===-1?'black':'gray'"
                >
                  <v-icon left :color="getColoring('nodes',item.label)">fas fa-genderless</v-icon>
                  {{ item.label }}
                  <span style="color: gray; margin-left: 3pt"
                        v-show="nodeModel.indexOf(item.index)>-1">({{
                      $global.metagraph.weights.nodes[item.label.toLowerCase()]
                    }})</span>
                </v-chip>
              </v-list-item>
            </v-list>

          </v-col>
          <v-col cols="4">
            <v-list v-model="edgeModel">
              <v-card-title>Edges</v-card-title>
              <template v-for="item in edges.filter(e=>!e.external)">
                <v-list-item :key="item.index">
                  <v-chip outlined v-on:click="toggleEdge(item.index)"
                          :color="edgeModel.indexOf(item.index)===-1?'gray':'primary'"
                          :text-color="edgeModel.indexOf(item.index)===-1?'black':'gray'"
                  >
                      <v-icon left :color="getColoring('edges',item.label)[0]">fas fa-genderless</v-icon>
                    <template v-if="direction(item.label)===0">
                      <v-icon left>fas fa-undo-alt</v-icon>
                    </template>
                    <template v-else>
                      <v-icon v-if="direction(item.label)===1" left>fas fa-long-arrow-alt-right</v-icon>
                      <v-icon v-else left>fas fa-arrows-alt-h</v-icon>
                      <v-icon left :color="getColoring('edges',item.label)[1]">fas fa-genderless</v-icon>
                    </template>
                    {{ item.label }}
                    <span style="color: gray; margin-left: 3pt"
                          v-show="edgeModel.indexOf(item.index)>-1">({{ $global.metagraph.weights.edges[item.label] }})</span>
                  </v-chip>
                </v-list-item>
                <v-list-item
                  v-show="edgeModel.indexOf(item.index)>-1 && (item.label==='ProteinInteractsWithProtein' ||item.label==='GeneInteractsWithGene' )">
                  <v-chip outlined v-on:click="interactions[item.label]=false"
                          :color="interactions[item.label]?'gray':'primary'"
                  >
                    experimental
                  </v-chip>
                  <v-chip outlined v-on:click="interactions[item.label]=true"
                          :color="!interactions[item.label]?'gray':'primary'"
                  >
                    all
                  </v-chip>

                </v-list-item>
              </template>
            </v-list>
          </v-col>
          <v-col cols="6">
            <Network ref="startgraph" @selectionEvent="graphSelection"
                   :startGraph="true" tools :configuration="{visualized:true}" :window-style="windowStyle">
              <template v-slot:tools>
                <v-card elevation="3">
                  <v-container v-if="tools.general">
                    <v-card-title>General</v-card-title>
                    <v-list>
                      <v-list-item v-if="filters[selectedEdge.label]">
                        <v-switch v-model="filters[selectedEdge.label].internalOnly"
                                  label="Connect filtered nodes only"></v-switch>
                      </v-list-item>
                    </v-list>
                  </v-container>
                  <v-container v-show="tools.filter">
                    <v-card-title>Filters</v-card-title>
                    <v-simple-table fixed-header ref="filterTable" v-if="filterEntity.length>0">
                      <template v-slot:default>
                        <thead>
                        <tr>
                          <th class="text-center">Type</th>
                          <th class="text-center">Filter</th>
                          <th class="text-center">Operation</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr v-for="(item,index) in filters[filterEntity]" :key="item.type+item.expression">
                          <td>{{ item.type }}</td>
                          <td>{{ item.expression }}</td>
                          <td>
                            <v-chip outlined v-on:click="removeFilter(index)">
                              <v-icon dense>fas fa-trash</v-icon>
                            </v-chip>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <v-select
                              v-model="filterTypeModel"
                              :items="filterTypes"
                              label="type"
                            ></v-select>
                          </td>
                          <td>
                            <v-text-field
                              v-model="filterModel"
                              :label="filterLabel"
                              placeholder="Pattern"
                            ></v-text-field>
                          </td>
                          <td>
                            <v-chip outlined v-on:click="saveFilter"
                                    :disabled="filterModel ===undefined|| filterModel.length ===0 ||filterTypeModel ===undefined">
                              <v-icon dense>fas fa-plus</v-icon>
                            </v-chip>
                          </td>
                        </tr>
                        </tbody>
                      </template>
                    </v-simple-table>
                  </v-container>
                </v-card>


              </template>
            </Network>
          </v-col>
        </v-row>
      </v-container>
    </v-card>
  </v-container>
</template>

<script>
import Network from "@/components/views/graph/Network";

export default {
  name: "Advanced",
  props: {
    options: Object,
    filters: Object,
    colors: {
      type: Object
    },
  },

  data() {
    return {
      interactions: {ProteinInteractsWithProtein: false, GeneInteractsWithGene: false},
      nodes: [],
      edges: [],
      nodeModel: [],
      edgeModel: [],
      filterId: -1,
      tools: {general: false, filter: false},
      windowStyle: {
        height: '75vh',
        'min-height': '75vh',
      },

      filterAdd: false,
      filterAdding: false,
      filterSelectDisabled: false,
      filterTypes: ['startsWith', 'contain', 'match'],
      filterEntity: "",
      neighborNodes: [],
      filterLabel: "",
      filterType: "",
      filterTypeModel: [],
      filterModel: "",
      filterName: "",
      selectedEdge: Object,
    }
  },
  created() {
    this.nodeModel = []
    this.edgeModel = []
    this.nodes = []
    this.edges = []
  },
  mounted() {
    this.$refs.startgraph.showAllNodes(false)
    this.$refs.startgraph.hideAllEdges(true)
    this.$refs.startgraph.setPhysics(true)
    this.initLists(this.$global.metagraph)
  },
  methods: {
    reset: function () {
      this.options.selectedElements.forEach(e => {
        if (e.type === "node") {
          this.$refs.startgraph.hideGroupVisibility(this.nodes[e.index].label.toLowerCase(), true)
        } else
          this.$refs.startgraph.toggleEdgeVisible(this.edges[e.index].label)
      })
      this.nodeModel = []
      this.edgeModel = []
      this.options.selectedElements.length = 0

      Object.keys(this.filters).forEach(key=>this.$delete(this.filters,key))
      this.graphSelection()
    },
    initLists: function (selectionGraph) {
      selectionGraph.nodes.forEach(n => {
        this.nodes.push({index: this.nodes.length, id: parseInt(n.id), label: n.label})
      })
      selectionGraph.edges.forEach(e => {
        let depends = [parseInt(e.from)]
        if (e.from !== e.to)
          depends.push(parseInt(e.to))
        this.edges.push({index: this.edges.length, id: e.id, label: e.label, depends: depends})
      })
    },

    setOptions: function (options) {
      this.options = options;
    },
    loadGraph: function (bool) {
      let graphLoad = {}
      if (!bool) {
        this.reset()
        return
      }
      graphLoad = {post: {nodes: {}, edges: {}}}
      this.options.selectedElements.forEach(element => {
        let filter = this.filters[element.name]
        if (filter === undefined)
          filter = []
        if (element.type === "node") {
          graphLoad.post.nodes[element.name.toLowerCase()] = {filters: filter}
        } else {
          graphLoad.post.edges[element.name] = {filters: filter}
        }
      })
      graphLoad.post.connectedOnly = this.options.onlyConnected
      graphLoad.post.interactions = {...this.interactions}
      if (Object.keys(graphLoad.post.nodes).length === 0) {
        this.$emit("printNotificationEvent", "Please select some nodes/edges first!", 1)
        return
      }
      graphLoad.post["uid"] = this.$cookies.get("uid")
      graphLoad["skipVis"] = this.options.skipVis;
      this.$emit("graphLoadEvent", graphLoad)
    },
    graphSelection: function (params) {
      this.$set(this.tools, "general", false)
      this.$set(this.tools, "filter", false)
      if (!params) {
        return
      }
      if (params.nodes.length > 0) {
        this.$set(this.tools, "filter", true)
        let node = this.$global.metagraph.nodes.filter(n => n.id === params.nodes[0])[0]
        this.setFilter(node.label)
      } else {
        let edge = this.$global.metagraph.edges.filter(m => params.edges[0] === m.id)[0]
        this.selectedEdge = edge
        if (edge.from === edge.to) {
          if (!this.filters[edge.label])
            this.filters[edge.label] = {name:edge.label,internalOnly: true}
          this.$set(this.tools, "general", true)
        }
      }

    },
    // evalEdgeOptionUpdate: function (updateName) {
    //   if (updateName === "internalOnly") {
    //     let bool = this.filters[this.selectedEdge.label][updateName]
    //     if(bool && this.selectedEdge.externalNode){
    //       this.$refs.startgraph.removeNode(externalNode)
    //     } else{
    //       this.createExternalNode(this.selectedEdge.from)
    //     }
    //   }
    // },
    // createExternalNode:function(nodeId){
    //   let node = this.$refs.startgraph.getNodeById(nodeId)
    // },
    toggleNode: function (nodeIndex) {
      let index = this.nodeModel.indexOf(nodeIndex)
      this.$refs.startgraph.hideGroupVisibility(this.nodes[nodeIndex].label.toLowerCase(), index > -1, true)
      if (index >= 0) {
        let remove = -1;
        for (let idx in this.options.selectedElements) {
          if (this.options.selectedElements[idx].type === "node" && this.options.selectedElements[idx].index === nodeIndex) {
            remove = idx;
            break;
          }
        }
        this.options.selectedElements.splice(remove, 1)
        this.nodeModel.splice(index, 1)
        Object.values(this.edges).filter(item => (item.depends.indexOf(this.nodes[nodeIndex].id) > -1 && this.edgeModel.indexOf(item.index) > -1)).map(item => item.index).forEach(this.toggleEdge)

      } else {
        this.nodeModel.push(nodeIndex)
        this.options.selectedElements.push({
          index: nodeIndex,
          type: "node",
          name: this.nodes[nodeIndex].label,
          filter: []
        })
      }
      this.$nextTick(() => {
        this.$refs.startgraph.focusNode()
        this.$refs.nodeSelector.$forceUpdate()
      })
    },

    graphViewEvent: function (data) {
      if (data.event === "toggle") {
        let params = data.params;
        if (params.type === "nodes")
          this.hideGroupVisibility(params.name, params.state, true)
        else if (params.type === "edges")
          this.toggleEdgeVisible(params.name)
      }
      if (data.event === "isolate") {
        this.showOnlyComponent(data.selected, data.state)
      }
    },
    saveFilter: function () {
      let data = {type: this.filterTypeModel, expression: this.filterModel};

      if (this.filters[this.filterEntity] === undefined)
        this.filters[this.filterEntity] = []

      if (this.filters[this.filterEntity].filter(f => (f.type === this.filterTypeModel && f.expression === this.filterModel)).length === 0) {
        this.filters[this.filterEntity].push(data)
      }
      this.filterTypeModel = ""
      this.filterModel = ""
    }
    ,
    removeFilter: function (idx) {
      this.filters[this.filterEntity].splice(idx, 1)
      this.$refs.filterTable.$forceUpdate()
    },
    setFilter: function (name) {
      this.filterName = name
      this.filterEntity = name
    },
    toggleEdge: function (edgeIndex) {
      let index = this.edgeModel.indexOf(edgeIndex)
      this.$refs.startgraph.toggleEdgeVisible(this.edges[edgeIndex].label)
      if (index >= 0) {
        let remove = -1;
        let restDepending = []
        for (let idx in this.options.selectedElements) {
          if (this.options.selectedElements[idx].type === "edge") {
            if (this.options.selectedElements[idx].index === edgeIndex)
              remove = idx;
            else
              this.edges[this.options.selectedElements[idx].index].depends.forEach(d => restDepending.push(d))
          }
        }
        this.options.selectedElements.splice(remove, 1)
        this.edgeModel.splice(index, 1)
      } else {
        this.edgeModel.push(edgeIndex)
        let depending = []
        for (let idx in this.edges[edgeIndex].depends) {
          depending.push(this.edges[edgeIndex].depends[idx])
        }
        Object.values(this.nodes).filter(item => depending.indexOf(item.id) >= 0 && this.nodeModel.indexOf(item.index) === -1).forEach(item => this.toggleNode(item.index))
        this.options.selectedElements.push({
          index: edgeIndex,
          type: "edge",
          name: this.edges[edgeIndex].label,
          filter: []
        })
        if (!this.filters[this.edges[edgeIndex].label])
          this.filters[this.edges[edgeIndex].label] = {name:this.edges[edgeIndex].label,internalOnly: true}
      }
      this.$nextTick(() => {
        this.$refs.startgraph.focusNode()
      })
    },
    direction: function (edge) {
      if (this.$utils.isEdgeDirected(this.$global.metagraph, edge))
        return 1
      return 0
    },
    getColoring: function (entity, name) {
      let colors = this.$utils.getColoring(this.$global.metagraph, entity, name, "light")
      return colors;
    },
  },
  components: {
    Network,
  },
}
</script>

<style scoped>

</style>
