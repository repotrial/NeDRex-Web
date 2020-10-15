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
    <SideCard v-show="showSidecard" ref="side"
              v-on:nodeSelectionEvent="setSelectedNode"
              v-on:hideEvent="hideSidecard"
              v-on:removeFilterEvent="removeFilter"
              v-on:togglePhysicsEvent="togglePhysics"
              v-on:nodeDetailsEvent="nodeDetails"
    ></SideCard>
    <v-navigation-drawer app right v-show="!showSidecard" style="width: 5%">
      <v-list-item>
        <v-list-item-content>
          <v-navigation-drawer>
            <v-btn v-on:click="showSidecard=true">
              <v-icon>fas fa-caret-left</v-icon>
            </v-btn>
          </v-navigation-drawer>
        </v-list-item-content>
      </v-list-item>
    </v-navigation-drawer>

    <v-main app style="padding-top: 0" @click.stop="hideSidecard()">
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
<!--          <v-divider></v-divider>-->
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
<!--          <v-divider></v-divider>-->
          <v-list class="transparent">
            <v-list-item
              v-for="item in Object.keys(listWarnObject.edges)"
              :key="item"
            >
              <v-list-item-title>{{ item}}
              </v-list-item-title>
              <v-list-item-subtitle>
                {{ listWarnObject.edges[item] }}
              </v-list-item-subtitle>

            </v-list-item>
          </v-list>
          <v-divider></v-divider>
          <v-card-text>
            This exceeds the limit of elements that can handled by the browser, so please apply a more strict filter!
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
      <v-container v-show="selectedTabId===0" fluid>
        <Start v-if="metagraph !== undefined" ref="start" v-on:graphLoadEvent="loadGraph" v-on:filterEvent="filter"
               :colors="colors" :metagraph="metagraph"></Start>
      </v-container>
      <v-container v-show="selectedTabId===1" fluid>
        <Graph ref="graph"
               v-on:selectionEvent="loadSelection"
               v-on:finishedEvent="setTabNotification(1)"
               v-on:graphLoadedEvent="loadList"
               v-on:sizeWarningEvent="sizeWarning"
        ></Graph>
        <v-card>
          <v-checkbox v-model="physics" label="Enable physics" v-on:click="togglePhysics"></v-checkbox>
        </v-card>
      </v-container>
      <v-container v-show="selectedTabId===2" fluid>
        <List ref="list" v-on:finishedEvent="setTabNotification(2)" v-on:selectionEvent="loadDetails"></List>
      </v-container>
    </v-main>

    <v-footer app>
    </v-footer>
  </v-app>
</template>

<script>
import Graph from './views/Graph.vue';
import Start from './views/Start.vue';
import List from './views/List.vue'
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
  physics: false,
  metagraph: Object,

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
      physics: this.physics,
      metagraph: this.metagraph,
      listWarnObject: undefined,
      listDialog: false,
    }
  },
  created() {
    this.colors = {
      buttons: {graphs: {active: "deep-purple accent-2", inactive: undefined}},
      tabs: {active: "rgba(25 118 210)", inactive: "rgba(0,0,0,.54)"}
    }
    this.selectedTabId = 0;
    this.physics = false
    this.tabslist = [
      {id: 0, label: "Start", icon: "fas fa-filter", color: this.colors.tabs.active, note: false},
      {id: 1, label: "Graph", icon: "fas fa-project-diagram", color: this.colors.tabs.inactive, note: false},
      {id: 2, label: "List", icon: "fas fa-list-ul", color: this.colors.tabs.inactive, note: false},
      {id: 3, label: "History", icon: "fas fa-history", color: this.colors.tabs.inactive, note: false},
    ]
    this.initGraphs()

  },
  // this.setGraph({message: "Default graph"})
  methods: {
    initGraphs: function () {
      this.$http.get("/getMetagraph").then(response => {
        this.metagraph = response.data;
      }).catch(err => {
        console.log(err)
      })
    },
    sizeWarning: function (info) {
      this.listWarnObject = info;
      this.listDialog = true;
    },
    loadGraph: function (graph) {
      this.$refs.side.hide()
      this.tabslist[1].icon = "fas fa-circle-notch fa-spin"
      this.tabslist[2].icon = "fas fa-circle-notch fa-spin"
      if (this.physics) {
        this.physics = false;
        this.togglePhysics()
      }
      this.$refs.graph.loadData(graph)
    },
    loadList: function (graphId) {
      this.$refs.list.getList(graphId)
    },
    toggleSide: function () {
      if (this.showSidecard) {
        this.hideSidecard();
      }
      this.showSidecard = !this.showSidecard;
    },
    nodeDetails: function (data){
      let type = ""
      this.metagraph.nodes.forEach(n=>{
        if(n.group.startsWith(data.prefix))
          type = n.group;
      })
      this.selectTab(2)
      // this.$refs.list.selectNodeTab(type)
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
    togglePhysics: function () {
      this.$refs.graph.togglePhysics()
    },
    hideSidecard: function () {
      this.showSidecard = false
      this.$refs.start.minimizeSide()
      this.$refs.side.clearModels()
    },
    loadSelection: function (params) {
      this.showSidecard = true
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
    // addFilter: function (param) {
    //   this.$refs.start.addFilter(param)
    // },
    removeFilter: function (param) {
      this.$refs.start.removeFilter(param)
    },
    adaptSidecard: function (param) {
      this.$refs.side.setTabId(this.selectedTabId)
      if (this.selectedTabId === 1) {
        this.$refs.side.setTitle({title: "Current Selection", description: "Including neighbors of first degree"})
      } else if (this.selectedTabId === 0) {
        if (param !== undefined) {
          this.showSidecard = true
          this.$refs.side.setTitle({
            title: "Filter " + param.name,
            description: "Apply a filter for better exploration"
          })
          this.$refs.side.loadFilter(param)
        } else {
          this.$refs.side.loadFilter(undefined)
          this.hideSidecard()
        }
      } else if (this.selectedTabId === 2) {
        this.$refs.side.setTitle({title: "Detailed Information", description: "No item selected!"})
      }
    },
    loadDetails: function (params) {
      this.$refs.side.loadDetails(params)
      this.showSidecard = true;
    }
  },
  components: {
    headerBar,
    Graph,
    SideCard,
    Start,
    List
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
