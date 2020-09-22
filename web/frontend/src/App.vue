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
    <v-navigation-drawer app>
<!--      <v-card-->
<!--        height="200"-->
<!--        width="256"-->
<!--        class="mx-auto"-->
<!--      >-->
<!--        <v-navigation-drawer permanent>-->
<!--          <v-list-item>-->
<!--            <v-list-item-content>-->
<!--              <v-list-item-title class="title">-->
<!--                Selection Tools-->
<!--              </v-list-item-title>-->
<!--              <v-list-item-subtitle>-->
<!--                discover the graph-->
<!--              </v-list-item-subtitle>-->
<!--            </v-list-item-content>-->
<!--          </v-list-item>-->

<!--          <v-divider></v-divider>-->

<!--          <v-list-->
<!--            dense-->
<!--            nav-->
<!--          >-->
<!--            <v-list-item>-->
<!--              <v-list-item-icon>-->
<!--                <v-icon>fas fa-filter</v-icon>-->
<!--              </v-list-item-icon>-->

<!--              <v-list-item-content>-->
<!--                <v-list-item-title>Apply Filter</v-list-item-title>-->
<!--              </v-list-item-content>-->
<!--            </v-list-item>-->
<!--            <v-list-item>-->
<!--              <v-list-item-icon>-->
<!--                <v-icon>fas fa-search</v-icon>-->
<!--              </v-list-item-icon>-->

<!--              <v-list-item-content>-->
<!--                <v-list-item-title>Search</v-list-item-title>-->
<!--              </v-list-item-content>-->
<!--            </v-list-item>-->
<!--          </v-list>-->
<!--        </v-navigation-drawer>-->
<!--      </v-card>-->
      <v-card
        height="100%"
        width="256"
        class="mx-auto"
      >
        <v-navigation-drawer>

          <v-list-item>
            <v-list-item-content>
              <v-list-item-title class="title">
                Current Selection
              </v-list-item-title>
              <v-list-item-subtitle>
                Including neighbors of first degree
              </v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>

          <v-simple-table fixed-header v-if="selectedNode !== undefined">
            <template v-slot:default>
              <thead>
              <tr>
                <th class="text-center">ID</th>
                <th class="text-center">Label</th>
              </tr>
              </thead>
              <tbody>
              <tr :key="selectedNode.id">
                <td><b>{{ selectedNode.id }}</b></td>
                <td><b>{{ selectedNode.title }}</b></td>
              </tr>
              <tr v-for="item in neighborNodes" :key="item.id" v-on:click="setSelectedNode(item.id)">
                <td>{{ item.id }}</td>
                <td>{{ item.title }}</td>
              </tr>
              </tbody>
            </template>
          </v-simple-table>
        </v-navigation-drawer>
      </v-card>

    </v-navigation-drawer>


    <v-main style="padding-top: 0">
      <v-container v-show="selectedTabId===0" fluid>
        <Start ref="start" v-on:graphLoadEvent="loadGraph" :colors="colors" ></Start>
      </v-container>
      <v-container v-show="selectedTabId===1" fluid>
        <Graph ref="graph" v-on:selectionEvent="loadSelection" v-on:finishedEvent="setTabNotification(1)"></Graph>
      </v-container>
    </v-main>

    <v-footer app>
      <!-- -->
    </v-footer>
  </v-app>
</template>

<script>
import Graph from './views/Graph.vue';
import Start from './views/Start.vue';
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
  data() {
    return {
      graphLoad: this.graphLoad,
      graphKey: 0,
      neighborNodes: this.neighborNodes,
      selectedNode: this.selectedNode,
      tabslist: this.tabslist,
      selectedTabId: this.selectedTabId,
      colors: this.colors,
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
    loadGraph: function (graph){
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
      if (params) {
        this.selectedNode = params.primary;
        this.neighborNodes = params.neighbors;
      } else {
        this.selectedNode = undefined;
        this.neighborNodes = [];
      }
    },
    setSelectedNode: function (nodeId){
      this.$refs.graph.setSelection([nodeId]);
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
    }
  },
  components: {
    headerBar,
    Graph,
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
