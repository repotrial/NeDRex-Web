<template>
  <div class="graph-window">
    <i>{{ text }}</i>
    <v-progress-linear v-show="loading" indeterminate :color=loadingColor></v-progress-linear>
    <network v-if="nodeSet !== undefined && visualize" v-show="!loading" class="wrapper" ref="network"
             :key="key"
             :nodes="nodeSet"
             :edges="edges"
             :options="options"
             :layout="layout"
             :physics="physics"
             :events="['click','release','startStabilizing','stabilizationProgress','stabilizationIterationsDone']"
             @click="onClick"
             @release="onRelease">
      <!--             @startStabilizing="onStabilizationStart"-->
      <!--             @stabilizationProgress="onStabilizationProgress"-->
      <!--             @stabilizationIterationsDone="onStabilizationDone"-->

      <!--    >-->
    </network>
    <template>
      <v-btn v-if="!sizeWarning()"
             v-show="!visualize"
             color="primary"
             dark
             v-on:click="dialogResolve(true)"
      >
        Visualize Graph!
      </v-btn>
      <v-dialog
        v-if="sizeWarning() && nodeSet !== undefined"
        v-model="dialog"
        persistent
        max-width="290"
      >
        <template v-slot:activator="{ on, attrs }">
          <v-btn
            v-show="!visualize"
            color="primary"
            dark
            v-bind="attrs"
            v-on="on"
          >
            Visualize Graph?
          </v-btn>
        </template>
        <v-card>
          <v-card-title class="headline">
            Load Visualization?
          </v-card-title>
          <v-card-text>The selected graph consists of {{ this.nodeSet.getIds().length }} nodes and
            {{ this.edges.getIds().length }} edges. Visualization and especially physics simulation could take a long
            time
            and cause freezes. Do you want to visualize the graph or skip it for now?
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn
              color="green darken-1"
              text
              @click="dialogResolve(false)"
            >
              Skip
            </v-btn>
            <v-btn
              color="green darken-1"
              text
              @click="dialogResolve(true)"
            >
              Visualize
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </template>
  </div>
</template>

<script>
import {DataSet} from 'vue-vis-network'

export default {
  name: "graph",
  props: {
    initgraph: Object,
    payload: Object,
  },
  key: 0,
  nodeSet: undefined,
  loading: true,
  text: "",
  nodes: Object,
  edges: Object,
  options: Object,
  layout: Object,
  physics: Object,
  highlight: false,
  colors: {},
  directed: false,
  loadingColor: 'primary',
  lastClick: 0,
  physicsOn: false,
  hideEdges: false,
  metagraph: undefined,
  dialog: false,
  visualize: true,

  created() {
    this.progress = 0
    this.key = 0
    this.loading = true;
    this.colors = {bar: {backend: "#6db33f", vis: 'primary', error: 'red darken-2'}}
    this.highlight = false
    this.loadData(this.initgraph)
    this.lastClick = 0
    this.physicsOn = false
    this.hideEdges = false
    this.dialog = false
    this.visualize = true
  },

  data() {
    return {
      edges: this.edges,
      nodeSet: this.nodeSet,
      options: this.options,
      layout: this.layout,
      physics: this.physics,
      key: this.key,
      text: this.text,
      loading: this.loading,
      loadingColor: this.loadingColor,
      dialog: this.dialog,
      visualize: this.visualize,
      skipVis: false,
    }
  }
  ,
  methods: {
    getData: function (url) {
      this.loading = true;
      this.directed = false;
      this.loadingColor = this.colors.bar.backend;
      this.$http.get(url).then(response => {
        return response.data
      }).then(graph => {
        this.setGraph(graph)
      }).catch(err => {
        this.loadingColor = this.colors.bar.error;
        console.log(err)
      })
    },

    setGraph: function (graph) {
      this.loadingColor = this.colors.bar.vis
      this.dialog = true;
      if (graph) {
        if (graph.directed !== undefined)
          this.directed = graph.directed
        if (graph.edges !== undefined)
          this.edges = new DataSet(graph.edges);

        if (graph.nodes !== undefined)
          this.nodeSet = new DataSet(graph.nodes)

        if (graph["options"] !== undefined)
          this.mergeOptions(graph.options)
      }
      if (this.skipVis) {
        this.dialog = false;
        this.visualize = false;
      } else if (!this.sizeWarning) {
        this.dialog = false;
        this.visualize = true
      }
      this.drawGraph();
    },
    dialogResolve: function (vis) {
      this.dialog = false;
      this.visualize = vis;
    },
    showDialogCheck: function () {
      if (!this.sizeWarning) {
        this.dialogResolve(true)
      }
    },
    isLoading: function () {
      return this.loading
    },
    drawGraph: function () {
      if (this.nodeSet === undefined || this.edges === undefined)
        return
      if (this.directed) {
        this.options.edges["arrows"] = {to: {enabled: true}}
      } else
        this.options.edges["arrows"] = {};
      this.key += 1
      this.nodes = this.nodeSet.get({returnType: "Object"})
      // this.setNodeColors()
      this.loading = false;
      this.$emit('finishedEvent')
    },
    loadData: function (payload) {
      this.visualize = true
      this.loading = true
      if (payload !== undefined && payload.skipVis !== undefined)
        this.skipVis = payload.skipVis

      if (payload !== undefined && payload.name !== undefined && payload.name === "metagraph" && this.metagraph === undefined) {
        this.metagraph = payload.graph;
      }
      if (this.metagraph !== undefined) {
        this.edges = new DataSet(this.metagraph.edges);
        this.nodeSet = new DataSet(this.metagraph.nodes);
        this.directed = this.metagraph.directed;
      }


      let defaults = this.getDefaults(payload)
      this.options = defaults.options;
      this.layout = defaults.layout;
      this.physics = defaults.physics;

      if (payload) {
        if (payload.get !== undefined)
          this.getData(payload.get)
        else if (payload.info !== undefined)
          this.loadInfo(payload.info)
        else if (payload.post !== undefined)
          this.postData(payload.post)
        else if (payload.data !== undefined)
          this.setGraph(payload.data)
        else
          this.drawGraph()
      } else
        this.drawGraph()
    },
    sizeWarning: function () {
      let warn = (this.nodeSet !== undefined && this.nodeSet.getIds().length > 1000) || (this.edges !== undefined && this.edges.getIds().length > 1000)
      return warn
    },
    loadInfo: function (info){
      this.visualize = false
      this.loading = true;
      this.directed = false;
      this.loadingColor = this.colors.bar.backend;
      this.evalPostInfo(info)
    },
    evalPostInfo: function (info) {

      console.log(info)
      console.log(info.id)
      let sum = 0
      for (let n in info.nodes)
        sum += info.nodes[n];
      for (let e in info.edges)
        sum += info.edges[e]
      console.log(sum)
      if (sum >= 100000)
        this.$emit("sizeWarningEvent", info)
      else {
        if (!this.skipVis)
          this.visualize = true;
        this.$http.get("/getGraph?id=" + info.id).then(response => {
          if (response !== undefined)
            return response.data
        })
          .then(graph => {
            console.log(graph)
            this.$cookies.set("gid", graph.id)
            this.$emit("graphLoadedEvent", graph.id)
            this.setGraph(graph)
          }).catch(err => {
          this.loadingColor = this.colors.bar.error;
          console.log(err)
        })
      }

    },
    postData: function (post) {
      this.visualize = false
      this.loading = true;
      this.directed = false;
      this.loadingColor = this.colors.bar.backend;
      this.$http.post("/getGraphInfo", post).then(response => {
        return response.data
      }).then(info => {
        this.evalPostInfo(info)
      }).catch(err => {
        console.log(err)
      })
    }
    ,
    getCurrentGraph: function () {
      if (this.nodeSet === undefined) {
        setTimeout(this.getCurrentGraph, 100)
      } else {
        return {nodes: this.nodeSet, edges: this.edges, options: this.options}
      }


    }
    ,
    setSelection: function (nodes) {
      if (nodes !== undefined && nodes[0] !== undefined) {
        this.$refs.network.selectNodes(nodes)
        this.identifyNeighbors(nodes[0])
        this.focusNode(nodes[0])
      } else {
        this.$refs.network.unselectAll()
        this.$emit("selectionEvent")
        this.focusNode()
      }

    }
    ,
// setNodeColors: function () {
//   let nodesContainColor = false
//   if (this.options.nodes.color !== undefined) {
//     this.nodeSet.getIds().forEach(id => {
//       if (this.nodeSet.get(id).color !== undefined)
//         nodesContainColor = true;
//     })
//     if (!nodesContainColor) {
//       for (let id in this.nodes) {
//         this.nodes[id].color = this.options.nodes.color
//       }
//     }
//   }
//   this.updateNodes()
// },
//     loadMetagraph: function () {
//       return new Promise(() => {
//         if (this.metagraph === undefined) {
//           this.$http.get("/getMetagraph").then(response => {
//             console.log("set metagraph")
//             this.metagraph = response.data
//             return response.data
//           }).catch(err => {
//             this.loadingColor = this.colors.bar.error;
//             console.log(err)
//           })
//         } else {
//           return this.metagraph
//         }
//       })
//     }
//     ,

    getDefaults: function () {
      return {
        // nodes: new DataSet(this.metagraph.nodes),
        // edges: new DataSet(this.metagraph.edges),
        // directed: this.metagraph.directed,
        layout: {
          improvedLayout: false,
          // clusterThreshold: 1000,
          // hierarchical: {enabled: true}

        },
        options: {
          groups: {
            drug: {
              color: {
                border: '#00CC96',
                background: '#b4cdcc',
                highlight: {border: '#00CC96', background: '#b4cdcc'}
              }
            },
            disorder: {
              color: {
                border: '#EF553B',

                background: '#ecd0cb',
                highlight: {border: '#EF553B', background: '#ecd0cb'}
              }
            },
            gene: {
              color: {
                border: '#636EFA',

                background: '#d6d9f8',
                highlight: {border: '#636EFA', background: '#d6d9f8'}
              }
            },
            protein: {
              color: {
                border: '#19d3f3',

                background: '#bcdfe5',
                highlight: {border: '#19d3f3', background: '#bcdfe5'}
              }
            },
            pathway: {
              color: {
                border: '#fecb52',

                background: '#fae6c1',
                highlight: {border: '#fecb52', background: '#fae6c1'}
              }
            },
            other: {
              color: {border: '#6b6a6a', background: '#D2E5FF', highlight: {border: '#6b6a6a', background: '#D2E5FF'}}
            }
          },
          nodes: {
            fixed: false,
            physics: true,
            borderWidth: 2

          },
          edges: {
            hidden: false,
            // arrows:{to:{enabled:true}},
            // scaling:{label:{enabled: true}},
            smooth: {enabled: true},
            color: '#454545',
            // hidden: true,
            width: 0.3,
            physics: true,
            // length:300
          },
          physics: {
            // solver: 'repulsion',
            enabled: false,
            stabilization: {enabled: true, updateInterval: 10, iterations: 1000, fit: true},
            timestep: 0.3,
            // wind: {x: 20, y: 20}
          }
        },

      }
      // }).catch(err => {
      //   this.loadingColor = this.colors.bar.error;
      //   console.log(err)
      // })
    }
    ,
    togglePhysics: function () {
      if (this.physicsOn) {
        this.physicsOn = false;
        this.hideEdges = false;
      } else {
        this.hideEdges = true;
        this.physicsOn = true;
      }
      this.options.physics.enabled = this.physicsOn
      this.options.edges.hidden = this.hideEdges
      this.$refs.network.setOptions(this.options)
    }
    ,
    mergeOptions: function (options) {
      //TODO merge
      this.options = options;
    }
    ,
    onStabilizationStart: function () {
      this.loading = true;
      this.loadingColor = this.colors.bar.primary
      console.log("stabilization has started")
    }
    ,
    onStabilizationProgress: function (params) {


      this.progress = 100 * params.iterations / params.total;
      console.log(this.progress)
      // this.width = Math.max(this.minWidth, this.maxWidth * widthFactor);
      this.text = "Graph generation " + Math.round(this.progress) + "%";
      console.log(this.text)
      //
      // console.log(Math.round(widthFactor * 100) + "%")
    }
    ,
    onStabilizationDone: function () {
      this.text = "100%";
      this.width = this.maxWidth
      this.loading = false;
    }
    ,
    onClick: function (params) {
      if (params.nodes.length > 0 || params.edges.length > 0) {
        // let selected = params.nodes[0];
        // this.identifyNeighbors(selected)
        this.$emit('selectionEvent', params)
      } else {
        // for (let nodeId in this.nodes) {
        //   this.nodes[nodeId].color = undefined
        //   if (this.nodes[nodeId].hiddenLabel !== undefined) {
        //     this.nodes[nodeId].label = this.nodes[nodeId].hiddenLabel;
        //     this.nodes[nodeId].hiddenLabel = undefined;
        //   }
        this.$emit('selectionEvent')
        // }
        this.highlight = false;
      }


      this.updateNodes()

    }
    ,
    onRelease: function (params) {
      if (new Date().getTime() - this.lastClick < 500)
        return;
      this.$emit('releaseEvent', params)
    }
    ,
    mapEdgeObject: function (edgeId) {
      return this.edges._data[edgeId]
    }
// deselectNode: function(nodeId){
//   this.$refs.network.
// }
    ,
    focusNode: function (nodeId) {
      this.$refs.network.focus(nodeId)
      if (nodeId === undefined)
        this.viewAll()
    }
    ,
    setSelectionMultiple: function (items) {
      // for(let idx in selection) {
      this.$refs.network.selectNodes(items.nodes)
      this.$refs.network.selectEdges(items.edges)
      // }
    }
    ,
    mapEdgeObjects: function (edgeIds) {
      let edges = []
      for (let idx in edgeIds)
        edges.push(this.mapEdgeObject(edgeIds[idx]))

      return edges;
    }
    ,
    viewAll: function () {
      this.$refs.network.fit()
    }
    ,
    getAllNodes: function () {
      let nodes = []
      this.nodeSet.getDataSet().forEach(n => nodes.push(n))
      return {neighbors: nodes}
    }
    ,
    identifyNeighbors: function (selected) {
      //   this.highlight = true;
      //   this.uncolorNodes()
      //
      //   let toColor = []
      //   toColor.push(selected)
      let neighbors = []
      this.getConnectedNodes(selected).forEach(n => {
        // toColor.push(n)
        if (n !== selected)
          neighbors.push(this.nodeSet.get(n))
      })
      //   this.recolorPrimary(toColor)
      return {primary: this.nodeSet.get(selected), neighbors: neighbors}
      // this.$emit('selectionEvent', {primary: this.nodeSet.get(selected), neighbors: neighbors})
    }
    ,
    uncolorNodes: function () {
      for (let nodeId in this.nodes) {
        this.nodes[nodeId].color = "rgba(200,200,200,0.5)";
        if (this.nodes[nodeId].hiddenLabel === undefined) {
          this.nodes[nodeId].hiddenLabel = this.nodes[nodeId].label;
          this.nodes[nodeId].label = undefined;
        }
      }
    }
    ,
    updateNodes: function () {
      let updates = []

      for (let nodeId in this.nodes) {
        if (this.nodes.hasOwnProperty(nodeId))
          updates.push(this.nodes[nodeId])
      }
      this.nodeSet.update(updates)
      // this.key++;
    }
    ,
    recolorPrimary: function (nodes) {
      nodes.forEach(nodeId => {
        this.nodes[nodeId].color = undefined;
        if (this.nodes[nodeId].hiddenLabel !== undefined) {
          this.nodes[nodeId].label = this.nodes[nodeId].hiddenLabel;
          this.nodes[nodeId].hiddenLabel = undefined;
        }
      })
    }
    ,
    getConnectedNodes: function (node) {
      return this.$refs.network.getConnectedNodes(node)
    }
    ,
    getNetwork: function () {
      return this.$refs.network;
    }


  }
}
</script>

<style scoped lang="sass">
.graph-window
  border: 1px solid gray
  min-height: 75vh
  height: 75vh

.wrapper
  min-height: 100%

  background-color: #ffffff
  padding: 10px
  height: 100%


</style>
