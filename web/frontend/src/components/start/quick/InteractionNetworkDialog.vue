<template>
  <div v-show="model"
       style="z-index: 1001;position: fixed; top: 0; left: 0; width: 100%; height: 100%; display: flex; justify-content: center; background-color: rgba(0,0,0,0.45)">
    <v-card style="width:75vw; height: 75vh; align-self: center; margin-top: auto; margin-bottom: auto;z-index: 1002;">
      <v-card-title>
        Seed interaction network
        <div style="display: flex; justify-content: flex-end; margin-left: auto; ">
          <v-btn icon style="padding:1em" color="red darker" @click="close()">
            <v-icon size="2em">far fa-times-circle</v-icon>
          </v-btn>
        </div>
      </v-card-title>
      <div style="height: calc(100% - 120px)">
        <div v-if="this.edgeCount + this.nodeCount>5000" style="height: 100%; display: flex; justify-content: center"><i>This interaction network contains {{ this.nodeCount }} nodes
          and {{ this.edgeCount }} which is too much for the quick visualization of the network.</i>
        </div>
        <v-progress-circular v-else-if="loading" indeterminate></v-progress-circular>
        <template>
          <div v-if="this.edgeCount>1000"><i>The interaction network has {{this.edgeCount}}, it might take a moment to define a suitable layout for the network.</i></div>
        <VisNetwork ref="network" v-if="!loading && this.edgeCount + this.nodeCount<=5000" :nodes="nodes" :edges="edges"
                    :options="options"
                    style="position: sticky; height: calc(100% - 25px)"></VisNetwork>
        </template>
      </div>
      <v-divider></v-divider>

      <v-card-actions style="width: 100%;">
        <div style="width: 100%; display: flex;justify-content: center">
          <v-btn

            color="error"
            @click="close()"
          >
            Close
          </v-btn>
        </div>
      </v-card-actions>
    </v-card>
  </div>
</template>

<script>
import {DataSet} from "vue-vis-network";
import {Network} from "vue-vis-network";

export default {
  name: "InteractionNetworkDialog",
  components: {
    'VisNetwork': Network
  },
  data() {
    return {
      model: false,
      loading: true,
      nodes: undefined,
      edges: undefined,
      options: undefined,
      nodeCount: 0,
      edgeCount: 0,
    }
  },

  created() {
    this.options = this.$global.metagraph.options.options;
    this.options.physics.enabled = true
  },

  methods: {

    show: function (type, ids) {
      document.getElementsByTagName("html")[0].style.overflowY="hidden"
      this.model = true;
      this.$http.getInteractionEdges({type: type, ids: ids}).then(graph => {
        this.nodeCount = graph.nodes.length
        this.edgeCount = graph.edges.length
        if (this.nodeCount + this.edgeCount > 5000) {
          return
        }
        this.setEdges(graph.edges);
        this.setNodes(graph.nodes);
      }).then(() => {
        this.loading = false
      })
    },
    setEdges: function (edges) {
      if (edges != null)
        this.edges = new DataSet(edges);
    },
    setNodes: function (nodes) {
      if (nodes != null) {
        for (let i = 0; i < nodes.length; i++) {
          nodes[i].title = nodes[i].label
        }
        this.nodes = new DataSet(nodes);
      }
    },

    close: function () {
      document.getElementsByTagName("html")[0].style.overflowY="auto"
      this.model = false
      this.loading = true
      this.nodeCount=0
      this.edgeCount=0
    }
    // setOptions: function (options) {
    //   if (options != null) {
    //     this.options = options;
    //     this.reloadOptions()
    //   }
    // },
    //
    // reloadOptions: function () {
    //   if (this.$refs.network)
    //     this.$refs.network.setOptions(this.options)
    // },

  }

}
</script>

<style scoped>

</style>
