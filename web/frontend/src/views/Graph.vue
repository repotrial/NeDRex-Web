<template>
  <div class="graph-window" :style="windowStyle">
    <i>{{ text }}</i>
    <v-progress-linear v-if="progress ===undefined" v-show="loading" indeterminate
                       :color=loadingColor></v-progress-linear>
    <v-progress-linear v-else v-show="progress <100" :value="progress" :color=loadingColor></v-progress-linear>
    <network v-if="nodeSet !== undefined && isVisualized()" v-show="!loading" class="wrapper" ref="network"
             :key="key"
             :nodes="nodeSet"
             :edges="edgeSet"
             :options="options"
             :layout="layout"
             :physics="physics"
             :events="['click','release','startStabilizing','stabilizationProgress','stabilizationIterationsDone']"
             @click="onClick">
    </network>
    <template>
      <v-dialog
        v-if="configuration.sizeWarning && nodeSet !== undefined"
        v-model="dialog"
        persistent
        max-width="290"
      >
        <v-card>
          <v-card-title class="headline">
            Load Visualization?
          </v-card-title>
          <v-card-text>The selected graph consists of {{ nodeSet.getIds().length }} nodes and
            {{ edgeSet.getIds().length }} edges. Visualization and especially physics simulation could take a long
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
    configuration: Object,
    startGraph: false,
    progress: Number,
    windowStyle: {
      height: '75vh',
      'min-height': '75vh',
    }
  },
  nodes: Object,
  highlight: false,
  colors: {},
  directed: false,
  lastClick: 0,
  physicsOn: false,
  hideEdges: false,
  metagraph: undefined,
  gid: undefined,
  unconnected: [],

  data() {
    return {
      edgeSet: Object,
      nodeSet: undefined,
      options: Object,
      layout: Object,
      physics: Object,
      key: 0,
      text: "",
      loading: true,
      loadingColor: 'primary',
      dialog: false,
      skipVis: false,
      skipDialog: false,
    }
  }
  ,
  created() {
    this.key = 0
    this.loading = true;
    this.colors = {bar: {backend: "#6db33f", vis: 'primary', error: 'red darken-2'}}
    this.highlight = false
    this.loadData(this.initgraph)
    this.lastClick = 0
    this.physicsOn = false
    this.hideEdges = false
    this.dialog = false
    this.configuration.visualized = true
    this.skipVis = true
    if (!this.startGraph) {
      this.gid = this.$route.params["gid"]
      if (this.gid !== undefined)
        this.init()
    }
  },


  methods: {
    reload: function () {
      this.key = 0
      this.loading = true;
      this.configuration.visualized = false
      this.gid = this.$route.params["gid"]
      this.skipDialog = false
      if (this.gid !== undefined)
        this.init()
    },
    show: function (gid) {
      this.gid = gid;
      this.skipVis = false
      this.skipDialog = true
      return this.init()
    },
    prepare: function () {
      if (this.startGraph || this.nodeSet === undefined)
        return
      this.prepareUnconnectedList()
    },
    setLoading: function (val) {
      this.loading = val
    },
    prepareUnconnectedList: function () {
      this.unconnected = this.nodeSet.getIds()
      this.edgeSet.get().forEach(item => {
        if (item.from === item.to)
          return
        let idx = this.unconnected.indexOf(item.from)
        if (idx > -1)
          this.unconnected.splice(idx, 1)
        idx = this.unconnected.indexOf(item.to)
        if (idx > -1)
          this.unconnected.splice(idx, 1)
      })
    },
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
    init: function () {
      if (!this.skipVis)
        this.configuration.visualized = true;
      //TODO getgraphinfo to handle displaying of sidebar and disabling of enable-physics and disable visualize graph when graph not loaded
      return this.$http.get("/getGraph?id=" + this.gid).then(response => {

        if (response !== undefined)
          return response.data
      })
        .then(graph => {
          this.setGraph(graph)
        }).catch(err => {
          this.loadingColor = this.colors.bar.error;
          console.log(err)
        })
    },
    setGraph: function (graph) {
      this.loadingColor = this.colors.bar.vis
      if (!this.skipDialog)
        this.dialog = true;
      if (graph) {
        if (graph.directed !== undefined)
          this.directed = graph.directed
        if (graph.edges !== undefined)
          this.edgeSet = new DataSet(graph.edges);

        if (graph.nodes !== undefined)
          this.nodeSet = new DataSet(graph.nodes)

        if (graph["options"] !== undefined)
          this.mergeOptions(graph.options)
      }
      this.checkSizeWarning()
      if (this.skipVis) {
        this.dialog = false;
        this.configuration.visualized = false;
      } else if (!this.configuration.sizeWarning) {
        this.dialog = false;
        this.configuration.visualized = true
      }
      this.drawGraph();
    },
    dialogResolve: function (vis) {
      this.dialog = false;
      this.configuration.visualized = vis;
      if (vis) {
        this.$forceUpdate()
        this.$emit("visualisationEvent")
      }
    },
    isVisualized: function () {
      return this.configuration.visualized
    },
    graphExists: function () {
      return this.$refs.network !== undefined
    },
    setWindowStyle: function (style) {
      this.windowStyle = style
    },
    // showDialogCheck: function () {
    //   this.checkSizeWarning()
    //   if (!this.configuration.sizeWarning) {
    //     this.dialogResolve(true)
    //   }
    // },
    // isLoading: function () {
    //   return this.loading
    // },
    drawGraph: function () {
      if (this.nodeSet === undefined || this.edgeSet === undefined)
        return
      if (this.directed) {
        this.options.edges["arrows"] = {to: {enabled: true}}
      } else
        this.options.edges["arrows"] = {};
      this.key += 1
      this.nodes = this.nodeSet.get({returnType: "Object"})
      // this.setNodeColors()
      this.loading = false;
      this.prepare()
      this.$emit('finishedEvent')
    },
    loadData: function (payload) {
      this.configuration.visualized = true
      this.loading = true
      if (payload !== undefined && payload.skipVis !== undefined)
        this.skipVis = payload.skipVis

      if (payload !== undefined && payload.name !== undefined && payload.name === "metagraph" && this.metagraph === undefined) {
        this.metagraph = payload.graph;
      }
      if (this.metagraph !== undefined) {
        this.edgeSet = new DataSet(this.metagraph.edges);
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
        else if (payload.data !== undefined)
          this.setGraph(payload.data)
        else
          this.drawGraph()
      } else
        this.drawGraph()
    },
    checkSizeWarning: function () {
      this.configuration.sizeWarning = (this.nodeSet !== undefined && this.nodeSet.getIds().length > 1000) || (this.edgeSet !== undefined && this.edgeSet.getIds().length > 1000)
    },


    getCurrentGraph: function () {
      if (this.nodeSet === undefined) {
        setTimeout(this.getCurrentGraph, 100)
      } else {
        return {nodes: this.nodeSet, edges: this.edgeSet, options: this.options}
      }
    }
    ,
    setSelection: function (nodes) {
      if (nodes !== undefined && nodes[0] !== undefined) {
        this.$refs.network.selectNodes(nodes)
        this.identifyNeighbors(nodes[0])
        this.zoomToNode(nodes[0])
      } else {
        this.$refs.network.unselectAll()
        this.$emit("selectionEvent")
        this.focusNode()
      }

    }
    ,


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
              // hidden: false,
              color: {
                border: '#00CC96',
                background: '#b4cdcc',
                highlight: {border: '#00CC96', background: '#b4cdcc'}
              }
            },
            disorder: {
              // hidden: false,
              color: {
                border: '#EF553B',
                background: '#ecd0cb',
                highlight: {border: '#EF553B', background: '#ecd0cb'}
              }
            },
            gene: {
              // hidden: false,
              color: {
                border: '#636EFA',

                background: '#d6d9f8',
                highlight: {border: '#636EFA', background: '#d6d9f8'}
              }
            },
            geneModule: {
              // hidden: false,
              color: {
                border: '#636EFA',

                background: '#636EFA',
                highlight: {border: '#636EFA', background: '#636EFA'}
              }
            },
            protein: {
              // hidden: false,
              color: {
                border: '#19d3f3',
                background: '#bcdfe5',
                highlight: {border: '#19d3f3', background: '#bcdfe5'}
              }
            },
            proteinModule: {
              // hidden: false,
              color: {
                border: '#19d3f3',
                background: '#19d3f3',
                highlight: {border: '#19d3f3', background: '#19d3f3'}
              }
            },
            pathway: {
              // hidden: false,
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
            // shape:"circle",
            fixed: false,
            physics: true,
            borderWidth: 2,
            // size:25,
            // scaling:{
            //   min:0,
            //   max:100,
            // label:{
            //   enabled:true
            // },
            // customScalingFunction:this.scalingFunction
            // }
          },
          edges: {
            hidden: false,
            // arrows:{to:{enabled:true}},
            // scaling:{label:{enabled: true}},
            smooth: {enabled: false},
            color: '#454545',
            width: 0.3,
            physics: true,
            // length:300
          },
          physics: {
            // solver: 'repulsion',
            barnesHut: {
              gravitationalConstant: -10000,
              centralGravity: 0.20,
              springLength: 200,
              springConstant: 0.04,
              damping: 0.5,
              avoidOverlap: 0.8
            },
            enabled: false,
            // stabilization: {enabled: true, updateInterval: 10, iterations: 1000, fit: true},
            // timestep: 0.3,
            // wind: {x: 20, y: 20}
          },
        },

      }
      // }).catch(err => {
      //   this.loadingColor = this.colors.bar.error;
      //   console.log(err)
      // })
    }
    ,
    // scalingFunction:function(min,max,total,value){
    //   if (max === min || value===undefined) {
    //     return 0.5;
    //   }
    //   else {
    //     let scale = 1 / (max - min);
    //     return Math.max(0,(value - min)*scale);
    //   }
    // },
    // getOptions: function () {
    //   return this.options
    // },
    setOptions: function (options) {
      this.options = options;
      this.updateOptions()
    },
    modifyGroups: function (nodeIds, group) {
      let updates = this.nodeSet.get().filter(n => nodeIds.indexOf(n.id) > -1).map(n => {
        return {id: n.id, group: group}
      })
      this.nodeSet.update(updates)
    },
    setPhysics: function (boolean) {
      this.physicsOn = boolean;
      this.options.physics.enabled = this.physicsOn
      if (!this.physicsOn && this.isVisualized()) {
        this.saveLayout()
      }
      this.updateOptions()
    },

    saveLayout: function () {
      let updates = Object.entries(this.$refs.network.getPositions()).map(e => {
        return {id: e[0], x: e[1].x, y: e[1].y}
      })
      this.updateNodes(updates)
    },

    updateOptions: function () {
      this.$refs.network.setOptions(this.options)
    }
    ,
    mergeOptions: function (options) {
      //TODO merge
      this.options = options;
    },

    hideAllEdges: function (boolean) {
      let updates = Object.values(this.edgeSet.get()).map(item => {
        return {id: item.id, hidden: boolean, physics: boolean}
      })
      if (updates)
        this.edgeSet.update(updates)
    },
    toggleEdgeVisible: function (name) {
      let updates = Object.values(this.edgeSet.get({
          filter: function (item) {
            return item.title === name
          }
        }
      )).map(item => {
        return {id: item.id, hidden: !item.hidden, physics: item.hidden}
      })
      this.edgeSet.update(updates)
    },

    showLoops: function (state) {
      let updates = Object.values(this.edgeSet.get({
        filter: function (item) {
          return item.from === item.to
        }
      })).map(item => {
        return {id: item.id, hidden: !state, physics: state}
      })
      this.edgeSet.update(updates)
    },

    showUnconnected: function (state) {
      this.toggleNodesVisible(this.unconnected, state)
    },

    showOnlyComponent: function (selectedId, state) {
      this.toggleNodesVisible(this.nodeSet.get({
        filter: function (item) {
          return item.id !== selectedId
        }
      }).map(item => item.id), !state)

      if (state) {
        let isolated = [selectedId]
        let neighbors = [selectedId]
        let change = 1
        while (change > 0) {
          let newneighbors = []
          neighbors.forEach(n => {
            this.getNeighbors(n).filter(newN => isolated.indexOf(newN) === -1).forEach(newN => {
              newneighbors.push(newN)
              isolated.push(newN)
            })
          })
          neighbors = newneighbors;
          change = neighbors.length;
        }
        this.toggleNodesVisible(isolated, true);
      }

    },
    toggleNodesVisible: function (list, state) {
      let updates = this.nodeSet.get({
        filter: function (item) {
          return list.indexOf(item.id) > -1
        }
      }).map(item => {
        return {id: item.id, hidden: !state, physics: state}
      })
      this.saveLayout()
      this.updateNodes(updates)
    },

    updateNodes: function (updates) {
      this.nodeSet.update(updates)
    },


    showAllNodes: function (boolean) {
      this.toggleNodesVisible(this.nodeSet.get().map(item => item.id), boolean)
    },
    hideGroupVisibility: function (name, boolean) {
      let updates = this.nodeSet.get({
        filter: function (item) {
          return item.group === name
        }
      }).map(item => {
        return {id: item.id, hidden: boolean, physics: !boolean}
      })
      this.updateNodes(updates)
    },

    // getVisualizedGraph: function () {
    //   return this.visualizeNow().then(resolve => {
    //     setTimeout(this.waitForVisualization, 50, resolve)
    //   })
    // },
    // waitForVisualization: function (resolve) {
    //   console.log(this.$refs.network)
    //   if (this.$refs.network !== undefined)
    //     resolve()
    //   else
    //     setTimeout(this.waitForVisualization, 500, resolve)
    // },

    visualizeNow: function () {
      if (this.configuration.sizeWarning && !this.dialog)
        this.dialog = true
      else {
        this.dialogResolve(true)
      }
    },
    onClick: function (params) {
      // this.setNodeSize(this.nodeSet.get().map(n=>n.id),25)
      if (params.nodes.length > 0 || params.edges.length > 0) {
        // this.$refs.network.enableEditMode()
        // if (params.nodes.length > 0)
        //   this.setNodeSize(params.nodes, 100)
        this.$emit('selectionEvent', params)
      } else {
        // this.$refs.network.disableEditMode()
        this.$emit('selectionEvent')
        this.highlight = false;
      }
      this.saveLayout()
    },
    // setNodeSize: function(item, size){
    //   let updates = []
    //   if (typeof item === "object") {
    //     updates = item.map(n=>{
    //       return {id:n, value:size}
    //     })
    //   } else {
    //     updates.push({id:item, value:size})
    //   }
    //   this.updateNodes(updates)
    //   this.$refs.network.redraw()
    // }
    // ,
    zoomToNode: function (nodeId) {
      if (nodeId !== undefined) {
        this.focusNode(nodeId)
        this.$refs.network.moveTo({scale: 0.9})
      }
    },
    focusNode: function (nodeId) {

      if (nodeId === undefined)
        this.viewAll()
      else
        this.$refs.network.focus(nodeId)
    },
    focusEdge: function (edgeId) {
      this.getVisualizedGraph().then(() => {
        console.log(this.edgeSet.get())
        if (edgeId === undefined)
          this.viewAll()
      })
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
    getNeighbors: function (selected) {
      let neighbors = []
      this.getConnectedNodes(selected).forEach(n => {
        if (n !== selected)
          neighbors.push(n)
      })
      return neighbors
    },
    identifyNeighbors: function (selected) {
      let neighbors = []
      this.getConnectedNodes(selected).forEach(n => {
        if (n !== selected)
          neighbors.push(this.nodeSet.get(n))
      })
      return {primary: this.nodeSet.get(selected), neighbors: neighbors}
    }
    ,
    getConnectedNodes: function (node) {
      return this.$refs.network.getConnectedNodes(node)
    }
    ,
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


  }
}
</script>

<style scoped lang="sass">
.graph-window
  border: 1px solid gray

.wrapper
  min-height: 100%

  background-color: #ffffff
  padding: 10px
  height: 100%


</style>
