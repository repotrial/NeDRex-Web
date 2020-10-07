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
            <v-simple-table fixed-header>
              <template v-slot:default>
                <thead>
                <tr>
                  <th v-for="attribute in attributes.nodes[Object.keys(nodes)[nodeTab]]" class="text-center">{{
                      attribute
                    }}
                  </th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="item in nodes[Object.keys(nodes)[nodeTab]]">
                  <td v-for="attribute in attributes.nodes[Object.keys(nodes)[nodeTab]]">{{ item[attribute] }}</td>
                </tr>
                </tbody>
              </template>
            </v-simple-table>
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
            <v-simple-table fixed-header>
              <template v-slot:default>
                <thead>
                <tr>
                  <th v-for="attribute in attributes.edges[Object.keys(edges)[edgeTab]]" class="text-center">{{
                      attribute
                    }}
                  </th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="item in edges[Object.keys(edges)[edgeTab]]">
                  <td v-for="attribute in attributes.edges[Object.keys(edges)[edgeTab]]">{{ item[attribute] }}</td>
                </tr>
                </tbody>
              </template>
            </v-simple-table>
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
      edgeTab: this.edgeTab
    }
  },
  methods: {
    loadList: function (data) {
      if (data === undefined) {
        this.attributes = {};
        this.edges = {};
        this.nodes = {};
      } else {
        this.attributes = data.attributes;
        this.edges = data.edges;
        this.nodes = data.nodes;
      }
    },
    getList: function (graphId) {
      this.$http.get("/getGraphList?id=" + graphId + "&cached=true").then(response => {
        this.loadList(response.data)
      }).catch(err => {
        console.log(err)
      })
    }
  }
}
</script>

<style scoped>

</style>
