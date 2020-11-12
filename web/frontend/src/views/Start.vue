<template>
  <div style="margin-top:20px">
    <v-card>
      <v-list>
        <v-list-item>
          <v-list-item-title class="title">Select your exploration path</v-list-item-title>
        </v-list-item>
        <v-list-item>
          <v-list-item-subtitle>For new users or basic tasks like drug-target identification, the guided path is recommended.
          </v-list-item-subtitle>
        </v-list-item>
        <v-divider></v-divider>
        <v-list-item>
          <v-tabs v-model="selectionTab" centered>
            <v-tabs-slider color="blue"></v-tabs-slider>
            <v-tab>Guided Exploration</v-tab>
            <v-tab>Advanced Exploration</v-tab>
          </v-tabs>
        </v-list-item>
      </v-list>
    </v-card>

    <v-container v-show="selectionTab===1">
      <v-card class="mx-auto">
        <v-list>
          <v-list-item>
              <v-list-item-title class="title">
                Customized Exploration
              </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-subtitle>
              Query a specified starting graph.
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
          <v-divider></v-divider>

        <v-container v-if="metagraph !== undefined">
          <v-row>
            <v-col>
              <Graph ref="startgraph" :initgraph="{graph:metagraph,name:'metagraph'}" :startGraph="true"></Graph>
            </v-col>
            <v-col>
              <v-list v-model="nodeModel" ref="nodeSelector">
                <v-card-title>Nodes</v-card-title>
                <v-list-item v-for="item in nodes" :key="item.index">
                  <v-chip outlined v-on:click="toggleNode(item.index)"
                          :color="nodeModel.indexOf(item.index)===-1?'gray':'primary'"
                          :text-color="nodeModel.indexOf(item.index)===-1?'black':'gray'">
                    <v-icon left :color="getColoring('nodes',item.label)">fas fa-genderless</v-icon>
                    {{ item.label }}
                  </v-chip>
                </v-list-item>
              </v-list>

            </v-col>
            <v-col>
              <v-list v-model="edgeModel">
                <v-card-title>Edges</v-card-title>
                <v-list-item v-for="item in edges" :key="item.index">
                  <v-chip outlined v-on:click="toggleEdge(item.index)"
                          :color="edgeModel.indexOf(item.index)===-1?'gray':'primary'"
                          :text-color="edgeModel.indexOf(item.index)===-1?'black':'gray'">

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
                  </v-chip>
                </v-list-item>
              </v-list>
            </v-col>
          </v-row>
        </v-container>
      </v-card>
      <v-card>
        <v-checkbox v-model="onlyConnected" label="Hide unconnected"></v-checkbox>
        <v-checkbox v-model="skipVis" label="Skip visualization"></v-checkbox>
      </v-card>
      <v-btn v-on:click="loadGraph(-1)" style="margin:5px">
        Apply Selection
      </v-btn>
      <v-simple-table>
        <template v-slot:default>
          <thead>
          <tr>
            <th class="text-center"></th>
            <th class="text-center">Type</th>
            <th class="text-center">Name</th>
            <th class="text-center">Filter</th>
          </tr>
          </thead>
          <tbody>
          <template v-for="item in selectedElements">
            <tr :key="item.name" v-on:click="filterEdit(item.name)">
              <v-icon v-if="filterId===item.name">fas fa-angle-double-left</v-icon>
              <v-icon v-else>fas fa-angle-right</v-icon>

              <td>
                {{ item.type }}
              </td>
              <td>{{ item.name }}</td>
              <td v-if="startFilters[item.name] !== undefined" :title="hoverFilters(item.name)">
                {{ startFilters[item.name].length }}
              </td>
              <td v-else>0</td>
            </tr>
          </template>
          </tbody>
        </template>

      </v-simple-table>
    </v-container>
  </div>
</template>

<script>
import Graph from "./Graph.vue"

export default {
  name: "Start",
  graphButtons: [],
  selectedElements: [],
  nodes: [],
  edges: [],
  nodeModel: [],
  edgeModel: [],
  filterId: -1,
  startFilters: {},
  onlyConnected: true,
  props: {
    metagraph: Object,
    colors: {
      type: Object
    },
  },
  data() {
    return {
      graphButtons: this.graphButtons,
      selectedElements: this.selectedElements,
      nodes: this.nodes,
      edges: this.edges,
      nodeModel: this.nodeModel,
      edgeModel: this.edgeModel,
      filterId: this.filterId,
      onlyConnected: true,
      startFilters: this.startFilters,
      skipVis: true,
      selectionTab: 1
    }
  },
  created() {
    this.selectedElements = []
    this.nodeModel = []
    this.edgeModel = []
    this.startFilters = {}
    this.graphButtons = [
      {id: 0, label: "Default Graph", color: this.colors.buttons.graphs.inactive, active: true},
      {id: 1, label: "Cancer-Comorbidity Graph", color: this.colors.buttons.graphs.inactive, active: false},
      {id: 2, label: "Neuro-Drug Graph", color: this.colors.buttons.graphs.inactive, active: false}
    ]

    this.nodes = []
    this.edges = []
  },
  mounted() {
    this.$refs.startgraph.hideAllGroups(true, true)
    this.$refs.startgraph.hideAllEdges(true)
    this.$refs.startgraph.setPhysics(true)
    this.initLists(this.metagraph)
  },
  methods: {
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
    loadGraph: function (id) {
      console.log("load graph")
      if (id === -1) {
        this.graphLoad = {post: {nodes: {}, edges: {}}}
        this.selectedElements.forEach(element => {
          let filter = this.startFilters[element.name]
          if (filter === undefined)
            filter = []
          if (element.type === "node") {
            this.graphLoad.post.nodes[element.name.toLowerCase()] = {filters: filter}
          } else {
            this.graphLoad.post.edges[element.name] = {filters: filter}
          }
        })
        this.graphLoad.post.connectedOnly = this.onlyConnected
      } else {
        for (let index in this.graphButtons) {
          if (this.graphButtons[index].id === id) {
            if (this.graphButtons[index].active)
              return;
            this.graphButtons[index].active = true
          } else
            this.graphButtons[index].active = false
        }
        if (id === 0) {
          this.graphLoad = {name: "default"}
        } else if (id === 1) {
          this.graphLoad = {get: "/getExampleGraph1"}
          this.graphLoad = {
            post: {
              nodes: {
                disorder: {
                  filters: [
                    {
                      type: "match",
                      expression: ".*((neur)|(prion)|(brain)|(enceph)|(cogni)).*"
                    }
                  ]
                }, drug: {}
              },
              edges: {"DrugHasIndication": {}},
              connectedOnly: true
            }
          }
        }
      }
      if (Object.keys(this.graphLoad.post.nodes).length === 0) {
        this.$emit("printNotificationEvent", "Please select some nodes/edges first!", 1)
        return
      }
      this.graphLoad.post["uid"] = this.$cookies.get("uid")
      this.graphLoad["skipVis"] = this.skipVis;
      this.$emit("graphLoadEvent", this.graphLoad)
    },
    hoverFilters: function (name) {
      let out = ""
      this.startFilters[name].forEach(f => {
        out += f.type + " " + f.expression + "\n"
      })
      return out
    },
    toggleNode: function (nodeIndex) {
      let index = this.nodeModel.indexOf(nodeIndex)
      this.$refs.startgraph.toggleGroupVisibility(this.nodes[nodeIndex].label.toLowerCase(), true)
      if (index >= 0) {
        let remove = -1;
        for (let idx in this.selectedElements) {
          if (this.selectedElements[idx].type === "node" && this.selectedElements[idx].index === nodeIndex) {
            remove = idx;
            break;
          }
        }
        this.selectedElements.splice(remove, 1)
        this.nodeModel.splice(index, 1)
        Object.values(this.edges).filter(item => (item.depends.indexOf(this.nodes[nodeIndex].id) > -1 && this.edgeModel.indexOf(item.index) > -1)).map(item => item.index).forEach(this.toggleEdge)

      } else {
        this.nodeModel.push(nodeIndex)
        this.selectedElements.push({index: nodeIndex, type: "node", name: this.nodes[nodeIndex].label, filter: []})
      }
      this.$nextTick(() => {
        this.$refs.nodeSelector.$forceUpdate()
      })
    },
    toggleEdge: function (edgeIndex) {
      let index = this.edgeModel.indexOf(edgeIndex)
      this.$refs.startgraph.toggleEdgeVisible(this.edges[edgeIndex].label)
      if (index >= 0) {
        let remove = -1;
        let restDepending = []
        for (let idx in this.selectedElements) {
          if (this.selectedElements[idx].type === "edge") {
            if (this.selectedElements[idx].index === edgeIndex)
              remove = idx;
            else
              this.edges[this.selectedElements[idx].index].depends.forEach(d => restDepending.push(d))
          }
        }
        this.selectedElements.splice(remove, 1)
        this.edgeModel.splice(index, 1)
      } else {
        this.edgeModel.push(edgeIndex)
        let depending = []
        for (let idx in this.edges[edgeIndex].depends) {
          depending.push(this.edges[edgeIndex].depends[idx])
        }
        Object.values(this.nodes).filter(item => depending.indexOf(item.id) >= 0 && this.nodeModel.indexOf(item.index) === -1).forEach(item => this.toggleNode(item.index))
        this.selectedElements.push({index: edgeIndex, type: "edge", name: this.edges[edgeIndex].label, filter: []})
      }
    },
    filterEdit: function (filterName) {
      if (this.filterId === filterName) {
        this.filterId = -1
        this.$emit("filterEvent")
      } else {
        this.filterId = filterName
        if (this.startFilters[filterName] === undefined)
          this.startFilters[filterName] = []
        let filters = this.startFilters[filterName]
        this.$emit("filterEvent", {name: filterName, filters: filters})
      }
    },
    minimizeSide: function () {
      this.filterId = -1
    },
    removeFilter: function (data) {
      this.startFilters[data.name].splice(data.index, 1)
    },
    direction: function (edge) {
      let e = Object.values(this.metagraph.edges).filter(e => e.label === edge)[0];
      if (e.from === e.to)
        return 0
      return 1
    },
    getColoring: function (entity, name) {
      if (entity === "nodes") {
        return this.metagraph.colorMap[Object.values(this.metagraph.nodes).filter(n => n.label === name)[0].group].main;
      } else {
        let edge = Object.values(this.metagraph.edges).filter(n => n.label === name)[0]
        let n1 = Object.values(this.metagraph.nodes).filter(n => n.id === edge.from)[0].group
        let n2 = Object.values(this.metagraph.nodes).filter(n => n.id === edge.to)[0].group
        return [this.metagraph.colorMap[n1].main, this.metagraph.colorMap[n2].main]
      }
    },
  },
  components: {
    Graph,
  },

}
</script>

<style scoped>

</style>
