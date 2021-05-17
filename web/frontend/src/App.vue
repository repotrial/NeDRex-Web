<template style="overflow-y: hidden">
  <v-app :style="{marginTop:selectedTabId===0 ? '60px': '0px'}" id="app">
    <headerBar @showVersionEvent="showVersionInfo=true" @showBugEvent="showBugInfo=true" @showHelpEvent="showHelp=true"
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
    <v-container align-self="start">
      <v-row>
        <v-col :cols="sideHidden ? 12:9">
          <v-main app style="padding-top: 0">
            <v-container v-show="selectedTabId===0" fluid>

              <Start v-if="metagraph !== undefined" ref="start"
                     v-on:graphLoadEvent="loadGraph"
                     @graphLoadNewTabEvent="loadGraphNewTab"
                     v-on:printNotificationEvent="printNotification"
                     @showSideEvent="setSideVisible"
                     @clearURLEvent="clearURL"
                     :colors="colors" :metagraph="metagraph" :options="options.start" :filters="startFilters"></Start>
            </v-container>
            <v-container v-show="selectedTabId===1" fluid>
              <Graph ref="graph"
                     style="position: sticky; "
                     v-on:selectionEvent="loadSelection"
                     v-on:finishedEvent="setTabNotification(1)"
                     v-on:visualisationEvent="visualisationEvent"
                     v-on:printNotificationEvent="printNotification"
                     v-on:graphLoadedEvent="loadList"
                     v-on:multiSelectionEvent="setMultiSelection"
                     v-on:setMetagraphEvent="setMetagraph"
                     :configuration="options.graph"
                     :window-style="graphWindowStyle"
                     :legend="showLegend"
                     :meta="metagraph"
              >
                <template v-slot:legend>
                  <Legend v-if="showLegend" :metagraph="metagraph" :countMap="options.list.countMap" ref="legend"
                          :entityGraph="options.list.entityGraph"
                          :options="options.graph.legend"
                          @graphViewEvent="graphViewEvent"></Legend>

                </template>

              </Graph>

            </v-container>
            <v-container v-show="selectedTabId===2" fluid>
              <List ref="list"
                    v-on:finishedEvent="setTabNotification(2)"
                    v-on:selectionEvent="loadDetails"
                    v-on:loadSelectionEvent="loadSubSelection"
                    v-on:updateInfo="evalPostInfo"
                    v-on:printNotificationEvent="printNotification"
                    v-on:reloadSide="reloadSide"
                    v-on:addJobEvent="registerJob"
                    v-on:focusInGraphEvent="focusInGraph"
                    @loadLegendEvent="loadLegendEvent"
                    @recolorGraphEvent="recolorGraph"
                    :configuration="options.list"
              ></List>
            </v-container>
            <v-container v-show="selectedTabId===3" fluid>
              <History ref="history"
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
            >
              <v-card>
                <v-card-title class="headline">
                  List loading warning!
                </v-card-title>
                <v-card-text>The selected graph consists of the following number of elements:
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
                  filter!
                </v-card-text>
                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn
                    color="green darken-1"
                    text
                    @click="listDialog=false"
                  >
                    Dismiss
                  </v-btn>

                </v-card-actions>
              </v-card>
            </v-dialog>
          </v-main>
        </v-col>
        <v-col cols="3" v-show="!sideHidden">
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
                    v-on:executeAlgorithmEvent="executeAlgorithm"
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
                    :metagraph="metagraph"
          ></SideCard>
        </v-col>
      </v-row>
      <v-dialog
        v-model="cookiesPopup"
        persistent
        max-width="500"
      >
        <v-card>
          <v-card-title>This page uses cookies</v-card-title>
          <v-card-text>This graph browser uses cookies to store an identification for your user. This is used to show
            you
            your previous explorations.
          </v-card-text>
          <v-divider></v-divider>

          <v-card-actions>
            <v-btn
              color="green darken-1"
              text
              @click="closeCookiePopup"
            >
              Accept
            </v-btn>
          </v-card-actions>
        </v-card>

      </v-dialog>
    </v-container>
    <v-bottom-sheet inset v-model="showBugInfo" width="30vw" :overlay-color="colors.main.bg1">
      <v-sheet dark :color="colors.main.bg1">
        <v-list>
          <v-list-item>
            <v-list-item-title>
              Found a Bug?
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-content>
              Did you find a bug or have other ideas to improve NeDRex-Web? Great! Just let us know using one of the
              following options:
            </v-list-item-content>
          </v-list-item>

          <v-list-item>
            <v-list-item-icon>
              <v-icon left>fas fa-at</v-icon>
            </v-list-item-icon>
            <v-list-item-title>By Mail:</v-list-item-title>
            <v-list-item-subtitle>maieran@cip.ifi.lmu.de</v-list-item-subtitle>
          </v-list-item>

          <v-list-item>
            <v-list-item-icon>
              <v-icon left>fab fa-github</v-icon>

            </v-list-item-icon>
            <v-list-item-title>Filing an Issue:</v-list-item-title>
            <v-list-item-subtitle>
              <v-chip outlined @click="openExternal('https://github.com/AndiMajore/RepoScapeWeb/issues')">
                Github-Page
                <v-icon right>fas fa-external-link-alt</v-icon>
              </v-chip>
            </v-list-item-subtitle>
          </v-list-item>
          <v-list-item>
            <v-list-item-icon>
              <v-icon left>fas fa-table</v-icon>

            </v-list-item-icon>
            <v-list-item-title>Adding to spreadsheet:</v-list-item-title>
            <v-list-item-subtitle>
              <v-chip outlined
                      @click="openExternal('https://docs.google.com/spreadsheets/d/15ZqaWko_EH4filOLfwC_ZxtEAb-FxJ2sKSPv9MmV75I/edit?usp=sharing')">
                Google-Spreadsheet
                <v-icon right>fas fa-external-link-alt</v-icon>
              </v-chip>
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
      </v-sheet>

    </v-bottom-sheet>
    <v-bottom-sheet inset v-model="showHelp" width="30vw" :overlay-color="colors.main.bg1">
      <v-sheet dark :color="colors.main.bg1">

        <v-list>
          <v-list-item>
            <v-list-item-title>
              Need help?
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-content>
              You can either write an email or checking resources explaining each page of NeDRex-Web:
            </v-list-item-content>
          </v-list-item>

          <v-list-item>
            <v-list-item-icon>
              <v-icon left>fas fa-at</v-icon>
            </v-list-item-icon>
            <v-list-item-title>By Mail:</v-list-item-title>
            <v-list-item-subtitle>maieran@cip.ifi.lmu.de</v-list-item-subtitle>
          </v-list-item>

          <v-list-item>
            <v-list-item-icon>
              <v-icon left>fab fa-github</v-icon>

            </v-list-item-icon>
            <v-list-item-title>Application-Example:</v-list-item-title>
            <v-list-item-subtitle>
              <v-chip outlined
                      @click="openExternal('https://github.com/AndiMajore/RepoScapeWeb/tree/master/material/page_explanation')">
                Page-Explanations
                <v-icon right>fas fa-external-link-alt</v-icon>
              </v-chip>
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
      </v-sheet>

    </v-bottom-sheet>
    <v-bottom-sheet inset v-model="showVersionInfo" width="60vw" :overlay-color="colors.main.bg1">
      <v-sheet dark>
        <v-list>
          <v-list-item>
            <v-card-title>DATA SOURCE INFORMATION</v-card-title>
          </v-list-item>
          <v-list-item>
            <v-row>
              <v-col cols="5">
                <v-list>
                  <v-list-item>
                    <v-list-item-icon>
                      <v-icon left>fas fa-server</v-icon>
                    </v-list-item-icon>
                    <v-list-item-title>RepoTrialDB-version:</v-list-item-title>
                    <span>{{
                        metadata.repotrial && metadata.repotrial.version ? metadata.repotrial.version : "?"
                      }}</span>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-icon>
                      <v-icon left>fas fa-sync</v-icon>
                    </v-list-item-icon>
                    <v-list-item-title>Last Check:</v-list-item-title>
                    <span>{{
                        metadata.lastCheck !== undefined ? formatTimestamp(metadata.lastCheck)[1] + " ago" : "?"
                      }}</span>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-icon>
                      <v-icon left>fas fa-cloud-download-alt</v-icon>
                    </v-list-item-icon>
                    <v-list-item-title>Last Update:</v-list-item-title>
                    <span>{{
                        metadata.lastUpdate !== undefined ? formatTimestamp(metadata.lastUpdate)[1] + " ago" : "?"
                      }}</span>
                  </v-list-item>
                </v-list>
              </v-col>
              <v-divider vertical></v-divider>
              <v-col cols="6">
                <v-list v-if="metadata.repotrial &&metadata.repotrial.source_databases">
                  <v-list-item v-for="source in Object.keys(metadata.repotrial.source_databases)" :key="source">
                    <v-list-item-icon>
                      <v-icon left>fas fa-database</v-icon>
                      {{ source }}
                    </v-list-item-icon>
                    <v-list-item-title>
                      <span>{{ metadata.repotrial.source_databases[source].date }}</span>
                    </v-list-item-title>
                    <v-list-item-subtitle v-if="metadata.repotrial.source_databases[source].version!=null">
                      <span>(Version: {{ metadata.repotrial.source_databases[source].version }})</span>
                    </v-list-item-subtitle>
                    <v-list-item-subtitle v-else></v-list-item-subtitle>
                  </v-list-item>
                </v-list>
              </v-col>
            </v-row>

          </v-list-item>
        </v-list>
        <v-divider style="margin-left:25px; margin-right: 25px"></v-divider>
      </v-sheet>
    </v-bottom-sheet>
    <Footer :color="colors.main.bg1"></Footer>
  </v-app>
</template>

<script>
import Graph from './components/views/graph/Graph.vue';
import Start from './components/views/Start.vue';
import History from "./components/views/History";
import List from './components/views/List.vue'
import SideCard from './components/side/SideCard.vue'
import Home from './components/views/Home.vue'
import headerBar from './components/app/Header.vue'
import Legend from "./components/views/graph/Legend";
import Footer from "@/components/app/Footer";
import Utils from "./scripts/Utils"
import * as CONFIG from "./Config"

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
      metagraph: undefined,
      listWarnObject: undefined,
      listDialog: false,
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
      metadata: {},
      sideHidden: true,
      graphWindowStyle: {
        height: '75vh',
        'min-height': '75vh',
      },
      showLegend: false,
    }
  },
  created() {
    this.loadUser()
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
    this.loadMetadata()
    this.initGraphs()
    this.sideHidden = false
    this.setView()
  },
  watch: {
    '$route'(to, from) {
      let new_gid = this.$route.params["gid"]
      this.applyUrlTab(true)
      this.setView()
      if (new_gid && new_gid !== this.gid) {
        this.gid = new_gid
        this.reloadAll()
      }
    },

    selectedTabId: function (val) {
      if (val > 0 && this.gid !== undefined)
        this.sideHidden = false
      if (val === 0 && this.$refs.start.getStartType() < 2)
        this.sideHidden = true
    }
  },
  methods: {
    closeCookiePopup: function () {
      this.cookiesPopup = false
      this.$http.get("/initUser").then(response => {
        if (response.data !== undefined) {
          return response.data;
        }
      }).then(userId => {
        this.$cookies.set("uid", userId);
        this.$router.go()
      }).catch(err => console.log)
    },
    loadUser: function () {
      if (this.$cookies.get("uid") === null) {
        this.cookiesPopup = true
      } else {
        this.$http.get("/getUser?user=" + this.$cookies.get("uid")).then(response => {
          if (response.data !== undefined)
            return response.data
        }).then(data => {
          this.$cookies.set("uid", data.uid);
        }).catch(err => console.log(err))
      }
    },
    clearURL: function (view) {
      this.gid = undefined
      if (this.$router.currentRoute.fullPath.length > 1) {
        if (!view)
          this.$router.push("/home")
        else {
          let path = "/explore/" + view + "/start"
          if (this.$route.fullPath !== path)
            this.$router.push(path)
          this.reloadAll()
          this.$refs.graph.reload()
        }
      }
    }
    ,
    reloadAll: function () {
      this.tabslist[1].icon = "fas fa-project-diagram"
      this.tabslist[2].icon = "fas fa-list-ul"
      this.$refs.list.setLoading(true)
      this.$refs.graph.reload()
      this.$refs.list.reload()
      this.$refs.history.reload()
      this.$refs.side.reload()

    }
    ,
    setView: function () {
      console.log(this.$route)
      let path = this.$route.path.split("/")
      let start = path.indexOf('start')>-1
      let mode = path.length > 1 ? path[2] : undefined

      //TODO set start graph based on params
    }
    ,
    selectionColorSelect: function () {
      this.$refs.list.selectColor()
      this.$refs.graph.visualizeNow()
    }
    ,
    setTabId: function (tab, skipReroute) {
      this.selectTab(['start', 'graph', 'list', 'history'].indexOf(tab), skipReroute)
    }
    ,
    applyUrlTab: function (skipReroute) {
      let new_tab = this.$route.params["tab"]
      if (new_tab !== this.tab) {
        this.tab = new_tab
        this.setTabId(new_tab, skipReroute)
      }
    }
    ,
    initGraphs: function () {
      this.gid = this.$route.params["gid"]
      this.initComponents()
      this.$http.get("/getMetagraph").then(response => {
        this.metagraph = response.data;
        this.$refs.list.setMetagraph(this.metagraph)
        this.$refs.history.setMetagraph(this.metagraph)
      }).then(() => {
        this.applyUrlTab(true)
      }).catch(err => {
        console.log(err)
      })
    }
    ,
    setSideVisible: function (bool) {
      this.sideHidden = !bool
    }
    ,
    visualisationEvent: function () {
      this.reloadSide()
    }
    ,
    reloadSide: function () {
      this.$refs.side.$forceUpdate()
    }
    ,
    setMetagraph: function () {
      this.$refs.graph.setMetagraph(this.metagraph)
    }
    ,
    toggleGraphSelectMode: function (select) {
      this.$refs.graph.toggleSelectMode(select);
    }
    ,
    focusInGraph: function (type, id) {
      if (!this.$refs.graph.isVisualized || !this.$refs.graph.graphExists()) {
        this.printNotification("Graph must be visualized first!", 2)
        this.$refs.graph.visualizeNow()
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
    redirect: function (route) {
      if (this.$route.fullPath !== route) {
        this.$router.push(route)
      }
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
    }
    ,
    loadGraph: function (graph) {
      this.tabslist[1].icon = "fas fa-circle-notch fa-spin"
      this.tabslist[2].icon = "fas fa-circle-notch fa-spin"
      this.options.graph.visualized = false
      this.$refs.side.reload()
      if (this.options.graph.physics) {
        this.options.graph.physics = false;
        this.updatePhysics()
      }
      this.$http.get("/isReady").then(response => {
        if (response.data)
          this.printNotification("Request successfully sent!", 1)
        else
          this.printNotification("Request scheduled: Server is currently busy! please wait.", 2)
        this.$http.post("/getGraphInfo", graph.post).then(response => {
          return response.data
        }).then(info => {
          if (!graph.post.id)
            this.evalPostInfo(info, graph.post.tab)
          else
            this.loadGraphURL(graph.post.id, "list")
        }).catch(err => {
          console.log(err)
        })
      }).catch(console.log)
    }
    ,
    loadGraphNewTab: function (graph) {
      let route = this.$router.resolve({
        path: "/explore/advanced/list/"+graph.post.id,
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
    }
    ,
    loadList: function (gid) {
      this.$refs.list.getList(gid, this.metagraph)
    }
    ,
    evalPostInfo: function (info, tab) {
      //TODO move to graph view
      let sum = 0
      for (let n in info.nodes)
        sum += info.nodes[n];
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
        }).catch(err => console.log(err))

      }
    }
    ,
    loadGraphURL: function (id, tab) {
      this.$router.push({path: "/explore/advanced/" + tab + "/" + id})
      this.$refs.graph.reload()
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
    executeAlgorithm: function (algorithm, params) {
      this.$refs.list.executeAlgorithm(algorithm, params)
    }
    ,
    applyEvent: function (bool) {
      if (this.selectedTabId === 0)
        this.$refs.start.executeGraphLoad(bool)
      if (this.selectedTabId === 1)
        this.$refs.graph.visualizeNow()
    }
    ,
    listModification: function (event) {
      this.$refs.list.recieveEvent(event)
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
      this.metagraph.nodes.forEach(n => {
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
      if (params !== undefined)
        this.$refs.side.loadSelection(this.$refs.graph.identifyNeighbors(params.nodes[0]))
      else {
        this.$refs.side.loadSelection(this.$refs.graph.getAllNodes())
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
        this.$refs.start.reset()
      let colInactive = this.colors.tabs.inactive;
      let colActive = this.colors.tabs.active;
      for (let idx in this.tabslist) {
        if (idx == tabid) {
          this.tabslist[idx].color = colActive
          this.tabslist[idx].note = false
        } else
          this.tabslist[idx].color = colInactive
      }
      this.selectedTabId = tabid;
      if (!skipReroute && this.gid !== undefined)
        this.$router.push("/explore/advanced/" + ['start', 'graph', 'list', 'history'][tabid] + "/" + this.gid)

      this.adaptSidecard()
    }
    ,
    reloadTables: function () {
      this.$refs.list.reloadTables()
    }
    ,
    adaptSidecard: function (param) {
      if (this.selectedTabId === 0) {
        if (param !== undefined) {
          this.$refs.side.loadFilter(param)
        } else {
          this.$refs.side.loadFilter(undefined)
        }
      }
    }
    ,
    loadDetails: function (params) {
      this.$refs.side.loadDetails(params)
    }
    ,
    printNotification: function (message, style) {
      if (style === 1) {
        this.setNotification(this.notifications.style1, message)
      }
      if (style === 2) {
        this.setNotification(this.notifications.style2, message)
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
      this.$refs.graph.graphViewEvent(data)
    }
    ,

    loadMetadata() {
      this.$http.get("getMetadata").then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(meta => {
        this.metadata = meta
      }).catch(console.log)
    }
    ,
    formatTimestamp(ts) {
      return Utils.formatTime(ts)
    }
  }
  ,
  components: {
    headerBar,
    Graph,
    SideCard,
    Start,
    List,
    History,
    Legend,
    Home,
    Footer
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
