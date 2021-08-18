<template>
  <div class="graph-window" :style="windowStyle">
    <v-progress-linear v-if="progress ===undefined" v-show="loading && !waiting" indeterminate
                       :color=loadingColor></v-progress-linear>
    <v-progress-linear v-else v-show="progress <100" :value="progress" :color=loadingColor></v-progress-linear>
    <div :style="{position:'relative', height:'100%',width:'100%',display: 'flex', justifyContent:'flex-end'}">
      <div style="justify-self: center; align-self: center; margin: auto" v-if="!configuration.visualized">
        <template v-if="nodeSet==null && (loading || waiting) && progressText !=null">
          <i>{{ progressText }}</i>
          <v-progress-circular color="primary" indeterminate size="40"></v-progress-circular>
        </template>
        <template v-if="nodeSet==null && loading && waiting && !show">
          <div v-if="secondaryViewer">
            <i>Generating Network...</i>
            <v-progress-circular color="primary" indeterminate size="40"></v-progress-circular>
          </div>
          <i v-else>No network has been selected yet!</i>
        </template>
        <div v-else-if="!show && !loading && !secondaryViewer">
          <v-card v-if="nodeSet !== undefined" :img="getThumbnail()" height="20vh">
            <v-card-title>
              <i>Create a View for the Network</i>
              <v-btn icon @click="visualize();">
                <v-icon>fas fa-play</v-icon>
              </v-btn>
            </v-card-title>
          </v-card>
          <div v-else>
            <i>Create a View for the Network</i>
            <v-btn icon @click="visualize();">
              <v-icon>fas fa-play</v-icon>
            </v-btn>
          </div>
        </div>
        <div v-show="show">
          <i>Generating Network Layout...</i>
          <v-progress-circular color="primary" indeterminate size="40"></v-progress-circular>
        </div>
      </div>
      <v-snackbar :value="selectMode" :timeout="-1" absolute top centered color="rgba(0,0,0,0.7)">Zooming and moving are
        disabled while selection mode is active!
      </v-snackbar>
      <VisNetwork v-if="nodeSet !== undefined && show" class="wrapper" ref="network"
                  :style="{width: '100%',cursor:canvasCursor, border: 'lightgray 1px solid'}"
                  :nodes="nodeSet"
                  :edges="edgeSet"
                  :options="options"
                  :layout="layout"
                  :physics="physics"
                  :events="['click','mousedown']"
                  @click="onClick"
                  v-on:mousedown.right="dragMouseDown"
      ></VisNetwork>
      <div style="position: absolute;"
           v-if=" (!waiting && show )|| keepLegends">
        <div v-if="legend">
          <v-btn @click="togglePanel(0)" :title="this.showPanels[0] ? 'Hide':'Show'" plain
                 style="display: flex; justify-content: flex-end; margin-left: auto; z-index: 201">
            <v-icon left>fas fa-scroll</v-icon>
            Legend
            <v-icon right>{{ this.showPanels[0] ? "fas fa-angle-up" : "fas fa-angle-down" }}</v-icon>
          </v-btn>
          <div v-show="this.showPanels[0]"
               style="margin-top: -35px; margin-right:1px; z-index: 600; display: flex; justify-content: flex-end;margin-left: auto">
            <slot name="legend"></slot>
          </div>
        </div>
        <template v-if="tools">
          <div style="display: flex; justify-content: flex-end;">
            <v-btn @click="togglePanel(1)" :title="this.showPanels[1] ? 'Hide':'Show'" plain style="z-index: 201">
              <v-icon left>fas fa-tools</v-icon>
              Tools
              <v-icon right>{{ this.showPanels[1] ? "fas fa-angle-up" : "fas fa-angle-down" }}</v-icon>
            </v-btn>
          </div>
          <div v-show="this.showPanels[1]" style="margin-top:-35px; margin-right:1px;  z-index: 200">
            <slot name="tools"></slot>
          </div>
        </template>
        <template v-if="styles">
          <div style="display: flex; justify-content: flex-end;">
            <v-btn @click="togglePanel(2)" :title="this.showPanels[2] ? 'Hide':'Show'" plain style="z-index: 201">
              <v-icon left>fas fa-palette</v-icon>
              Styling
              <v-icon right>{{ this.showPanels[2] ? "fas fa-angle-up" : "fas fa-angle-down" }}</v-icon>
            </v-btn>
          </div>
          <div v-show="this.showPanels[2]" style="margin-top:-35px; margin-right:1px;  z-index: 200">
            <slot name="styles"></slot>
          </div>
        </template>
      </div>
    </div>
    <template>
      <v-dialog
        v-if="configuration.sizeWarning && nodeSet !== undefined"
        v-model="sizeDialog"
        persistent
        max-width="350"
        style="z-index: 202"
      >
        <v-card>
          <v-card-title class="headline">
            Load Visualization?
          </v-card-title>
          <v-card-text>The selected network consists of {{ nodeSet.getIds().length }} nodes and
            {{ edgeSet.getIds().length }} edges. Visualization and especially physics simulation could take a long
            time
            and cause freezes. Do you want to visualize the network or skip it for now?
            <span v-if="secondaryViewer">There is also the option to directly load the results in the Advanced View, to use the subselection tools provided in the <i>List</i> tab for more filtering.</span>
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
            <v-btn v-if="secondaryViewer" color="green darken-1" text
                   @click="dialogResolve(false);$emit('loadIntoAdvancedEvent')" :disabled="disableAdvancedLoading">
              Advanced
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
import {Network} from "vue-vis-network";
import * as CONFIG from "@/Config";

export default {
  name: "Network",

  props: {
    legend: Boolean,
    tools: Boolean,
    styles: Boolean,
    configuration: Object,
    startGraph: false,
    progress: Number,
    secondaryViewer: false,
    windowStyle: {
      height: '75vh',
      'min-height': '75vh',
    }
  },

  extractedDefaults: false,
  colors: Object,
  groups: Object,

  data() {
    return {
      showPanels: [true, false, false],
      edgeSet: Object,
      nodeSet: undefined,
      options: Object,
      layout: Object,
      physics: Object,
      preventPhysics: false,
      loading: true,
      waiting: true,
      show: false,
      loadingColor: 'primary',
      canvasCursor: "default",
      sizeDialog: false,
      progressText: undefined,
      disableAdvancedLoading: false,
      clickParams: {t0: 0, threshold: 250},
      selectMode: false,
      keepLegends: false,
    }
  },

  created() {
    this.physics = false
    this.colors = {bar: {backend: "#6db33f", vis: 'primary', error: 'red darken-2'}}
    this.configuration.visualized = false
    this.init()
    if (this.startGraph)
      this.initStartGraph()
  },

  methods: {

    init: function () {
      this.extractDefaults()
    },

    initStartGraph: function () {
      this.setGraph(this.$global.metagraph)
      this.visualize()
    },

    reset: function () {
      this.show = false;
      this.setLoading(true)
      this.setWaiting(true)
      this.setVisualized(false)
      this.toggleSelectMode(false)
      this.setPhysics(false)
      this.nodeSet = undefined
      this.edgeSet = undefined
    },

    reload: function () {
      this.reset();
      this.readGIDfromRoute()
      if (this.gid !== undefined)
        this.init()
    },

    extractDefaults: function () {
      if (!this.extractedDefaults) {
        let defaults = this.$utils.clone(this.$global.metagraph.options);
        this.options = defaults.options;
        if (this.startGraph) {
          this.options['interaction']['hideEdgesOnDrag'] = false;
          this.options['interaction']['hideEdgesOnZoom'] = false;
        }
        this.layout = defaults.layout;
        this.physics = defaults.physics;
        this.extractedDefaults = true;
        this.groups = this.$utils.clone(this.options.groups)
        this.reloadOptions()
      }
    },

    reloadOptions: function () {
      if (this.isVisualized())
        this.$refs.network.setOptions(this.options)
    },

    setVisualized: function (bool) {
      this.configuration.visualized = bool;
    },

    isVisualized: function () {
      return this.$refs.network != null && this.configuration.visualized && this.show
    },

    setLoading: function (bool) {
      this.loading = bool;
    },

    loadNetworkById: function (gid, disableAdvancedLoading) {
      this.disableAdvancedLoading = !!disableAdvancedLoading
      if (gid === this.gid)
        return
      this.reset()
      this.setLoading(false)
      this.setWaiting(true)
      return this.$http.getGraph(gid).then(this.setGraph).catch(err => {
        this.loadingColor = this.colors.bar.error;
        console.error(err)
      })
    },
    setDirected: function (bool) {
      if (bool != null)
        this.directed = bool;
    },
    setEdges: function (edges) {
      if (edges != null)
        this.edgeSet = new DataSet(edges);
    },
    setNodes: function (nodes) {
      if (nodes != null) {
        for (let i = 0; i < nodes.length; i++) {
          nodes[i].title = nodes[i].label
        }
        this.nodeSet = new DataSet(nodes);
      }
    },
    setOptions: function (options) {
      if (options != null) {
        this.options = options;
        this.reloadOptions()
      }
    },

    setWaiting: function (bool) {
      this.waiting = bool;
    },

    setGraph: function (graph) {
      this.setWaiting(false)
      this.loadingColor = this.colors.bar.vis
      this.setVisualized(false)
      if (graph != null) {
        this.setDirected(graph.directed)
        this.setEdges(graph.edges);
        this.setNodes(graph.nodes);
        this.setOptions(this.startGraph ? null : graph.options)
        this.checkSizeWarning(graph)
      }
      this.setLoading(false)
      if (this.show || this.secondaryViewer)
        this.visualize()
      else
        this.$emit("disableLoadingEvent")
    },
    checkSizeWarning: function () {
      let sum = (this.nodeSet != null ? this.nodeSet.length : 0) + (this.edgeSet != null ? this.edgeSet.length : 0);
      this.$emit("disablePhysicsEvent", sum > 20000)
      this.$set(this.configuration, "sizeWarning", (this.nodeSet !== undefined && this.nodeSet.length > 1000) || (this.edgeSet !== undefined && this.edgeSet.length > 1000))
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

    prepare: function () {
      if (this.startGraph || this.nodeSet === undefined)
        return
      this.prepareUnconnectedList()
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

    loadNewNetwork: function (graph) {
      this.$http.isReady().then(state => {
        if (state)
          this.printNotification("Request successfully sent!", 1)
        else
          this.printNotification("Request scheduled: Server is currently busy! please wait.", 2)
      })
      this.setLoading(false)
      this.setWaiting(true)
      this.$http.requestGraph(graph).then(response => {
        return response.data
      }).then(info => {
        this.evalInfo(info)
      }).catch(err => {
        console.error(err)
      })
    },

    printNotification: function (message, style) {
      this.$emit("printNotificationEvent", message, style)
    },

    evalInfo: function (info) {
      let sum = 0
      for (let n in info.nodes)
        sum += info.nodes[n];
      if (sum === 0) {
        this.printNotification("Resulting graph contains no node!", 2)
        return
      }
      for (let e in info.edges)
        sum += info.edges[e]
      if (sum >= 50000 && !this.$cookies.get("override-limit")) {
        this.$emit("sizeWarnEvent", info)
      } else {
        this.$http.saveGraph(info.id, this.$cookies.get("uid")).then(() => {
          this.$emit("setGlobalGidEvent", info.id)
          this.loadNetworkById(info.id)
        }).catch(err => console.error(err))
      }
    }
    ,
    visualize: function () {
      if (this.configuration.sizeWarning)
        this.sizeDialog = true
      else {
        this.prepare()
        this.show = true
        if (this.nodeSet != null) {
          this.showLoops(false)
          this.setVisualized(true)
          this.$emit("visualizationEvent")
        }
      }
    },
    readGIDfromRoute: function () {
      return this.$route.params["gid"]
    }
    ,
    togglePanel: function (index) {
      let state = !this.showPanels[index]
      if (state) {
        for (let idx = 0; idx < this.showPanels.length; idx++) {
          this.$set(this.showPanels, idx, false)
        }
      }
      this.$set(this.showPanels, index, state)
    },

    modifyGroups: function (nodeIds, group) {
      let updates = this.nodeSet.get().filter(n => nodeIds.indexOf(n.id) > -1).map(n => {
        return {id: n.id, group: group}
      })
      this.nodeSet.update(updates)
    },
    setPhysics: function (bool) {
      this.saveLayout()
      this.$set(this.options.physics, 'enabled', bool)
      this.reloadOptions()
      return bool
    },

    showUnconnected: function (state) {
      this.toggleNodesVisible(this.unconnected, state)
    },

    hasPhysicsEnabled: function () {
      return this.options.physics.enabled;
    },

    onDoubleClick: function (params) {
      if (params == null)
        return
      if (params.nodes.length > 0) {
        this.$emit("toggleNodeSelectEvent", params.nodes.map(id => this.$refs.network.getNode(id)).map(n => {
          return {id: parseInt(n.id.substring(4)), group: n.group}
        }))
      } else if (params.edges.length > 0) {
        this.$emit("toggleedgeSelectEvent", params.edges.map(id => this.$refs.network.getEdge(id)).map(e => {
          return {from: e.from, to: e.to, group: e.title}
        }))
      }
    },

    onClick: function (params) {
      let t = new Date().getTime()
      if ((t - this.clickParams.t0) < this.clickParams.threshold) {
        this.onDoubleClick(params)
      }
      this.clickParams.t0 = t;
      let selection = params.nodes.length > 0 || params.edges.length > 0;
      if (selection) {
        if (params.edges.length > 0)
          params.edges = params.edges.map(id => this.$refs.network.getEdge(id))
        this.$emit('selectionEvent', params)
      } else {
        this.$emit('selectionEvent')
        this.highlight = false;
      }
      this.saveLayout()
    }
    ,

    setShadow: function (state) {
      this.saveLayout()
      this.options.nodes.shadow.enabled = state;
      this.reloadOptions()
    },

    saveLayout: function () {
      try {
        if (this.$refs.network == null)
          return
        let updates = Object.entries(this.$refs.network.getPositions()).map(e => {
          return {id: e[0], x: e[1].x, y: e[1].y}
        })
        this.updateNodes(updates)
      } catch (e) {
        console.warn("Error on saving the layout. Network may not have been visualized yet!")
      }
    },
    showAllNodes: function (boolean) {
      this.toggleNodesVisible(this.nodeSet.get().map(item => item.id), boolean)
    },
    hideAllEdges: function (boolean) {
      let updates = Object.values(this.edgeSet.get()).map(item => {
        return {id: item.id, hidden: boolean, physics: boolean}
      })
      if (updates)
        this.edgeSet.update(updates)
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
    getNeighbors: function (selected) {
      let neighbors = []
      this.getConnectedNodes(selected).forEach(n => {
        if (n !== selected)
          neighbors.push(n)
      })
      return neighbors
    },
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
    getThumbnail: function () {
      return CONFIG.HOST_URL + CONFIG.CONTEXT_PATH + "/api/getThumbnailPath?gid=" + this.readGIDfromRoute()
    },

    getAllNodes: function () {
      let nodes = []
      this.nodeSet.getDataSet().forEach(n => nodes.push(n))
      return {neighbors: nodes}
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

    setEdgeVisible: function (name, state, loops) {
      let updates = Object.values(this.edgeSet.get({
          filter: function (item) {
            return item.title === name
          }
        }
      )).map(item => {
        return {id: item.id, hidden: !state, physics: state}
      })
      this.edgeSet.update(updates)
      if (state && !loops) {
        this.showLoops(false)
      }
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
          this.setEdgeVisible(params.name, !params.state, params.loops)
      }
      if (data.event === "isolate") {
        this.showOnlyComponent(data.selected, data.state)
      }
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

    updateNodes: function (updates) {
      this.nodeSet.update(updates)
    },
    getGroupNodes: function (groupName) {
      return this.nodeSet.get({
        filter: function (item) {
          return item.group === groupName
        }
      })
    },

    switchNodeStyle: async function (style) {
      let physicsEnabled = this.hasPhysicsEnabled()
      if (physicsEnabled)
        await this.setPhysics(false)
      if (style === 'shapes') {
        Object.keys(this.options.groups).forEach(g => this.options.groups[g].shape = this.groups[g].shape)
      } else {
        Object.values(this.options.groups).forEach(g => g.shape = style)
      }
      this.reloadOptions();
      this.redraw(physicsEnabled, true);
    },

    redraw: function (enablePhysics, keepLegends) {
      if (this.isVisualized()) {
        this.keepLegends = keepLegends
        this.$nextTick().then(() => {
          this.show = false
        }).then(() => this.show = true).then(() => this.keepLegends = false)
      }
    },

    // execWhenVisualized: function (callback) {
    //   if (!this.isVisualized)
    //     setTimeout(this.execWhenVisualized, 500, callback)
    //   else
    //     setTimeout(callback(),50)
    // },

    getGroupEdges: function (groupName) {
      return this.edgeSet.get({
        filter: function (item) {
          return item.title === groupName
        }
      })
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
        this.reloadOptions()
        this.updateNodes(updates)
        this.$emit("printNotificationEvent", request.ids.length + " nodes recolored!", 1)
      }
    },

    setProgressText: function (text) {
      this.progressText = text
    },


    /* Rectengular overlay selection methods */

    toggleSelectMode: function (select) {
      this.selectMode = select;
      if (select) {
        this.options.interaction.zoomView = false;
        this.options.interaction.dragView = false;
        this.initDragSelect();
      } else {
        this.options.interaction.zoomView = true;
        this.options.interaction.dragView = true;
        this.removeDragSelect();
      }
      this.reloadOptions()
    }
    ,

    selectNodesFromHighlight: function () {
      let fromX, toX, fromY, toY;
      let nodesIdInDrawing = [];
      let xRange = this.getStartToEnd(this.rect.startX, this.rect.w);
      let yRange = this.getStartToEnd(this.rect.startY, this.rect.h);

      let allNodes = this.nodeSet.get();
      let selection = []
      for (let i = 0; i < allNodes.length; i++) {
        let curNode = allNodes[i];
        let nodePosition = this.net.getPositions([curNode.id]);
        let nodeXY = this.net.canvasToDOM({x: nodePosition[curNode.id].x, y: nodePosition[curNode.id].y});
        if (xRange.start <= nodeXY.x && nodeXY.x <= xRange.end && yRange.start <= nodeXY.y && nodeXY.y <= yRange.end) {
          nodesIdInDrawing.push(curNode.id);
          selection.push({id: curNode.id, label: curNode.label})
        }
      }
      this.net.selectNodes(nodesIdInDrawing);
      this.$emit("multiSelectionEvent", selection)
    },

    getStartToEnd: function (start, theLen) {
      return theLen > 0 ? {start: start, end: start + theLen} : {start: start + theLen, end: start};
    },

    initDragSelect: function () {
      if (this.$refs.network !== undefined && this.net === undefined) {
        this.net = this.$refs.network.network
        this.canvas = this.net.canvas.frame.canvas;
        this.canvas.oncontextmenu = function () {
          return false
        }
        this.ctx = this.canvas.getContext('2d');
        this.canvas.addEventListener("mousemove", this.dragMouseMove)
        this.canvas.addEventListener("mousedown", this.dragMouseDown)
        this.canvas.addEventListener("mouseup", this.dragMouseUp)
      }
    }
    ,

    removeDragSelect: function () {
      this.net = undefined;
      if (this.canvas) {
        this.canvas.removeEventListener("mousemove", this.dragMouseMove)
        this.canvas.removeEventListener("mousedown", this.dragMouseDown)
        this.canvas.removeEventListener("mouseup", this.dragMouseUp)
      }
    },

    graphExists: function () {
      return this.$refs.network !== undefined
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
    }
    ,

    dragMouseUp: function (e) {
      if (e.button === 0) {
        this.restoreDrawingSurface();
        this.drag = false;

        this.canvasCursor = "default";
        this.selectNodesFromHighlight();
      }
    }
    ,

    saveDrawingSurface: function () {
      this.drawingSurfaceImageData = this.ctx.getImageData(0, 0, this.canvas.width, this.canvas.height);
    }
    ,

    restoreDrawingSurface: function () {
      this.ctx.putImageData(this.drawingSurfaceImageData, 0, 0);
    }
    ,

    dialogResolve: function (vis) {
      this.sizeDialog = false;
      if (this.secondaryViewer && !this.disableAdvancedLoading && !vis) {
        this.keepLegends = true;
        this.togglePanel(1);
      }
      this.configuration.sizeWarning = !vis;
      if (vis) {
        this.visualize()
      }
    }
  },
  components: {
    'VisNetwork': Network
  }
}
</script>

<style scoped>

</style>
