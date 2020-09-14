<template>
  <v-app>
    <headerBar/>
    <v-navigation-drawer app>
      <v-card
        height="400"
        width="256"
        class="mx-auto"
      >
        <v-navigation-drawer permanent>
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title class="title">
                Selection
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

    </v-navigation-drawer>


    <!-- Sizes your content based upon application components -->
    <v-main>
      <button v-on:click="updateGraph()">update Graph</button>
      <v-container fluid>
        <!--        <router-link :to="{name:'Graph'}">Load a Graph</router-link>-->
        <!--        <router-view></router-view>-->
        <Graph :payload="graphLoad" :key="graphKey"></Graph>
      </v-container>
    </v-main>

    <v-footer app>
      <!-- -->
    </v-footer>
  </v-app>
</template>

<script>
import {mapGetters} from "vuex";

import {library} from '@fortawesome/fontawesome-svg-core'
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'
import Graph from './views/Graph.vue';
import {
  faFilter, faSearch
} from '@fortawesome/free-solid-svg-icons'
import headerBar from './components/header.vue'

library.add(faFilter, faSearch)

export default {
  name: 'app',
  graphLoad: {},
  graphKey: 0,
  exampleGraph: undefined,
  data() {
    return {
      graphLoad: this.graphLoad,
      graphKey: 0
    }
  },
  methods: {
    setGraph: function (graph) {
      this.graphLoad = graph;
    },
    updateGraph: function () {
      if (!this.exampleGraph)
        this.getExampleGraph()
      else
        this.graphKey += 1
    },
    getExampleGraph: function () {
      this.$http.get("/getExampleGraph").then(response => {
        if (response.data) {
          console.log(response.data)
          this.exampleGraph = response.data
          this.setGraph(this.exampleGraph)
          this.graphKey += 1
        }
      }).catch(err => {
        console.log(err)
      })
    }
    // getGraph: function (){
    //   return {payload:this.graphLoad};
    // },
    // reloadGraph: function (){
    //
    // }
  },
  components: {
    "font-awesome-icon": FontAwesomeIcon,
    headerBar,
    Graph
  },
  created() {
    this.setGraph({message: "Default graph"})
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
