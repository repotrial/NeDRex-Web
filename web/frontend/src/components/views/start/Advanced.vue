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
            Create a specified starting network by selecting the nodes and edge types it should contain <i>(left
            side)</i>.
            Apply filters by clicking on the cog icon <i>(left side)</i> or on the nodes or the edge labels to apply the
            filter on in the <i>(right panel)</i>.
          </div>
        </v-list-item>
      </v-list>
      <v-divider></v-divider>
      <v-container>
        <v-row>
          <v-col cols="3">
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
                      (countMap[item.label.toLowerCase()] ? countMap[item.label.toLowerCase()] + '/' : '?/') +
                      $global.metagraph.weights.nodes[item.label.toLowerCase()]
                    }})</span>
                </v-chip>
                <v-btn icon @click="nodeSelection(item.index)" small>
                  <v-icon v-show="nodeModel.indexOf(item.index)>-1" small>fas fa-cog</v-icon>
                </v-btn>
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
                          v-show="edgeModel.indexOf(item.index)>-1">({{
                        $global.metagraph.weights.edges[item.label]
                      }})</span>
                  </v-chip>
                  <v-btn icon @click="edgeSelection(item.index)" small>
                    <v-icon v-show="edgeModel.indexOf(item.index)>-1" small>fas fa-cog</v-icon>
                  </v-btn>
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
                  <v-btn icon @click="edgeSelection(item.index)" small>
                    <v-icon v-show="edgeModel.indexOf(item.index)>-1" small>fas fa-cog</v-icon>
                  </v-btn>
                </v-list-item>
              </template>
            </v-list>
          </v-col>
          <v-col cols="5">
            <Network ref="startgraph" @selectionEvent="graphSelection"
                     :startGraph="true" :configuration="{visualized:true}" :window-style="windowStyle">
            </Network>
          </v-col>
        </v-row>
        <v-row>
          <v-col :cols="7">
            <v-card-title>General Configuration</v-card-title>
            <v-list-item-subtitle>
              <LabeledSwitch v-model="options.onlyConnected" label-off="Include all nodes"
                             label-on="Remove unconnected">
                <template v-slot:tooltip>
                  <div>
                    Removes nodes of network degree zero, so nodes that are not connected by any edge in the
                    network.<br>
                    This counts specifically for nodes that were filtered.
                  </div>
                </template>
              </LabeledSwitch>
            </v-list-item-subtitle>
          </v-col>
          <v-col></v-col>
        </v-row>
        <v-row>
          <div style="width:100%; display: flex; justify-content: center; margin-bottom: 5px">
            <ButtonCancel label="Reset" @click="loadGraph(false)"></ButtonCancel>
            <ButtonNext :disabled="nodeModel.length===0" label="Generate Network" @click="loadGraph(true)"></ButtonNext>
          </div>
        </v-row>
      </v-container>
      <FilterDialog ref="filter" :filterType="filterTypeMap" :node-id="filterNodeId" @updateNodeCount="setNodeCount"
                    @filterTypeChangeEvent="setFilterType"></FilterDialog>
      <OptionsDialog ref="options" :edge-id="optionEdgeId" :edges="edges"></OptionsDialog>
      <v-dialog width="500px" v-model="missingFilterDialog" style="z-index: 1001">
        <v-card style="position: relative">
          <v-btn icon style="position: absolute; right: 0" @click="missingFilterDialog=false" color="error"><v-icon>far fa-times-circle</v-icon></v-btn>
          <v-card-title>Unfiltered Request</v-card-title>
          <template v-if="edgeModel.length>1">
            <v-card-text>
              <div>You selected edges or nodes without applying any filter to the network. Please either select IDs or
                add
                a text filter by clicking on the option button beside the label or on the node in the network
                representation to at least one of the following nodes to proceed:
              </div>
              <div v-for="node in missingFilterNodes"><b>{{ node }}</b></div>
            </v-card-text>
          </template>
          <template v-else>
            <v-card-text>
              <div>
                You selected one edge without applying any filter to a node. You can proceed but be aware that depending
                on the edge you selected, your browser might have to handle a lot of data and that visualizing it might
                also be prevented. If you choose to load this network regardlessly you will still be able to either
                download the network as .graphml file or to filter or adjust your selection in the list view.
              </div>
            </v-card-text>
            <v-card-actions>
              <v-btn
              text
              @click="missingFilterDialog=false"
              >
                Cancel
              </v-btn>
              <v-btn
              text
              @click="loadGraph(true,true)"
              >
                Proceed
              </v-btn>
            </v-card-actions>
          </template>
        </v-card>
      </v-dialog>
    </v-card>
  </v-container>
</template>

<script>
import Network from "@/components/views/graph/Network";
import FilterDialog from "@/components/views/start/advanced/FilterDialog";
import OptionsDialog from "@/components/views/start/advanced/OptionsDialog";
import LabeledSwitch from "@/components/app/input/LabeledSwitch";
import ButtonAdvanced from "@/components/start/quick/ButtonAdvanced";
import ButtonNext from "@/components/start/quick/ButtonNext";
import ButtonCancel from "@/components/start/quick/ButtonCancel";


export default {
  name: "Advanced",
  props: {
    options: Object,
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
      countMap: {},
      filterTypeMap: {},
      windowStyle: {
        height: '50vh',
        'min-height': '100%',
      },

      neighborNodes: [],
      selectedEdge: Object,
      filterNodeId: undefined,
      optionEdgeId: undefined,
      missingFilterNodes: undefined,
      missingFilterDialog: false,
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
      this.filterNodeId = undefined;
      this.countMap = {}
      this.filterTypeMap = {}
      this.options.selectedElements.forEach(e => {
        if (e.type === "edge")
          this.toggleEdge(e.index)
      })
      this.options.selectedElements.forEach(n => {
        if (n.type === "node")
          this.$refs.startgraph.hideGroupVisibility(this.nodes[n.index].label.toLowerCase(), true)
      })
      this.nodeModel = []
      this.edgeModel = []
      this.options.selectedElements.length = 0
      this.$refs.filter.clear(true)

      this.graphSelection()
    },
    initLists: function (selectionGraph) {
      this.countMap = {}
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
    getConnectedMetacomponents: function (edges, nodes) {
      let allNodes = nodes.map(n => this.$global.metagraph.nodes.filter(no => no.label === n.name)[0].id)
      let allEdges = edges.map(e => e.name)
      let edgeObjects = edges.map(e => this.$global.metagraph.edges.filter(ed => ed.label === e.name)[0])
      let out = []
      edgeObjects.forEach(e => {
        if (allNodes.indexOf(e.from) === -1)
          allNodes.push(e.from)
        if (allNodes.indexOf(e.to) === -1)
          allNodes.push(e.to)
      })

      while (allNodes.length > 0) {
        let currentNodes = [allNodes[0]]
        let currentEdges = []

        let change = true
        while (change) {
          change = false
          edgeObjects.forEach(edge => {
            if (currentNodes.indexOf(edge.from) > -1) {
              currentEdges.push(edge.label)
              if (currentNodes.indexOf(edge.to) === -1) {
                currentNodes.push(edge.to)
                change = true
              }
            } else if (currentNodes.indexOf(edge.to) > -1) {
              currentEdges.push(edge.label)
              if (currentNodes.indexOf(edge.from) === -1) {
                currentNodes.push(edge.from)
                change = true
              }
            }
          })
        }
        out.push(currentNodes.map(n => this.$global.metagraph.nodes.filter(no => no.id === n)[0].label))
        currentNodes.forEach(n => allNodes.splice(allNodes.indexOf(n), 1))
        currentEdges.forEach(n => allEdges.splice(allEdges.indexOf(n), 1))
      }
      return out
    },

    graphAllowed: function () {
      let ccs = this.getConnectedMetacomponents(this.options.selectedElements.filter(e => e.type === 'edge'), this.options.selectedElements.filter(e => e.type === 'node'))
      this.options.selectedElements.forEach(element => {
        if (element.type === 'node') {
          let filter = this.$refs.filter.getFilter(element.name.toLowerCase())
          if (!filter || (!filter.ids && filter.length === 0))
            return
          for (let i = 0; i < ccs.length; i++) {
            if (ccs[i].indexOf(element.name) > -1)
              ccs.splice(i, 1)
          }
        }
      });
      if (ccs.length > 0) {
        this.missingFilterNodes = ccs[0]
        this.missingFilterDialog = true
      }
      return ccs.length === 0
    },

    loadGraph: function (bool,force) {
      let graphLoad = {}
      if (!bool) {
        this.reset()
        return
      }
      if (!force && !this.graphAllowed())
        return;
      graphLoad = {post: {nodes: {}, edges: {}}}
      this.options.selectedElements.forEach(element => {
        if (element.type === 'node') {
          let filter = this.$refs.filter.getFilter(element.name.toLowerCase())
          if (filter == null)
            filter = {filters: []}
          if (filter.ids)
            graphLoad.post.nodes[element.name.toLowerCase()] = {ids: filter.ids}
          else
            graphLoad.post.nodes[element.name.toLowerCase()] = {filters: filter.filters}
        } else {
          graphLoad.post.edges[element.name] = {filters: []}
        }
      })
      graphLoad.post.connectedOnly = this.options.onlyConnected
      graphLoad.post.interactions = {...this.interactions}
      graphLoad.post.options = {nodes: {...this.$refs.filter.getOptions()}, edges: {...this.$refs.options.getOptions()}}
      if (Object.keys(graphLoad.post.nodes).length === 0) {
        this.$emit("printNotificationEvent", "Please select some nodes/edges first!", 1)
        return
      }
      graphLoad.post["uid"] = this.$cookies.get("uid")
      graphLoad["skipVis"] = true
      this.reset()
      this.$emit("graphLoadEvent", graphLoad)
    },
    graphSelection: function (params) {
      if (!params)
        return
      if (params.nodes.length > 0)
        for (let i = 0; i < this.$global.metagraph.nodes.length; i++) {
          if (this.$global.metagraph.nodes[i].id === params.nodes[0])
            this.nodeSelection(i)
        }
      else if (params.edges.length > 0) {
        this.edges.forEach(edge => {
          if (edge.id === params.edges[0].id)
            this.edgeSelection(edge.index)
        })
      }
    },

    nodeSelection: function (idx) {
      if (idx == null)
        return
      this.$set(this, 'filterNodeId', idx)
      this.$refs.filter.show()
    },

    edgeSelection: function (idx) {
      this.optionEdgeId = idx;
      this.$refs.options.show()
    },

    setFilterType: function (data) {
      this.$set(this.filterTypeMap, data.node, data.state)
    },


    setNodeCount: function (data) {
      this.$set(this.countMap, data.node, data.count)
    },
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
        if (this.$refs.startgraph)
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
          this.setEdgeVisible(params.name, true)
      }
      if (data.event === "isolate") {
        this.showOnlyComponent(data.selected, data.state)
      }
    }
    ,
    toggleEdge: function (edgeIndex) {
      let index = this.edgeModel.indexOf(edgeIndex)
      this.edges[edgeIndex].hidden = !(this.edges[edgeIndex].hidden == null || this.edges[edgeIndex].hidden)
      this.$refs.startgraph.setEdgeVisible(this.edges[edgeIndex].label, !this.edges[edgeIndex].hidden, true)
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
      }
      this.$nextTick(() => {
        if (this.$refs.startgraph)
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
  }
  ,
  components: {
    ButtonCancel,
    ButtonNext,
    ButtonAdvanced,
    LabeledSwitch,
    OptionsDialog,
    FilterDialog,
    Network,
  }
  ,
}
</script>

<style scoped>

</style>
