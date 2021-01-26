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
               :configuration="{visualized:true}" :window-style="windowStyle" >
        </Graph>
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
          <template v-for="item in edges">
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
    </v-row>
  </v-container>
  </v-card>
  </v-container>
</template>

<script>
import Utils from "../../scripts/Utils";
import Graph from "../Graph";

export default {
  name: "Advanced",
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
      interactions: {ProteinInteractsWithProtein: false, GeneInteractsWithGene: false},
      nodes: [],
      edges: [],
      nodeModel: [],
      edgeModel: [],
      filterId: -1,
      windowStyle:{
        height: '75vh',
        'min-height': '75vh',
      }
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
    loadGraph: function (bool) {
      let graphLoad = {}
      if (!bool) {
        this.options.selectedElements.forEach(e => {
          if (e.type === "node") {
            this.$refs.startgraph.hideGroupVisibility(this.nodes[e.index].label.toLowerCase(), true)
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
    toggleNode: function (nodeIndex) {
      let index = this.nodeModel.indexOf(nodeIndex)
      this.$refs.startgraph.hideGroupVisibility(this.nodes[nodeIndex].label.toLowerCase(), index>-1,true)
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
