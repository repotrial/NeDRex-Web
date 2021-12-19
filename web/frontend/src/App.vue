<template style="overflow-y: hidden">
  <v-app :style="{marginTop:selectedTabId===0 ? '60px': '0px'}" id="app">
    <headerBar @showVersionEvent="showVersionInfo=true" @showBugEvent="showBugInfo=true"
               @showHelpEvent="showHelp=true"
               @showCompatability="showCompatability=true"
               @redirectEvent="redirect"
               :prominent="selectedTabId===0" style="z-index: 1000;"/>
    <v-card style="position: sticky ; top: 0; margin-top: -10px; z-index: 999 ">
      <v-toolbar flat :color="colors.main.bg1">
        <template v-slot:extension>
          <v-tabs
            fixed-tabs
            v-model="selectedTabId"
            :color="colors.main.primary"
          >
            <v-tabs-slider></v-tabs-slider>
            <v-tab v-for="tab in tabslist" class="primary--text" v-on:click="selectTab(tab.id)" :key=tab.id>
              <v-badge
                dot
                :color="colors.main.primary"
                :value="tab.note"
              >
                <i :style="{color:tab.color}">
                  <v-icon dense :color="tab.color">{{ tab.icon }}</v-icon>
                  {{ tab.label }}
                </i>
              </v-badge>
            </v-tab>
          </v-tabs>
        </template>
      </v-toolbar>
    </v-card>
    <v-container ref="mainContainer" align-self="start" v-if="metaLoaded"
                 style="width: 100%; max-width:95vw; margin-left: 1%; margin-right: 1%" @mouseup="resizeUp">
      <v-row>
        <v-col v-resize="setMainWidth"
               :style="{width: (mainWidth - (sideHidden ? 0 :sideCardWidth))+'px', minWidth: (mainWidth  - (sideHidden? 0 :sideCardWidth))+'px'}">
          <v-main app style="padding-top: 0">
            <v-container fluid style="margin-right:0; padding-right: 0">

              <Start v-if="selectedTabId===0" ref="start"
                     v-on:graphLoadEvent="loadGraph"
                     @graphLoadNewTabEvent="loadGraphNewTab"
                     v-on:printNotificationEvent="printNotification"
                     @showSideEvent="setSideVisible"
                     @clearURLEvent="clearURL"
                     @modifyURLEvent="modifyURL"
                     @newGraphEvent="reloadHistory()"
                     :colors="colors" :options="options.start" :filters="startFilters" :jid="jid"></Start>
              <Network
                v-show="selectedTabId===1"
                ref="graph"
                :configuration="options.graph"
                :window-style="graphWindowStyle"
                :legend="showLegend"
                :tools="showLegend"
                :styles="showLegend"
                style="position: sticky; "
                @finishedEvent="setTabNotification(1)"
                @multiSelectionEvent="setMultiSelection"
                @printNotificationEvent="printNotification"
                @selectionEvent="loadSelection"
                @sizeWarnEvent="sizeWarning"
                @setGlobalGidEvent="setGlobalGid"
                @visualizationEvent="visualizationEvent"
                @disableLoadingEvent="disableLoadingEvent(1)"
                @disablePhysicsEvent="disablePhysics"
                @toggleNodeSelectEvent="toggleNodeSelection"
                @toggleedgeSelectEvent="toggleEdgeSelection"
              >
                <template v-slot:legend>
                  <Legend v-if="showLegend" :countMap="options.list.countMap" ref="legend"
                          :entityGraph="options.list.entityGraph"
                          :options="options.graph.legend"
                          @graphViewEvent="graphViewEvent"
                          @downloadEntries="downloadEntries"
                  ></Legend>

                </template>
                <template v-slot:tools>
                  <Tools v-if="showLegend" :physics="true" :physicsDisabled="physicsDisabled" ref="tools"
                         @toggleOptionEvent="toggleToolOption" @clickOptionEvent="clickToolOption"></Tools>
                </template>
                <template v-slot:styles>
                  <VisualizationOptions v-if="showLegend" ref="styles"
                                        @toggleOptionEvent="toggleToolOption" @clickOptionEvent="clickToolOption"
                                        @switchOptionEvent="switchToolOption"></VisualizationOptions>
                </template>
              </Network>

              <List ref="list"
                    v-show="selectedTabId===2"
                    v-on:finishedEvent="setTabNotification(2)"
                    v-on:selectionEvent="loadDetails"
                    v-on:loadSelectionEvent="loadSubSelection"
                    v-on:updateInfo="evalPostInfo"
                    v-on:printNotificationEvent="printNotification"
                    v-on:reloadSide="reloadSide"
                    v-on:addJobEvent="registerJob"
                    v-on:focusInGraphEvent="focusInGraph"
                    @newJobEvent="addListJob"
                    @loadLegendEvent="loadLegendEvent"
                    @recolorGraphEvent="recolorGraph"
                    :configuration="options.list"
              ></List>
            </v-container>
            <v-container v-show="selectedTabId===3" fluid style="margin-right:0; padding-right: 0">
              <History ref="history"
                       :width="mainWidth-sideCardWidth"
                       v-on:graphLoadEvent="loadGraph"
                       v-on:printNotificationEvent="printNotification"
                       v-on:reloadEvent=""
                       :options="options.history"
              ></History>
            </v-container>
            <v-snackbar v-model="notifications.style1.show" :multi-line="true" :timeout="notifications.style1.timeout"
                        color="green" dark>
              {{ notifications.style1.message }}
            </v-snackbar>
            <v-snackbar v-model="notifications.style2.show" :multi-line="true" :timeout="notifications.style2.timeout"
                        color="red">
              {{ notifications.style2.message }}
            </v-snackbar>
            <v-dialog
              v-model="listDialog"
              v-if="listWarnObject !== undefined"
              persistent
              max-width="500"
              style="z-index: 1001"
            >
              <v-card>
                <v-card-title class="headline">
                  List loading warning!
                </v-card-title>
                <v-card-text>The selected network consists of the following number of elements:
                </v-card-text>
                <v-divider></v-divider>
                <v-card-subtitle style="font-size: 15pt; margin-top: 10px;">Nodes:</v-card-subtitle>
                <v-list class="transparent">
                  <v-list-item
                    v-for="item in Object.keys(listWarnObject.nodes)"
                    :key="item"
                  >
                    <v-list-item-title>{{ item.toLocaleUpperCase() }}
                    </v-list-item-title>
                    <v-list-item-subtitle>
                      {{ listWarnObject.nodes[item] }}
                    </v-list-item-subtitle>

                  </v-list-item>
                </v-list>
                <v-divider></v-divider>
                <v-card-subtitle style="font-size: 15pt; margin-top: 10px;">Edges:</v-card-subtitle>
                <v-list class="transparent">
                  <v-list-item
                    v-for="item in Object.keys(listWarnObject.edges)"
                    :key="item"
                  >
                    <v-list-item-title>{{ item }}
                    </v-list-item-title>
                    <v-list-item-subtitle>
                      {{ listWarnObject.edges[item] }}
                    </v-list-item-subtitle>

                  </v-list-item>
                </v-list>
                <v-divider></v-divider>
                <v-card-text>
                  This exceeds the limit of elements that can handled by the browser, so please apply a more strict
                  filter! You can still save the Network in graphml format and analyze it yourself.
                </v-card-text>
                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn color="green darken-1" text @click="resolveWarning(listWarnObject.id,true)">
                    Download
                  </v-btn>
                  <v-btn
                    color="green darken-1"
                    text
                    @click="resolveWarning(listWarnObject.id, false)"
                  >
                    Dismiss
                  </v-btn>

                </v-card-actions>
              </v-card>
            </v-dialog>
          </v-main>
        </v-col>
        <v-col v-show="!sideHidden"
               :style="{marginLeft:'-40px', marginRight: 0, width: sideCardWidth, minWidth: sideCardWidth}">
          <div :style="{position: 'fixed', top:'50vh',right:sideCardWidth-25+'px', zIndex:1000}">
            <v-icon style="cursor:col-resize; z-index: 1000" @mousedown="resizeDown">fas
              fa-grip-lines-vertical
            </v-icon>
          </div>
          <SideCard ref="side"
                    v-on:printNotificationEvent="printNotification"
                    v-on:nodeSelectionEvent="setSelectedNode"
                    v-on:updatePhysicsEvent="updatePhysics"
                    v-on:nodeDetailsEvent="nodeDetails"
                    v-on:applyEvent="applyEvent"
                    v-on:reloadTablesEvent="reloadTables"
                    v-on:graphModificationEvent="listModification"
                    v-on:historyReloadEvent="historyReloadEvent"
                    v-on:reverseSortingEvent="reverseHistorySorting"
                    v-on:selectionEvent="listSelectionEvent"
                    v-on:openAlgorithmDialogEvent="openAlgorithmDialogEvent"
                    v-on:graphLoadEvent="loadGraph"
                    v-on:reloadHistoryEvent="reloadHistory"
                    @selectModeEvent="toggleGraphSelectMode"
                    @showLoopsEvent="showLoops"
                    @showUnconnectedEvent="showUnconnected"
                    @graphViewEvent="graphViewEvent"
                    @applyMultiSelect="applyMultiSelect"
                    @colorSelectionEvent="selectionColorSelect"
                    :options="options"
                    :selected-tab="selectedTabId"
                    :filters="startFilters"
                    :side-width="sideCardWidth"
          ></SideCard>
        </v-col>
      </v-row>
    </v-container>
    <LegalDialog ref="tos" v-model="cookiesPopup" @initUserEvent="initUser()"></LegalDialog>
    <v-bottom-sheet inset v-model="showBugInfo" width="30vw" :overlay-color="colors.main.bg1" style="z-index: 1001">
      <BugSheet :color="colors.main.bg1"></BugSheet>
    </v-bottom-sheet>
    <v-bottom-sheet inset v-model="showHelp" width="35vw" :overlay-color="colors.main.bg1" style="z-index: 1001">
      <HelpSheet :color="colors.main.bg1"></HelpSheet>
    </v-bottom-sheet>
    <v-bottom-sheet inset v-model="showVersionInfo" width="60vw" :overlay-color="colors.main.bg1"
                    style="z-index: 1001">
      <VersionSheet :color="colors.main.bg1" @showTOSEvent="showTos(true)"></VersionSheet>
    </v-bottom-sheet>
    <v-bottom-sheet inset v-model="showCompatability" width="60vw" :overlay-color="colors.main.bg1"
                    style="z-index:1001">
      <CompatabilitySheet :color="colors.main.bg1"></CompatabilitySheet>
    </v-bottom-sheet>
    <Footer :color="colors.main.bg1" style="z-index: 1000;"></Footer>
  </v-app>
</template>

<script>
import Start from './components/views/Start.vue';
import History from "./components/views/History";
import List from './components/views/List.vue'
import SideCard from './components/side/SideCard.vue'
import headerBar from './components/app/Header.vue'
import Legend from "./components/views/graph/Legend";
import Footer from "@/components/app/Footer";
import BugSheet from "@/components/app/sheets/BugSheet";
import HelpSheet from "@/components/app/sheets/HelpSheet";
import VersionSheet from "@/components/app/sheets/VersionSheet";
import Network from "@/components/views/graph/Network";
import VisualizationOptions from "@/components/views/graph/VisualizationOptions";
import * as CONFIG from "@/Config";
import Tools from "@/components/views/graph/Tools";
import CompatabilitySheet from "@/components/app/sheets/CompatabilitySheet";
import LegalDialog from "@/components/app/dialogs/LegalDialog";

export default {
  name: 'app',
  exampleGraph: undefined,
  gid: undefined,
  tab: undefined,


  data() {
    return {
      // graphLoad: {},
      // graphKey: 0,
      neighborNodes: [],
      selectedNode: undefined,
      tabslist: {},
      selectedTabId: 0,
      colors: {},
      listWarnObject: undefined,
      listDialog: false,
      metaLoaded: false,
      notifications: {
        style1: {show: false, message: "", timeout: 2000},
        style2: {show: false, message: "", timeout: 4000}
      },
      options: {},
      cookiesPopup: false,
      startFilters: {},
      showVersionInfo: false,
      showBugInfo: false,
      showHelp: false,
      showCompatability: false,
      sideHidden: true,
      graphWindowStyle: {
        height: '75vh',
        'min-height': '75vh',
      },
      showLegend: false,
      physicsDisabled: false,
      jid: undefined,
      hoverResizer: false,
      sideCardWidth: 0,
      mainWidth: 0,
      resizeStart: undefined
    }
  },
  created() {
    this.sideCardWidth = window.document.body.getBoundingClientRect().width * 0.2
    this.mainWidth = window.document.body.getBoundingClientRect().width
    this.loadUser()
    this.init()
  },

  watch: {
    '$route'(to, from) {
      let new_gid = this.$route.params["gid"]
      this.applyUrlTab(true)
      this.setView()
      if (new_gid && new_gid !== this.gid) {
        this.jid = null
        this.gid = new_gid
        this.reloadAll()
      }
      if (this.$route.params["job"] != null) {
        let new_jid = this.$route.params["job"]
        if (this.jid !== new_jid) {
          this.gid = null
          this.jid = new_jid
          this.reloadAll()
        }
      }
    },
  },
  methods: {
    init: async function () {
      this.colors = {
        main: {bg1: '#383838', primary: '#35d0d4'},
        buttons: {graphs: {active: "deep-purple accent-2", inactive: undefined}},
        tabs: {active: "#35d0d4", inactive: "white"}
      }
      this.tabslist = [
        {id: 0, label: "Start", icon: "fas fa-filter", color: this.colors.tabs.active, note: false},
        {id: 1, label: "Graph", icon: "fas fa-project-diagram", color: this.colors.tabs.inactive, note: false},
        {id: 2, label: "List", icon: "fas fa-list-ul", color: this.colors.tabs.inactive, note: false},
        {id: 3, label: "History", icon: "fas fa-history", color: this.colors.tabs.inactive, note: false},
      ]
      this.initComponents()
      this.$global.metadata = await this.$http.getMetadata()
      this.$global.metagraph = this.$utils.adjustMetaOptions(await this.$http.getMetagraph())
      this.metaLoaded = true;
      this.initGraphs()
      this.setView()
    },

    disablePhysics: function (bool) {
      this.physicsDisabled = bool
    },
    initUser: function () {
      this.$http.get("/initUser").then(response => {
        if (response.data !== undefined) {
          return response.data;
        }
      }).then(userId => {
        this.$cookies.set("uid", userId);
        this.$router.go()
      }).catch(console.error)
    },
    showTos: function () {
      if (this.$refs.tos)
        this.$refs.tos.show()
      else
        setTimeout(() => {
          this.showTos()
        }, 500)
    },

    loadUser: function () {
      if (this.$cookies.get("uid") == null) {
        this.showTos()
      } else {
        if (sessionStorage.getItem("tos") == null)
          this.showTos()
        this.$http.get("/getUser?user=" + this.$cookies.get("uid")).then(response => {
          if (response.data !== undefined)
            return response.data
        }).then(data => {
          this.$cookies.set("uid", data.uid);
        }).catch(err => console.error(err))
      }
    },
    modifyURL: function (view) {
      let path = "/explore/" + view + "/start/" + this.gid
      if (this.$route.fullPath !== path)
        this.$router.push(path)
    },
    clearURL: function (view) {
      this.gid = undefined
      if (this.$router.currentRoute.fullPath.length > 1) {
        if (!view)
          this.$router.push("/explore/")
        else {

          let path = "/explore/" + view + "/start"
          if (this.$route.fullPath !== path)
            this.$router.push(path)
          this.reloadAll(["start"])
        }
      }
    }
    ,
    reloadAll: function (except) {
      this.tabslist[1].icon = "fas fa-project-diagram"
      this.tabslist[2].icon = "fas fa-list-ul"
      if (this.$refs.list && (except == null || except.indexOf("list") === -1)) {
        this.$refs.list.setLoading(true)
        this.$refs.list.reload();
      }
      if (this.$refs.graph && (except == null || except.indexOf("graph") === -1))
        this.$refs.graph.reload()
      if (this.$refs.history && (except == null || except.indexOf("history") === -1))
        this.$refs.history.reload()
      if (this.$refs.side && (except == null || except.indexOf("side") === -1))
        this.$refs.side.reload()
      if (this.$refs.start && (except == null || except.indexOf("start") === -1))
        this.$refs.start.reload()
    }
    ,
    setView: function () {
      let path = this.$route.path.split("/")
      let start = path.indexOf('start') > -1
      let home = path.indexOf('home') > -1
      let history = path.indexOf('history') > -1
      let mode = path.length > 1 ? path[2] : undefined
      if ((path[4] != null && path[3] === "list")) {
        this.setSideVisible(true);
      }
      if (start || home)
        this.setSideVisible(false);

      if (history) {
        this.selectTab(3, true)
      }

    }
    ,
    selectionColorSelect: function () {
      if (!this.$refs.graph.isVisualized || !this.$refs.graph.graphExists()) {
        this.printNotification("Graph must be visualized first!", 2)
        return
      }
      this.$refs.list.selectColor()
    }
    ,
    setTabId: function (tab, skipReroute) {
      this.selectTab(['start', 'graph', 'list', 'history'].indexOf(tab), skipReroute)
    }
    ,
    applyUrlTab: function (skipReroute) {

      let new_tab = this.$route.params["job"] != null ? "quick" : this.$route.params["tab"]
      if (new_tab && new_tab !== this.tab) {
        this.tab = new_tab
        this.setTabId(new_tab, skipReroute)
      }
    }
    ,
    initGraphs: function () {
      this.gid = this.$route.params["gid"]
      if (this.gid != null) {
        this.scheduleVisualization(this.gid)
        this.applyUrlTab(true)
      }
    },

    setMainWidth: function () {
      this.mainWidth = window.document.body.getBoundingClientRect().width
      if (this.sideCardWidth > this.mainWidth * 0.4)
        this.sideCardWidth = this.mainWidth * 0.4
    },

    scheduleVisualization: function (id) {
      if (this.$refs.graph == null)
        setTimeout(this.scheduleVisualization, 100, id)
      else
        this.$refs.graph.loadNetworkById(id)
    },

    setSideVisible: function (bool) {
      this.$set(this, 'sideHidden', !bool)
    }
    ,
    visualizationEvent: function () {
      this.reloadSide()
      this.setSideVisible(true)
      this.tabslist[1].icon = "fas fa-project-diagram"
    },

    disableLoadingEvent: function (tab) {
      this.tabslist[tab].icon = "fas fa-project-diagram"
    },
    reloadSide: function () {
      this.$refs.side.$forceUpdate()
    }
    ,
    toggleGraphSelectMode: function (select) {
      this.$refs.graph.toggleSelectMode(select);
    }
    ,
    focusInGraph: function (type, id) {
      if (!this.$refs.graph.isVisualized || !this.$refs.graph.graphExists()) {
        this.printNotification("Graph must be visualized first!", 2)
        return
      }
      this.setTabId("graph", false)
      new Promise(resolve => setTimeout(resolve, 500)).then(() => {
        if (type === "node") {
          this.$refs.side.setSelectedNode(id)
          this.$refs.graph.setSelection([id])
        } else
          this.$refs.graph.focusEdge(id)
      })
    }
    ,
    redirect: function (route, reload) {
      if (this.$route.fullPath !== route) {
        this.$router.push(route)
        if (reload)
          this.$router.go()
      }
    }
    ,
    downloadEntries: async function (entity, name) {
      let table = await this.$http.getTableDownload(this.gid, entity, name, ["primaryDomainId", "displayName"])
      this.download(this.gid + "_" + name + "-" + entity + ".tsv", table)
    },
    download: function (name, content) {
      let dl = document.createElement('a')
      dl.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(content))
      dl.setAttribute('download', name)
      dl.style.direction = 'none'
      document.body.appendChild(dl)
      dl.click()
      document.body.removeChild(dl)
    }
    ,
    initComponents: function () {
      this.options.start = {skipVis: true, onlyConnected: true, selectedElements: []}
      this.options.graph = {
        physics: false,
        noPhysics: false,
        loops: true,
        single: true,
        visualized: false,
        sizeWarning: false,
        component: false,
        selection: {selectMode: false},
        legend: {}
      }
      this.options.list = {showAll: true, selected: 0, total: 0, countMap: {nodes: {}, edges: {}}, entityGraph: {}}
      this.options.history = {chronological: false, otherUsers: false, entityGraph: {}, favos: false}
    }
    ,
    loadSubSelection: function (selection) {
      this.loadGraph({data: selection})
    }
    ,

    addListJob: function (data) {
      if (this.$refs.side)
        this.$refs.side.addJob(data);
    },

    loadLegendEvent: function (val) {
      if (val === this.showLegend && val) {
        this.showLegend = false;
      }
      this.$nextTick(() => {
        this.showLegend = val
      })
    }
    ,
    applyMultiSelect: function (selection) {
      this.$refs.list.applyMultiSelect(selection)
      this.printNotification("Added " + selection.length + " nodes to selection!", 1)
    }
    ,
    setMultiSelection: function (selection) {
      this.$refs.side.setMultiSelect(selection)
    }
    ,
    sizeWarning: function (info) {
      if (!this.$cookies.get("override-limit")) {
        this.listWarnObject = info;
        this.listDialog = true;
      }
      this.tabslist[1].icon = "fas fa-project-diagram"
      this.tabslist[2].icon = "fas fa-list-ul"
      this.$refs.list.setLoading(false)
    },

    switchToolOption: function (option, value) {
      if (option === "nodeStyle") {
        this.$refs.graph.switchNodeStyle(value)
      }
    },

    toggleToolOption: function (option, value) {
      if (option === "physics")
        this.$refs.graph.setPhysics(value);
      if (option === "loops")
        this.$refs.graph.showLoops(value)
      if (option === "unconnected")
        this.$refs.graph.showUnconnected(value)
      if (option === "isolation")
        this.$refs.graph.graphViewEvent(value)
      if (option === 'shadow')
        this.$refs.graph.setShadow(value)
      if (option === 'label')
        this.$refs.graph.showLabels(value)
    },

    clickToolOption: function (option) {
      if (option === "fit")
        this.$refs.graph.setSelection()
    },

    setGlobalGid: function (gid) {
      this.gid = gid
      let tab = (this.tab !== undefined && this.tab !== "start" && this.tab !== "history") ? this.tab : "list"
      this.loadGraphURL(gid, tab)
    },
    loadGraph: function (graph) {
      this.tabslist[1].icon = "fas fa-circle-notch fa-spin"
      this.tabslist[2].icon = "fas fa-circle-notch fa-spin"
      if (graph.post.id)
        this.loadGraphURL(graph.post.id, "list")
      else
        this.$refs.graph.loadNewNetwork(graph.post)
    }
    ,
    loadGraphNewTab: function (graph) {
      let route = this.$router.resolve({
        path: "/explore/advanced/list/" + graph.post.id,
      })
      window.open(route.href, "_blank")
    }
    ,
    recolorGraph: function (request) {
      this.$refs.legend.addColoring(request)
      this.$nextTick(() => this.$refs.graph.recolorGraph(request))
    }
    ,
    listSelectionEvent: function (type, operation) {
      if (operation === "all")
        this.$refs.list.selectAll(type)
      if (operation === "none")
        this.$refs.list.deselectAll(type)
      if (operation === "induced")
        this.$refs.list.selectEdges()
    },
    toggleNodeSelection: function (nodes) {
      this.$refs.list.toggleNodes(nodes)
    },
    toggleEdgeSelection: function (edges) {
      this.$refs.list.toggleEdges(edges)
    }
    ,
    evalPostInfo: function (info) {
      let sum = 0
      for (let n in info.nodes)
        sum += info.nodes[n];
      if (sum === 0) {
        this.printNotification("Resulting graph contains no node!", 2)
        return
      }
      for (let e in info.edges)
        sum += info.edges[e]
      if (sum >= 100000 && !this.$cookies.get("override-limit")) {
        this.sizeWarning(info)
        this.tabslist[1].icon = "fas fa-project-diagram"
        this.tabslist[2].icon = "fas fa-list-ul"
        this.$refs.list.setLoading(false)
      } else {
        this.options.graph.noPhysics = sum > 50000
        this.gid = info.id
        let tab = (this.tab !== undefined && this.tab !== "start" && this.tab !== "history") ? this.tab : "list"
        this.$http.get("/archiveHistory?uid=" + this.$cookies.get("uid") + "&gid=" + info.id).then(() => {
          this.loadGraphURL(info.id, tab)
        }).catch(err => console.error(err))
      }
    }
    ,
    loadGraphURL: function (id, tab) {
      this.$router.push({path: "/explore/advanced/" + tab + "/" + id})
      this.$refs.graph.loadNetworkById(id)
      this.$refs.list.reload()
      this.$refs.history.reload()
      this.$refs.side.reload()
    }
    ,
    registerJob: function (data) {
      this.$refs.side.addJob(data)
    }
    ,
    reloadHistory: function () {
      this.$refs.history.reload()
    }
    ,
    openAlgorithmDialogEvent: function (data) {
      this.$refs.list.openAlgorithmDialogEvent(data)
    }
    ,
    applyEvent: function (bool) {
      if (this.selectedTabId === 0)
        this.$refs.start.executeGraphLoad(bool)
      if (this.selectedTabId === 1)
        this.$refs.graph.visualize()
    }
    ,
    resizeDown: function (e) {
      this.resizeStart = e
      this.$refs.mainContainer.addEventListener("mousemove", this.resizeMove)
    },

    resizeUp: function (e) {
      this.resizeStart = undefined
      this.$refs.mainContainer.removeEventListener("mousemove", this.resizeMove)
    },

    resizeMove: function (e) {
      let size = this.sideCardWidth - (e.clientX - this.resizeStart.clientX);
      if (size < 700 && size < 0.4 * this.mainWidth) {
        if (size > 300) {
          this.sideCardWidth = size
          this.resizeStart = e;
        } else {
          this.sideCardWidth = 300
        }
      } else {
        this.sideCardWidth = Math.min(700, 0.4 * this.mainWidth);
      }
    },


    listModification: function (event) {
      this.$refs.list.receiveEvent(event)
    }
    ,
    reverseHistorySorting: function () {
      this.$refs.history.reverseList()
    }
    ,
    historyReloadEvent: function () {
      this.$refs.history.$forceUpdate()
    }
    ,
    nodeDetails: function (data) {
      let type = ""
      this.$global.metagraph.nodes.forEach(n => {
        if (n.group.startsWith(data.prefix))
          type = n.group;
      })
      this.selectTab(2)
      this.loadDetails({type: "node", name: type, id: data.id})
    }
    ,
    setTabNotification: function (tabId) {
      if (this.selectedTabId !== tabId)
        this.tabslist[tabId].note = true
      if (tabId === 1) {
        this.tabslist[tabId].icon = "fas fa-project-diagram"
      }
      if (tabId === 2) {
        this.tabslist[tabId].icon = "fas fa-list-ul"
      }
    }
    ,
    updatePhysics: function () {
      this.$refs.graph.setPhysics(this.options.graph.physics)
    }
    ,
    showLoops: function (state) {
      this.$refs.graph.showLoops(state)
    }
    ,
    showUnconnected: function (state) {
      this.$refs.graph.showUnconnected(state)
    }
    ,
    loadSelection: function (params) {
      if (params != null && params.nodes != null && params.nodes.length > 0) {
        this.$refs.tools.setSelectedNodeId(params.nodes[0])
        this.$refs.side.loadSelection(this.$refs.graph.identifyNeighbors(params.nodes[0]))
        this.$refs.side.loadDetails({
          edge: false,
          type: ["gene", "protein", "pathway", "disorder", "drug"][["gen", "pro", "pat", "dis", "dru"].indexOf(params.nodes[0].substring(0, 3))],
          id: parseInt(params.nodes[0].substring(4))
        })

      } else if (params != null && params.edges != null && params.edges.length > 0) {
        this.$refs.side.loadDetails({
          edge: true,
          type: params.edges[0].title,
          id1: parseInt(params.edges[0].from.substring(4)),
          id2: parseInt(params.edges[0].to.substring(4))
        })
      } else {
        this.$refs.side.loadSelection(this.$refs.graph.getAllNodes())
        this.$refs.side.loadDetails()
      }
    }
    ,
    setSelectedNode: function (nodeId) {
      if (nodeId !== undefined) {
        this.$refs.graph.setSelection([nodeId]);
        this.loadSelection({nodes: [nodeId]})
      } else {
        this.$refs.graph.setSelection()
        this.loadSelection()
      }
    }
    ,
    filter: function (filterData) {
      this.adaptSidecard(filterData)
    }
    ,
    selectTab: function (tabid, skipReroute) {
      if (this.selectedTabId === tabid)
        return
      if (this.selectedTabId === 0 && !skipReroute)
        this.$nextTick(() => {
          this.$refs.start.reset()
        })
      let colInactive = this.colors.tabs.inactive;
      let colActive = this.colors.tabs.active;
      for (let idx in this.tabslist) {
        if (parseInt(idx) === tabid) {
          this.tabslist[idx].color = colActive
          this.tabslist[idx].note = false
        } else
          this.tabslist[idx].color = colInactive
      }
      this.selectedTabId = tabid;
      if (!skipReroute) {
        let route = ""
        if (this.gid != null)
          route = ("/explore/" + ['quick/start', 'advanced/graph', 'advanced/list', 'advanced/history'][tabid] + "/" + this.gid)
        else
          route = ("/" + ['explore', 'explore', 'explore', 'history'][tabid])
        if (this.$route.path !== route)
          this.$router.push(route)
      }
      this.adaptSidecard()
    }
    ,
    reloadTables: function () {
      this.$refs.list.reloadTables()
    }
    ,
    adaptSidecard: function (param) {
      // if (this.selectedTabId === 0) {
      // this.$refs.side.loadFilter(param)
      // this.setSideVisible(false)
      // }
      if (this.selectedTabId === 3) {
        this.setSideVisible(true)
      }
      if (this.selectedTabId === 2 && this.gid == null)
        this.setSideVisible(false)
      if (this.selectedTabId === 1)
        this.setSideVisible(this.$refs.graph != null && this.$refs.graph.isVisualized())
    }
    ,
    loadDetails: function (params) {
      this.$refs.side.loadDetails(params)
    }
    ,
    resolveWarning: async function (id, download) {
      if (download)
        await window.open(CONFIG.HOST_URL + CONFIG.CONTEXT_PATH + '/api/downloadGraph?gid=' + id)
      else {
        this.listDialog = false;
        this.$http.removeGraph(id)
      }
    },
    printNotification: function (message, style, timeout) {
      if (style === 1) {
        let old = this.notifications.style1.timeout;
        this.notifications.style1.timeout = timeout
        this.setNotification(this.notifications.style1, message)
        if (timeout !== old)
          setTimeout(() => {
            this.notifications.style1.timeout = old
          }, timeout)
      }
      if (style === 2) {
        let old = this.notifications.style2.timeout;
        this.notifications.style2.timeout = timeout
        this.setNotification(this.notifications.style2, message)
        if (timeout !== old)
          setTimeout(() => {
            this.notifications.style2.timeout = old
          }, timeout)
      }
    }
    ,

    openExternal: function (url) {
      window.open(url, '_blank')
    }
    ,
    setNotification: function (to, message) {
      to.message = message;
      to.show = true;
    }
    ,
    graphViewEvent: function (data) {
      if (this.$refs.tools != null && data.params != null)
        data.params.loops = this.$refs.tools.isLoops()
      this.$refs.graph.graphViewEvent(data)
    }
    ,


    formatTimestamp(ts) {
      return this.$utils.formatTime(ts)
    }
  }
  ,
  components: {
    LegalDialog,
    headerBar,
    Network,
    SideCard,
    Start,
    List,
    History,
    Legend,
    Tools,
    Footer,
    BugSheet,
    HelpSheet,
    CompatabilitySheet,
    VersionSheet,
    VisualizationOptions,
  }
  ,


}

</script>

<style lang="sass">
#v-app-bar
  text-align: center

#header
  width: 100%
  height: 75px
  background-color: lightcoral
  text-align: center


#app
  font-family: 'Avenir', Helvetica, Arial, sans-serif
  -webkit-font-smoothing: antialiased
  -moz-osx-font-smoothing: grayscale
  text-align: center
  color: #2c3e50
//padding-top: 60px


h1, h2
  font-weight: normal


ul
  list-style-type: none
  padding: 0


li
  display: inline-block
  margin: 0 10px

a
  color: #42b983

</style>
