<template>
  <div style="margin-top:20px">
    <v-container>
      <v-card class="mx-auto">
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title">
              Predefined Graphs
            </v-list-item-title>
            <v-list-item-subtitle>
              Start exploring by choosing a start example
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
        <v-divider></v-divider>
        <v-btn-toggle tile group>
          <template v-for="item in graphButtons">
            <v-btn :name=item.id :outlined="item.active" v-on:click="loadGraph(item.id)"
                   :color=item.color style="margin:5px">
              {{ item.label }}
            </v-btn>
          </template>
        </v-btn-toggle>
      </v-card>
    </v-container>
    <v-container>
      <v-card class="mx-auto">
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title">
              Customized Exploration
            </v-list-item-title>
            <v-list-item-subtitle>
              Query a specified starting graph.
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
        <v-divider></v-divider>

        <v-list-item style="align-content: center">
          <v-container>
            <v-row>
              <v-col>
                <Graph ref="graph" v-on:selectionEvent="setSelection" v-on:releaseEvent="resetSelection"></Graph>
              </v-col>
              <v-col>
                <v-list-item-group multiple color="indigo" v-model="edgeModel">
                  <v-list-item v-for="item in edges" v-on:click="toggleEdge(item.index,false)" :key="item.index">
                    <v-list-item-content>
                      <v-list-item-title v-text="item.label"></v-list-item-title>
                    </v-list-item-content>
                  </v-list-item>
                </v-list-item-group>
              </v-col>
            </v-row>
          </v-container>
        </v-list-item>
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
                <td v-if="filters[item.name] !== undefined" :title="hoverFilters(item.name)">
                  {{filters[item.name].length }}
                </td>
                <td v-else>0</td>
              </tr>
            </template>
            </tbody>
          </template>

        </v-simple-table>
      </v-card>
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
  filters: {},
  onlyConnected: true,
  props: {
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
      filters: this.filters
    }
  },
  created() {
    this.selectedElements = []
    this.nodeModel = []
    this.edgeModel = []
    this.filters = {}
    this.graphButtons = [
      {id: 0, label: "Default Graph", color: this.colors.buttons.graphs.inactive, active: true},
      {id: 1, label: "Cancer-Comorbidity Graph", color: this.colors.buttons.graphs.inactive, active: false},
      {id: 2, label: "Neuro-Drug Graph", color: this.colors.buttons.graphs.inactive, active: false}
    ]

    this.nodes = []
    this.edges = []
  },
  mounted() {
    let selectionGraph = this.$refs.graph.getCurrentGraph()
    selectionGraph.nodes.forEach(n => {
      this.nodes.push({index: this.nodes.length, id: n.id, label: n.label})
    })
    selectionGraph.edges.forEach(e => {
      let depends = [e.from]
      if (e.from !== e.to)
        depends.push(e.to)
      this.edges.push({index: this.edges.length, id: e.id, label: e.label, depends: depends})
    })
  },
  methods: {
    loadGraph: function (id) {
      console.log("load graph")
      if (id === -1) {
        this.graphLoad = {post: {nodes: {}, edges: {}}}
        this.selectedElements.forEach(element => {
          let filter = this.filters[element.label]
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
        } else if (id === 2) {
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
      console.log(this.graphLoad)
      this.$emit("graphLoadEvent", this.graphLoad)
    },
    hoverFilters: function (name){
      let out = ""
      this.filters[name].forEach(f=>{
        out+=f.type+" "+f.expression+"\n"
      })
      return out
    },
    toggleNode: function (nodeIndex, graphSelect) {
      let index = this.nodeModel.indexOf(nodeIndex)
      if (index >= 0) {
        let remove = -1;
        for (let idx in this.selectedElements) {
          if (this.selectedElements[idx].type === "node" && this.selectedElements[idx].index === nodeIndex) {
            remove = idx;
            break;
          }
        }
        this.selectedElements.splice(remove, 1)
        if (graphSelect)
          this.nodeModel.splice(index, 1)
      } else {
        if (graphSelect)
          this.nodeModel.push(nodeIndex)
        this.selectedElements.push({index: nodeIndex, type: "node", name: this.nodes[nodeIndex].label, filter: []})
      }
    },
    filterEdit: function (filterName) {
      if (this.filterId === filterName) {
        this.filterId = -1
        this.$emit("filterEvent")
      }
      else {
        this.filterId = filterName
        let filters = this.filters[filterName]
        this.$emit("filterEvent", {name:filterName, filters: filters})
      }
    },
    minimizeSide: function (){
      this.filterId = -1
      this.$emit("filterEvent")
    },
    addFilter: function (data) {
      if (this.filters[data.name] === undefined)
        this.filters[data.name] = []
      this.filters[data.name].push(data.filter)
    },
    setSelection: function (params) {
      if (params !== undefined) {
        if (params.nodes.length > 0) {
        } else if (params.edges.length > 0) {
          for (let idx in this.edges) {
            if (this.edges[idx].id === params.edges[0]) {
              this.toggleEdge(this.edges[idx].index, true)
            }
          }
        }
      }
      this.resetSelection()
    },

    resetSelection: function () {
      let edgeIds = []
      this.edgeModel.forEach(e => {
        edgeIds.push(this.edges[e].id)
      })
      let nodeIds = []
      this.nodeModel.forEach(n => {
        nodeIds.push(this.nodes[n].id)
      })
      let data = {nodes: nodeIds, edges: edgeIds}
      this.$refs.graph.setSelectionMultiple(data)
    },
    toggleEdge: function (edgeIndex, graphSelect) {
      let index = this.edgeModel.indexOf(edgeIndex)
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
        this.edges[edgeIndex].depends.forEach(d => {
          if (restDepending.indexOf(d) === -1)
            for (let nodeIndex in this.nodes)
              if (this.nodes[nodeIndex].id === d && this.nodeModel.indexOf(nodeIndex) > -1) {
                this.toggleNode(nodeIndex, true)
              }
        })
        if (graphSelect)
          this.edgeModel.splice(index, 1)
      } else {
        if (graphSelect)
          this.edgeModel.push(edgeIndex)
        let depending = []
        for (let idx in this.edges[edgeIndex].depends) {
          depending.push(this.edges[edgeIndex].depends[idx])
        }
        for (let idx in depending) {
          for (let nodeIndex in this.nodes)
            if (this.nodes[nodeIndex].id === depending[idx] && this.nodeModel.indexOf(nodeIndex) === -1) {
              this.toggleNode(nodeIndex, true)
            }
        }
        this.selectedElements.push({index: edgeIndex, type: "edge", name: this.edges[edgeIndex].label, filter: []})
      }
      this.resetSelection()
    }
  },
  components: {
    Graph,
  },

}
</script>

<style scoped>

</style>
