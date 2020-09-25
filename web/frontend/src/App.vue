<template>
  <v-app>
    <headerBar/>
    <v-card>
      <v-toolbar flat>
        <template v-slot:extension>
          <v-tabs
            fixed-tabs
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
    <SideCard v-show="showSidecard" ref="side" v-on:nodeSelectionEvent="setSelectedNode" v-on:addFilterEvent="addFilter"></SideCard>


    <v-main app style="padding-top: 0">
      <v-container v-show="selectedTabId===0" fluid>
        <Start ref="start" v-on:graphLoadEvent="loadGraph" v-on:filterEvent="filter" :colors="colors"></Start>
      </v-container>
      <v-container v-show="selectedTabId===1" fluid>
        <Graph ref="graph" v-on:selectionEvent="loadSelection" v-on:finishedEvent="setTabNotification(1)"></Graph>
      </v-container>
    </v-main>

    <v-footer app>
    </v-footer>
  </v-app>
</template>

<script>
import Graph from './views/Graph.vue';
import Start from './views/Start.vue';
import SideCard from './views/SideCard.vue'
import headerBar from './components/header.vue'


export default {
  name: 'app',
  graphLoad: {},
  graphKey: 0,
  exampleGraph: undefined,
  selectedNode: undefined,
  neighborNodes: [],
  selectedTabId: 0,
  colors: {},
  tabslist: {},
  showSidecard: false,
  data() {
    return {
      graphLoad: this.graphLoad,
      graphKey: 0,
      neighborNodes: this.neighborNodes,
      selectedNode: this.selectedNode,
      tabslist: this.tabslist,
      selectedTabId: this.selectedTabId,
      colors: this.colors,
      showSidecard: this.showSidecard,
    }
  },
  created() {
    this.colors = {
      buttons: {graphs: {active: "deep-purple accent-2", inactive: undefined}},
      tabs: {active: "rgba(25 118 210)", inactive: "rgba(0,0,0,.54)"}
    }
    this.selectedTabId = 0;
    this.tabslist = [
      {id: 0, label: "Start", icon: "fas fa-filter", color: this.colors.tabs.active, note: false},
      {id: 1, label: "Graph", icon: "fas fa-project-diagram", color: this.colors.tabs.inactive, note: false},
      {id: 2, label: "List", icon: "fas fa-list-ul", color: this.colors.tabs.inactive, note: false},
      {id: 3, label: "Statistics", icon: "fas fa-chart-pie", color: this.colors.tabs.inactive, note: false},
    ]
  },
  // this.setGraph({message: "Default graph"})
  methods: {
    loadGraph: function (graph) {
      this.tabslist[1].icon = "fas fa-circle-notch fa-spin"
      this.$refs.graph.loadData(graph)
    },
    setTabNotification: function (tabId) {
      if (this.selectedTabId !== tabId)
        this.tabslist[tabId].note = true
      if (tabId === 1) {
        this.tabslist[tabId].icon = "fas fa-project-diagram"
      }
    }
    ,
    loadSelection: function (params) {
      // if (params) {
      //   this.selectedNode = params.primary;
      //   this.neighborNodes = params.neighbors;
      // } else {
      //   this.selectedNode = undefined;
      //   this.neighborNodes = [];
      // }
      this.$refs.side.loadSelection(this.$refs.graph.identifyNeighbors(params.nodes[0]))
    },
    setSelectedNode: function (nodeId) {
      this.$refs.graph.setSelection([nodeId]);
      this.loadSelection({nodes: [nodeId]})
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
    addFilter: function (param){
      this.$refs.start.addFilter(param)
    },
    adaptSidecard: function (param) {
      if (this.selectedTabId === 1) {
        this.showSidecard = true
        this.$refs.side.setTitle({title: "Current Selection", description: "Including neighbors of first degree"})
      } else if (this.selectedTabId === 0) {
        console.log(param)
        if (param !== undefined) {
          this.showSidecard = true
          this.$refs.side.setTitle({
            title: "Filter " + param.name,
            description: "Apply a filter for better exploration"
          })
          this.$refs.side.loadFilter(param)
        } else {
          this.$refs.side.loadFilter(undefined)
          this.showSidecard = false
        }
      }
    }
  },
  components: {
    headerBar,
    Graph,
    SideCard,
    Start
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
