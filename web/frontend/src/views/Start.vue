<template>
  <div style="margin-top:20px">
    <v-card>
      <v-list>
        <v-list-item>
          <v-list-item-title class="title">Select your exploration path</v-list-item-title>
        </v-list-item>
        <v-list-item>
          <v-list-item-subtitle>For new users or basic tasks like drug-target identification, the guided path is
            recommended.
          </v-list-item-subtitle>
        </v-list-item>
        <v-divider></v-divider>
        <v-list-item>
          <v-tabs v-model="selectionTab" centered>
            <v-tabs-slider color="blue"></v-tabs-slider>
            <v-tab>Guided Exploration</v-tab>
            <v-tab>Quick Start: Disease Modules</v-tab>
            <v-tab>Quick Start: Drug Repurposing</v-tab>
            <v-tab>Advanced Exploration</v-tab>
          </v-tabs>
        </v-list-item>
      </v-list>
    </v-card>

    <v-container v-show="selectionTab===0">
      <v-card class="mx-auto">
        <v-list>
          <v-list-item>
            <v-list-item-title class="title">
              Guided exploration
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-subtitle>
              Selection of a starting point (e.g. Disorder) and then targets of interest (e.g. Drug or Gene/Protein) in different steps for easy usage.
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
      </v-card>
    </v-container>

    <v-container v-show="selectionTab===1">
      <v-card class="mx-auto">
        <v-list>
          <v-list-item>
            <v-list-item-title class="title">
              Disease Modules
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-subtitle>
              Selection of seed Genes/Proteins (manual or BiCon) for algorithmic disease module identification
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
      </v-card>
    </v-container>

    <v-container v-show="selectionTab===2">
      <v-card class="mx-auto">
        <v-list>
          <v-list-item>
            <v-list-item-title class="title">
              Drug Repurposing
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-subtitle>
              Starting e.g. with a disorder to derive Genes/Proteins for module identification and subsequent drug ranking (e.g. trustrank)
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
      </v-card>
    </v-container>

    <v-container v-show="selectionTab===3">
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
            <v-col cols="6">
              <Graph ref="startgraph" :initgraph="{graph:metagraph,name:'metagraph'}" :startGraph="true"
                     :configuration="{visualized:true}"></Graph>
            </v-col>
            <v-col cols="2">
              <v-list v-model="nodeModel" ref="nodeSelector">
                <v-card-title>Nodes</v-card-title>
                <v-list-item v-for="item in nodes" :key="item.index">
                  <v-chip outlined v-on:click="toggleNode(item.index)"
                          :color="nodeModel.indexOf(item.index)===-1?'gray':'primary'"
                          :text-color="nodeModel.indexOf(item.index)===-1?'black':'gray'"
                  >
                    <!--                    <v-icon left :color="getColoring('nodes',item.label)">fas fa-genderless</v-icon>-->
                    <v-icon left :color="getColoring('nodes',item.label)">fas fa-genderless</v-icon>
                    {{ item.label }}
                    <span style="color: gray; margin-left: 3pt"
                          v-show="nodeModel.indexOf(item.index)>-1">({{
                        metagraph.weights.nodes[item.label.toLowerCase()]
                      }})</span>
                  </v-chip>
                </v-list-item>
              </v-list>

            </v-col>
            <v-col cols="4">
              <v-list v-model="edgeModel">
                <v-card-title>Edges</v-card-title>
                <template v-for="item in edges" >
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
                          v-show="edgeModel.indexOf(item.index)>-1">({{ metagraph.weights.edges[item.label] }})</span>
                  </v-chip>
                </v-list-item>
                  <v-list-item v-show="edgeModel.indexOf(item.index)>-1 && (item.label==='ProteinInteractsWithProtein' ||item.label==='GeneInteractsWithGene' )">
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
          </v-row>
        </v-container>
      </v-card>
    </v-container>
  </div>
</template>

<script>
import Graph from "./Graph.vue"
import socket from "../services/socket.js"
import Utils from "../scripts/Utils"

export default {
  name: "Start",
  graphButtons: [],
  nodes: [],
  edges: [],
  nodeModel: [],
  edgeModel: [],
  filterId: -1,
  // startFilters: {},
  props: {
    options: Object,
    metagraph: Object,
    filters: Object,
    colors: {
      type: Object
    },
  },
  data() {
    return {
      interactions:{ProteinInteractsWithProtein:false, GeneInteractsWithGene:false},
      graphButtons: this.graphButtons,
      nodes: this.nodes,
      edges: this.edges,
      nodeModel: this.nodeModel,
      edgeModel: this.edgeModel,
      filterId: this.filterId,
      selectionTab: 3,
    }
  },
  created() {
    this.nodeModel = []
    this.edgeModel = []
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
    setOptions: function (options) {
      this.options = options;
    },
    loadGraph: function (id, bool) {
      console.log("load graph")
      if (id === -1) {
        if (!bool) {
          this.options.selectedElements.forEach(e => {
            if (e.type === "node") {
              this.$refs.startgraph.toggleGroupVisibility(this.nodes[e.index].label.toLowerCase(), true)
            } else
              this.$refs.startgraph.toggleEdgeVisible(this.edges[e.index].label)
          })
          this.nodeModel = []
          this.edgeModel = []
          this.options.selectedElements.length = 0
          this.filters.length = 0
          this.$nextTick(() => {
            this.$refs.nodeSelector.$forceUpdate()
          })
          return
        }
        this.graphLoad = {post: {nodes: {}, edges: {}}}
        this.options.selectedElements.forEach(element => {
          let filter = this.filters[element.name]
          if (filter === undefined)
            filter = []
          if (element.type === "node") {
            this.graphLoad.post.nodes[element.name.toLowerCase()] = {filters: filter}
          } else {
            this.graphLoad.post.edges[element.name] = {filters: filter}
          }
        })
        this.graphLoad.post.connectedOnly = this.options.onlyConnected
        this.graphLoad.post.interactions ={...this.interactions}
      } else {
        for (let index in this.graphButtons) {
          if (this.graphButtons[index].id === id) {
            if (this.graphButtons[index].active)
              return;
            this.graphButtons[index].active = true
          } else
            this.graphButtons[index].active = false
        }
      //   if (id === 0) {
      //     this.graphLoad = {name: "default"}
      //   } else if (id === 1) {
      //     this.graphLoad = {get: "/getExampleGraph1"}
      //     this.graphLoad = {
      //       post: {
      //         nodes: {
      //           disorder: {
      //             filters: [
      //               {
      //                 type: "match",
      //                 expression: ".*((neur)|(prion)|(brain)|(enceph)|(cogni)).*"
      //               }
      //             ]
      //           }, drug: {}
      //         },
      //         edges: {"DrugHasIndication": {}},
      //         connectedOnly: true
      //       }
      //     }
      //   }
      }
      if (Object.keys(this.graphLoad.post.nodes).length === 0) {
        this.$emit("printNotificationEvent", "Please select some nodes/edges first!", 1)
        return
      }
      this.graphLoad.post["uid"] = this.$cookies.get("uid")
      this.graphLoad["skipVis"] = this.options.skipVis;
      this.$emit("graphLoadEvent", this.graphLoad)
    },
    toggleNode: function (nodeIndex) {
      let index = this.nodeModel.indexOf(nodeIndex)
      this.$refs.startgraph.toggleGroupVisibility(this.nodes[nodeIndex].label.toLowerCase(), true)
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
      }
      this.$nextTick(() => {
        this.$refs.startgraph.focusNode()
      })
    },
    direction: function (edge) {
      if (Utils.isEdgeDirected(this.metagraph, edge))
        return 1
      return 0
    },
    getColoring: function (entity, name) {
      return Utils.getColoring(this.metagraph, entity, name)
    },
  },
  components: {
    Graph,
  },

}
</script>

<style scoped>

</style>
