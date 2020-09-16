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
              <i :style="{color:tab.color}">
                <font-awesome-icon :icon=tab.icon></font-awesome-icon>
                {{ tab.label }}
              </i>

            </v-tab>

          </v-tabs>
        </template>
      </v-toolbar>
    </v-card>
    <v-navigation-drawer app>
      <v-card
        height="200"
        width="256"
        class="mx-auto"
      >
        <v-navigation-drawer permanent>
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title class="title">
                Selection Tools
              </v-list-item-title>
              <v-list-item-subtitle>
                discover the graph
              </v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>

          <v-divider></v-divider>

          <v-list
            dense
            nav
          >
            <v-list-item>
              <v-list-item-icon>
                <font-awesome-icon icon="filter"/>
              </v-list-item-icon>

              <v-list-item-content>
                <v-list-item-title>Apply Filter</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-list-item>
              <v-list-item-icon>
                <font-awesome-icon icon="search"/>
              </v-list-item-icon>

              <v-list-item-content>
                <v-list-item-title>Search</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-list>
        </v-navigation-drawer>
      </v-card>
      <v-card
        height="670"
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

          <v-simple-table v-if="selectedNode !== undefined">
            <template v-slot:default>
              <thead>
              <tr>
                <th class="text-left">ID</th>
                <th class="text-left">Label</th>
              </tr>
              </thead>
              <tbody>
              <tr :key="selectedNode.id">
                <td><b>{{ selectedNode.id }}</b></td>
                <td><b>{{ selectedNode.title }}</b></td>
              </tr>
              <tr v-for="item in neighborNodes" :key="item.id">
                <td>{{ item.id }}</td>
                <td>{{ item.title }}</td>
              </tr>
              </tbody>
            </template>
          </v-simple-table>
        </v-navigation-drawer>
      </v-card>

    </v-navigation-drawer>


    <!-- Sizes your content based upon application components -->
    <v-main style="padding-top: 0; height: 60%; max-height: 60%">
<!--      <template v-for="item in graphButtons">-->
<!--        <v-btn :name=item.id :outlined="item.active" :dark="!item.active" v-on:click="loadGraph(item.id)"-->
<!--               :color=item.color style="margin:5px">{{ item.label }}-->
<!--        </v-btn>-->
<!--      </template>-->

      <v-container v-show="selectedTabId===2" fluid>
        <Graph ref="graph" v-on:selectionEvent="loadSelection"
        ></Graph>
      </v-container>
    </v-main>

    <v-footer app>
      <!-- -->
    </v-footer>
  </v-app>
</template>

<script>
import {library} from '@fortawesome/fontawesome-svg-core'
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'

export default {
  name: 'app',
  graphLoad: {},
  graphKey: 0,
  exampleGraph: undefined,
  selectedNode: undefined,
  neighborNodes: [],
  graphButtons: [],
  selectedTabId: 0,
  colors: {},
  tabslist: {},
  data() {
    return {
      graphLoad: this.graphLoad,
      graphKey: 0,
      neighborNodes: this.neighborNodes,
      selectedNode: this.selectedNode,
      graphButtons: this.graphButtons,
      tabslist: this.tabslist,
      selectedTabId: this.selectedTabId,
    }
  },
  created() {
    this.colors = {
      buttons: {graphs: {active: "deep-purple accent-2", inactive: undefined}},
      tabs: {active: "rgba(25 118 210)", inactive: "rgba(0,0,0,.54)"}
    }
    this.graphButtons = [
      {id: 0, label: "Default Graph", color: this.colors.buttons.graphs.inactive, active: true},
      {id: 1, label: "Cancer-Comorbidity Graph", color: this.colors.buttons.graphs.inactive, active: false},
      {id: 2, label: "Neuro-Drug Graph", color: this.colors.buttons.graphs.inactive, active: false}
    ]
    this.tabslist = [
      {id: 1, label: "Start", icon: "filter", color: this.colors.tabs.active},
      {id: 2, label: "Graph", icon: "project-diagram", color: this.colors.tabs.inactive},
      {id: 3, label: "List", icon: "list-ul", color: this.colors.tabs.inactive},
      {id: 4, label: "Statistics", icon: "chart-pie", color: this.colors.tabs.inactive},
    ]
  },
  // this.setGraph({message: "Default graph"})
  methods: {
    loadGraph: function (id) {
      console.log(id)
      for (let index in this.graphButtons) {
        if (this.graphButtons[index].id === id) {
          if (this.graphButtons[index].active)
            return;
          this.graphButtons[index].active = true
          // this.graphButtons[index].color = this.colors.buttons.graphs.active;
        } else
          this.graphButtons[index].active = false
        // this.graphButtons[index].color = this.colors.buttons.graphs.inactive;
      }
      if (id === 0) {
        this.graphLoad = {name: "default"}
      } else if (id === 1) {
        this.graphLoad = {url: "/getExampleGraph1"}
      } else if (id === 2) {
        this.graphLoad = {url: "/getExampleGraph2"}
      }
      this.$refs.graph.loadData(this.graphLoad)
    },
    loadSelection: function (params) {
      console.log("selection")
      console.log(params)
      this.selectedNode = params.primary;
      this.neighborNodes = params.neighbors;
    },
    selectTab: function (tabid) {
      console.log(tabid)
      let colInactive = this.colors.tabs.inactive;
      let colActive = this.colors.tabs.active;
      for (let idx in this.tabslist) {
        if (this.tabslist[idx].id === tabid) {
          if (this.tabslist[idx].color === colActive)
            return
          else
            this.tabslist[idx].color = colActive
        } else
          this.tabslist[idx].color = colInactive
      }
      this.selectedTabId = tabid;
    }
  },
  components: {
    "font-awesome-icon": FontAwesomeIcon,
    headerBar,
    Graph
  },


}
import Graph from './views/Graph.vue';
import {faFilter, faSearch, faProjectDiagram, faChartPie, faListUl} from '@fortawesome/free-solid-svg-icons'

import headerBar from './components/header.vue'


library.add(faFilter, faSearch, faProjectDiagram, faChartPie, faListUl)
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
