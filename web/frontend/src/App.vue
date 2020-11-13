<template>
  <v-app>
    <headerBar/>
    <v-card>
      <v-toolbar flat>
        <template v-slot:extension>
          <v-tabs
            fixed-tabs
            v-model="selectedTabId"
          >
            <v-tabs-slider></v-tabs-slider>
            <v-tab v-for="tab in tabslist" class="primary--text" v-on:click="selectTab(tab.id)" :key=tab.id>
              <v-badge
                dot
                color="blue"
                :value="tab.note"
              >
                <i :style="{color:tab.color}">
                  <v-icon dense>{{ tab.icon }}</v-icon>
                  {{ tab.label }}
                </i>
              </v-badge>
            </v-tab>

          </v-tabs>
        </template>
      </v-toolbar>
    </v-card>

    <v-container>
      <v-row>
        <v-col cols="9">

          <v-main app style="padding-top: 0">

            <v-container v-show="selectedTabId===0" fluid>
              <Start v-if="metagraph !== undefined" ref="start"
                     v-on:graphLoadEvent="loadGraph"
                     v-on:printNotificationEvent="printNotification"
                     :colors="colors" :metagraph="metagraph" :options="options.start" :filters="startFilters"></Start>
            </v-container>
            <v-container v-show="selectedTabId===1" fluid>
              <Graph ref="graph"
                     v-on:selectionEvent="loadSelection"
                     v-on:finishedEvent="setTabNotification(1)"
                     v-on:visualisationEvent="visualisationEvent"
                     v-on:printNotificationEvent="printNotification"
                     v-on:graphLoadedEvent="loadList"
                     :configuration="options.graph"
              ></Graph>

            </v-container>
            <v-container v-show="selectedTabId===2" fluid>
              <List ref="list"
                    v-on:finishedEvent="setTabNotification(2)"
                    v-on:selectionEvent="loadDetails"
                    v-on:loadSelectionEvent="loadSubSelection"
                    v-on:updateInfo="evalPostInfo"
                    v-on:printNotificationEvent="printNotification"
              ></List>
            </v-container>
            <v-container v-show="selectedTabId===3" fluid>
              <History ref="history"
                       v-on:graphLoadEvent="loadGraph"
                       v-on:printNotificationEvent="printNotification"
              ></History>
            </v-container>
            <v-snackbar v-model="notifications.style1.show" :multi-line="true" :timeout="notifications.style1.timeout">
              {{ notifications.style1.message }}
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
        <v-col cols="3">
          <SideCard ref="side"
                    v-on:printNotificationEvent="printNotification"
                    v-on:nodeSelectionEvent="setSelectedNode"
                    v-on:updatePhysicsEvent="updatePhysics"
                    v-on:nodeDetailsEvent="nodeDetails"
                    v-on:applyEvent="applyEvent"
                    :options="options"
                    :selected-tab="selectedTabId"
                    :filters="startFilters"
          ></SideCard>
        </v-col>
      </v-row>
    </v-container>
    <v-footer app>
    </v-footer>
  </v-app>
</template>

<script>
import Graph from './views/Graph.vue';
import Start from './views/Start.vue';
import History from "./views/History";
import List from './views/List.vue'
import SideCard from './views/SideCard.vue'
import headerBar from './components/header.vue'


export default {
  name: 'app',
  exampleGraph: undefined,

  data() {
    return {
      graphLoad: {},
      graphKey: 0,
      neighborNodes: [],
      selectedNode: undefined,
      tabslist: {},
      selectedTabId: 0,
      colors: {},
      metagraph: undefined,
      listWarnObject: undefined,
      listDialog: false,
      notifications: {style1: {show: false, message: "", timeout: 2000}, style2: {}},
      options: {},
      startFilters: {},
    }
  },
  created() {
    this.loadUser()
    this.colors = {
      buttons: {graphs: {active: "deep-purple accent-2", inactive: undefined}},
      tabs: {active: "rgba(25 118 210)", inactive: "rgba(0,0,0,.54)"}
    }
    this.selectedTabId = 0;
    this.tabslist = [
      {id: 0, label: "Start", icon: "fas fa-filter", color: this.colors.tabs.active, note: false},
      {id: 1, label: "Graph", icon: "fas fa-project-diagram", color: this.colors.tabs.inactive, note: false},
      {id: 2, label: "List", icon: "fas fa-list-ul", color: this.colors.tabs.inactive, note: false},
      {id: 3, label: "History", icon: "fas fa-history", color: this.colors.tabs.inactive, note: false},
    ]
    this.initGraphs()
    this.initComponents()
  },
  methods: {
    loadUser: function () {
      this.$http.get("/getUser?user=" + this.$cookies.get("uid")).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.$cookies.set("uid", data.uid);
      }).catch(err => console.log(err))
    },
    initGraphs: function () {
      this.$http.get("/getMetagraph").then(response => {
        this.metagraph = response.data;
        this.$refs.list.setMetagraph(this.metagraph)
      }).catch(err => {
        console.log(err)
      })
    },
    visualisationEvent: function () {
      this.$refs.side.$forceUpdate()
    },
    initComponents: function () {
      this.options.start = {skipVis: true, onlyConnected: true, selectedElements: []}
      this.options.graph = {physics: false, visualized: false, sizeWarning: false}
    },
    loadSubSelection: function (selection) {
      this.loadGraph({data: selection})
    },
    sizeWarning: function (info) {
      this.listWarnObject = info;
      this.listDialog = true;
    },
    loadGraph: function (graph) {
      this.tabslist[1].icon = "fas fa-circle-notch fa-spin"
      this.tabslist[2].icon = "fas fa-circle-notch fa-spin"
      if(this.options.graph.physics) {
        this.options.graph.physics = false;
        this.updatePhysics()
      }

      this.$http.post("/getGraphInfo", graph.post).then(response => {
        return response.data
      }).then(info => {
        this.evalPostInfo(info)
      }).catch(err => {
        console.log(err)
      })
    },
    loadList: function (gid) {
      this.$refs.list.getList(gid, this.metagraph)
    },
    evalPostInfo: function (info) {
      let sum = 0
      for (let n in info.nodes)
        sum += info.nodes[n];
      for (let e in info.edges)
        sum += info.edges[e]
      if (sum >= 100000) {
        this.sizeWarning(info)
        this.tabslist[1].icon = "fas fa-project-diagram"
        this.tabslist[2].icon = "fas fa-list-ul"
        this.$refs.list.setLoading(false)
      } else {
        console.log(info)
        this.$http.get("/archiveHistory?uid=" + this.$cookies.get("uid") + "&gid=" + info.id).then(() => {
          this.$router.push({path: '/' + info.id})
          this.$refs.graph.reload()
          this.$refs.list.reload()
          this.$refs.history.reload()
        }).catch(err => console.log(err))

      }
    },
    applyEvent: function () {
      if (this.selectedTabId === 0)
        this.$refs.start.loadGraph(-1)
      if (this.selectedTabId === 1)
        this.$refs.graph.visualizeNow()
    },
    nodeDetails: function (data) {
      let type = ""
      this.metagraph.nodes.forEach(n => {
        if (n.group.startsWith(data.prefix))
          type = n.group;
      })
      this.selectTab(2)
      this.loadDetails({type: "node", name: type, id: data.id})
    },
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
    },
    loadSelection: function (params) {
      if (params !== undefined)
        this.$refs.side.loadSelection(this.$refs.graph.identifyNeighbors(params.nodes[0]))
      else {
        this.$refs.side.loadSelection(this.$refs.graph.getAllNodes())
      }
    },
    setSelectedNode: function (nodeId) {
      if (nodeId !== undefined) {
        this.$refs.graph.setSelection([nodeId]);
        this.loadSelection({nodes: [nodeId]})
      } else {
        this.$refs.graph.setSelection()
        this.loadSelection()
      }
    },
    filter: function (filterData) {
      this.adaptSidecard(filterData)
    },
    selectTab: function (tabid) {
      if (this.selectedTabId === tabid)
        return
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

      this.adaptSidecard()
    },
    adaptSidecard: function (param) {
      if (this.selectedTabId === 0) {
        if (param !== undefined) {
          this.$refs.side.loadFilter(param)
        } else {
          this.$refs.side.loadFilter(undefined)
        }
      }
    },
    loadDetails: function (params) {
      this.$refs.side.loadDetails(params)
    },
    printNotification: function (message, style) {
      if (style === 1) {
        this.setNotification(this.notifications.style1, message)
      }
    },
    setNotification: function (to, message) {
      to.message = message;
      to.show = true;
    }
  },
  components: {
    headerBar,
    Graph,
    SideCard,
    Start,
    List,
    History
  },


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
  margin-top: 60px


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
