<template>
  <div class="graph-window" :style="windowStyle">
    <i>{{ text }}</i>
    <v-progress-linear v-if="progress ===undefined" v-show="loading && !waiting" indeterminate
                       :color=loadingColor></v-progress-linear>
    <v-progress-linear v-else v-show="progress <100" :value="progress" :color=loadingColor></v-progress-linear>
    <i v-show="loading && waiting && progress ===undefined">No graph has been loaded yet!</i>
    <div :style="{position:'relative', height:'100%',width:'100%',display: 'flex', 'justify-content': 'flex-end'}">
      <network v-if="nodeSet !== undefined && isVisualized()" v-show="!loading" class="wrapper" ref="network"
               :style="{width: '100%',cursor:canvasCursor}"
               :key="key"
               :nodes="nodeSet"
               :edges="edgeSet"
               :options="options"
               :layout="layout"
               :physics="physics"
               :events="['click','release','startStabilizing','stabilizationProgress','stabilizationIterationsDone','mousemove','mousedown','mouseup']"
               @click="onClick"
               v-on:mousedown.right="dragMouseDown"

      >
      </network>
      <div style="position: absolute" v-if="legend ">
        <div style="display: flex; justify-content: flex-end;">
          <v-btn @click="showLegend= !showLegend" :title="showLegend ? 'Hide':'Show'" :plain="!showLegend">
            Legend
            <v-icon right>{{ showLegend ? "fas fa-angle-up" : "fas fa-angle-down" }}</v-icon>
          </v-btn>
        </div>
        <div v-show="showLegend" style="padding-right: 2px; padding-top:2px">
          <slot name="legend"></slot>
        </div>
      </div>
    </div>
    <template>
      <v-dialog
        v-if="configuration.sizeWarning && nodeSet !== undefined"
        v-model="dialog"
        persistent
        max-width="290"
        style="z-index: 1000"
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
    legend: Boolean,
    initgraph: Object,
    payload: Object,
    configuration: Object,
    startGraph: false,
    progress: Number,
    meta: Object,
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
      showLegend: true,
      edgeSet: Object,
      nodeSet: undefined,
      options: Object,
      layout: Object,
      physics: Object,
      key: 0,
      text: "",
      loading: true,
      waiting:true,
      loadingColor: 'primary',
      dialog: false,
      skipVis: false,
      skipDialog: false,

      network: undefined,
      canvas: undefined,
      ctx: undefined,
      rect: {},
      drag: false,
      drawingSurfaceImageData: undefined,
      offsetLeft: 0,
      offsetTop: 0,
      canvasCursor: "default",
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
  watch: {
    meta: function () {
      this.setMetagraph(this.meta)
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
      if((this.metagraph ==null || this.options ==null) && this.meta!=null)
        this.setMetagraph(this.meta)
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
      this.waiting=false
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
      if (this.metagraph == null || this.options == null) {
        if (this.metagraph)
          this.setMetagraph(this.metagraph)
        setTimeout(this.drawGraph, 50)
        return
      }
      if (this.nodeSet === undefined || this.edgeSet === undefined)
        return
      if (this.directed) {
        this.options.edges.arrows = {to: {enabled: true}}
      } else
        this.options.edges.arrows = {};
      this.key += 1
      this.nodes = this.nodeSet.get({returnType: "Object"})
      // this.setNodeColors()
      this.loading = false;
      this.prepare()
      this.$emit('finishedEvent')
    },
    setMetagraph: function (metagraph) {
      this.metagraph = metagraph;
      let defaults = metagraph.options;
      this.options = defaults.options;
      this.layout = defaults.layout;
      this.physics = defaults.physics;
      this.updateOptions()
    },
    loadData: function (payload) {
      this.configuration.visualized = true
      this.loading = true
      if (payload !== undefined && payload.skipVis !== undefined)
        this.skipVis = payload.skipVis

      if (payload !== undefined && payload.name !== undefined && payload.name === "metagraph" && this.metagraph === undefined) {
        this.setMetagraph(payload.graph);
        this.edgeSet = new DataSet(this.metagraph.edges);
        this.nodeSet = new DataSet(this.metagraph.nodes);
        this.directed = this.metagraph.directed;
      }
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
    // loadMetaColors: function (fromDefaults) {
    //   if(!this.metagraph || !this.metagraph.colorMap)
    //     return
    //   if (!fromDefaults) {
    //     this.options = this.metagraph.options
    //   }
    //   Object.keys(this.options.groups).forEach(group => {
    //     if (group.endsWith("other"))
    //       return
    //     if (group.endsWith("Module")) {
    //       let restgroup = group.replace("Module", "")
    //       this.options.groups[group].color.border = this.metagraph.colorMap[restgroup].light;
    //       this.options.groups[group].color.background = this.metagraph.colorMap[restgroup].light;
    //       this.options.groups[group].color.highlight.border = this.metagraph.colorMap[restgroup].light;
    //       this.options.groups[group].color.highlight.background = this.metagraph.colorMap[restgroup].light;
    //     } else {
    //       this.options.groups[group].color.border = this.metagraph.colorMap[group].light;
    //       this.options.groups[group].color.background = this.metagraph.colorMap[group].main;
    //       this.options.groups[group].color.highlight.border = this.metagraph.colorMap[group].light;
    //       this.options.groups[group].color.highlight.background = this.metagraph.colorMap[group].main;
    //     }
    //   })
    //   this.updateOptions()
    // },


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


    // getDefaults: function () {
    //   return this.metagraph.options
    //   // }).catch(err => {
    //   //   this.loadingColor = this.colors.bar.error;
    //   //   console.log(err)
    //   // })
    // }
    // ,
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
      // this.physicsOn=false
      this.options.physics.enabled = this.physicsOn
      if (this.isVisualized()) {
        this.saveLayout()
      }
      this.updateOptions()
    },

    toggleSelectMode: function (select) {
      if (select) {
        this.options.interaction.zoomView = false;
        this.options.interaction.dragView = false;
        this.initDragSelect();
      } else {
        this.options.interaction.zoomView = true;
        this.options.interaction.dragView = true;
        this.removeDragSelect();
      }
      this.updateOptions()
    },

    saveLayout: function () {
      let updates = Object.entries(this.$refs.network.getPositions()).map(e => {
        return {id: e[0], x: e[1].x, y: e[1].y}
      })
      this.updateNodes(updates)
    },
    recolorGraph: function (request) {
      let group = {
        color: {
          border: request.color,
          background: request.color,
          highlight: {border: request.color, background: request.color}
        }
      }
      let groupLower = request.name.toLowerCase()
      this.options.groups[groupLower] = group;
      let updates = request.ids
      updates.forEach(n => n.group = groupLower)
      if (this.$refs.network === undefined)
        this.$emit("printNotificationEvent", "Graph was not ready", 2)
      else {
        this.updateOptions()
        this.updateNodes(updates)
        this.$emit("printNotificationEvent", request.ids.length + " nodes recolored!", 1)
      }
    },
    updateOptions: function () {
      if (this.$refs.network)
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
      if (this.configuration.sizeWarning && !this.dialog && this.$refs.network === undefined)
        this.dialog = true
      else {
        this.dialogResolve(true)
        this.reloadLegend()
      }
    },
    reloadLegend: function(){
      this.showLegend=!this.showLegend
      this.showLegend=!this.showLegend
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

    initDragSelect: function () {
      if (this.$refs.network !== undefined && this.network === undefined) {
        this.network = this.$refs.network.network
        this.canvas = this.network.canvas.frame.canvas;
        this.canvas.oncontextmenu = function () {
          return false
        }
        this.ctx = this.canvas.getContext('2d');
        this.canvas.addEventListener("mousemove", this.dragMouseMove)
        this.canvas.addEventListener("mousedown", this.dragMouseDown)
        this.canvas.addEventListener("mouseup", this.dragMouseUp)
      }
    },

    removeDragSelect: function () {
      this.network = undefined;
      this.canvas.removeEventListener("mousemove", this.dragMouseMove)
      this.canvas.removeEventListener("mousedown", this.dragMouseDown)
      this.canvas.removeEventListener("mouseup", this.dragMouseUp)
    },

    dragMouseMove: function (e, drag, rect, ctx) {
      if (this.drag) {
        this.restoreDrawingSurface();
        this.rect.w = (e.pageX - this.offsetLeft) - this.rect.startX;
        this.rect.h = (e.pageY - this.offsetTop) - this.rect.startY;

        this.ctx.setLineDash([5]);
        this.ctx.strokeStyle = "rgb(0, 102, 0)";
        this.ctx.strokeRect(this.rect.startX, this.rect.startY, this.rect.w, this.rect.h);
        this.ctx.setLineDash([]);
        this.ctx.fillStyle = "rgba(0, 255, 0, 0.2)";
        this.ctx.fillRect(this.rect.startX, this.rect.startY, this.rect.w, this.rect.h);
      }
    },
    dragMouseDown: function (e) {
      if (e.button === 0) {
        this.offsetLeft = e.target.getBoundingClientRect().left
        this.offsetTop = e.target.getBoundingClientRect().top
        // this.selectedNodes = e.ctrlKey ? this.network.getSelectedNodes() : null;
        this.saveDrawingSurface();
        this.rect = {}
        // let that = this;
        this.rect.startX = e.pageX - this.offsetLeft;
        this.rect.startY = e.pageY - this.offsetTop;
        this.drag = true;
        this.canvasCursor = "crosshair";
      }
    },

    dragMouseUp: function (e) {
      if (e.button === 0) {
        this.restoreDrawingSurface();
        this.drag = false;

        this.canvasCursor = "default";
        this.selectNodesFromHighlight();
      }
    },

    saveDrawingSurface: function () {
      this.drawingSurfaceImageData = this.ctx.getImageData(0, 0, this.canvas.width, this.canvas.height);
    },

    restoreDrawingSurface: function () {
      this.ctx.putImageData(this.drawingSurfaceImageData, 0, 0);
    },

    selectNodesFromHighlight: function () {
      let fromX, toX, fromY, toY;
      let nodesIdInDrawing = [];
      let xRange = this.getStartToEnd(this.rect.startX, this.rect.w);
      let yRange = this.getStartToEnd(this.rect.startY, this.rect.h);

      let allNodes = this.nodeSet.get();
      let selection = []
      for (let i = 0; i < allNodes.length; i++) {
        let curNode = allNodes[i];
        let nodePosition = this.network.getPositions([curNode.id]);
        let nodeXY = this.network.canvasToDOM({x: nodePosition[curNode.id].x, y: nodePosition[curNode.id].y});
        if (xRange.start <= nodeXY.x && nodeXY.x <= xRange.end && yRange.start <= nodeXY.y && nodeXY.y <= yRange.end) {
          nodesIdInDrawing.push(curNode.id);
          selection.push({id: curNode.id, label: curNode.label})
        }
      }
      this.network.selectNodes(nodesIdInDrawing);
      this.$emit("multiSelectionEvent", selection)
    },

    getStartToEnd: function (start, theLen) {
      return theLen > 0 ? {start: start, end: start + theLen} : {start: start + theLen, end: start};
    }


  }
}
</script>

<style scoped lang="sass">
.graph-window
  border: 1px solid gray

.wrapper
  min-height: 100%

  background-color: #ffffff
  padding: 0px
  height: 100%


</style>
