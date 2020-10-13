<template>
  <div>
    <v-container>
      <v-card style="margin:5px">
        <v-card-title>Nodes</v-card-title>
        <i v-show="Object.keys(nodes).length === 0">no node entries</i>
        <template v-if="Object.keys(nodes).length>0">
          <v-tabs next-icon="mdi-arrow-right-bold-box-outline"
                  prev-icon="mdi-arrow-left-bold-box-outline"
                  show-arrows
                  v-model="nodeTab"
          >
            <v-tabs-slider color="blue"></v-tabs-slider>
            <v-tab v-for="node in Object.keys(nodes)" :key="node">
              {{ node }}
            </v-tab>
          </v-tabs>
          <v-tabs-items>
            <v-data-table
              dense
              class="elevation-1"
              :headers="headers('nodes',Object.keys(nodes)[nodeTab])"
              :items="nodes[Object.keys(nodes)[nodeTab]]"
            >
              <template v-slot:item.displayName="{item}">
                <v-tooltip right>
                  <template v-slot:activator="{ on, attrs }">
                    <v-icon
                      color="primary"
                      dark
                      v-bind="attrs"
                      v-on="on"
                      v-on:click="nodeDetails(item.id)"
                    >
                      fas fa-info-circle
                    </v-icon>
                    {{ item.displayName }}
                  </template>
                  <span>id:{{ item.id }}</span>
                </v-tooltip>
              </template>
            </v-data-table>
          </v-tabs-items>
        </template>
      </v-card>

      <v-card style="margin:5px">
        <v-card-title>Edges</v-card-title>
        <i v-show="Object.keys(edges).length === 0">no edge entries</i>
        <template v-if="Object.keys(edges).length>0">
          <v-tabs next-icon="mdi-arrow-right-bold-box-outline"
                  prev-icon="mdi-arrow-left-bold-box-outline"
                  show-arrows
                  v-model="edgeTab"
          >
            <v-tabs-slider color="blue"></v-tabs-slider>
            <v-tab v-for="edge in Object.keys(edges)" :key="edge">
              {{ edge }}
            </v-tab>
          </v-tabs>
          <v-tabs-items>
            <v-data-table fixed-header
                          dense
                          class="elevation-1"
                          :headers="headers('edges',Object.keys(edges)[edgeTab])"
                          :items="edges[Object.keys(edges)[edgeTab]]"
            >
              <template v-slot:item.edgeid="{item}">
                <template v-if="item.sourceId !== undefined">
                  <v-icon
                    color="primary"
                    dark
                    v-on:click="edgeDetails(item.sourceId, item.targetId)"
                  >
                    fas fa-info-circle
                  </v-icon>
                  {{ item.sourceId }}
                  <v-icon>fas fa-long-arrow-alt-right</v-icon>
                  {{ item.targetId }}
                </template>
                <template v-else>
                  <v-icon
                    color="primary"
                    dark
                    v-on:click="edgeDetails(item.idOne, item.idTwo)"
                  >
                    fas fa-info-circle
                  </v-icon>
                  {{ item.idOne }}
                  <v-icon>fas fa-arrows-alt-h</v-icon>
                  {{ item.idTwo }}
                </template>
              </template>
            </v-data-table>
          </v-tabs-items>
        </template>
      </v-card>
    </v-container>
  </div>
</template>

<script>
export default {
  name: "List",
  nodes: {},
  edges: {},
  attributes: {},
  nodeTab: undefined,
  edgeTab: undefined,

  created() {
    this.nodes = {}
    this.attributes = {}
    this.edges = {}
  },

  data() {
    return {
      edges: this.edges,
      nodes: this.nodes,
      attributes: this.attributes,
      nodeTab: this.nodeTab,
      edgeTab: this.edgeTab,
    }
  },
  methods: {
    clearLists: function (){
      this.loadList(undefined)
    },
    loadList: function (data) {
      if (data === undefined) {
        this.attributes = {};
        this.edges = {};
        this.nodes = {};
        this.nodeTab = 0
        this.edgeTab = 0
      } else {
        console.log(data)
        this.attributes = data.attributes;
        this.edges = data.edges;
        this.nodes = data.nodes;
        this.nodeTab = 0
        this.edgeTab = 0
      }
    },
    nodeDetails: function (nodeId) {
      this.$emit("selectionEvent", {type: "node", name: Object.keys(this.nodes)[this.nodeTab], id: nodeId})
    },
    edgeDetails: function (id1, id2) {
      this.$emit("selectionEvent", {type: "edge", name: Object.keys(this.edges)[this.edgeTab], id1: id1, id2: id2})
    },
    headers: function(entity, node) {
      //TODO sortable for all floats
      console.log("entity:" + entity + ", attributes:" + node)
      let out = []
      this.attributes[entity][node].forEach(attr => {
        if (attr === "id")
          return;
        if (attr === "sourceId" || attr === "idOne") {
          out.push({text: "EdgeId", align: 'start', sortable: false, value: "edgeid"})
        }
        out.push({text: attr, align: 'start', sortable: false, value: attr})
      })
      return out
    },
    getList: function (graphId) {
      this.clearLists()
      this.$http.get("/getGraphList?id=" + graphId + "&cached=true").then(response => {
        this.loadList(response.data)
      }).then(() => {
        this.$emit("finishedEvent")
      }).catch(err => {
        console.log(err)
      })
    }
  }
}
</script>

<style scoped>

</style>
