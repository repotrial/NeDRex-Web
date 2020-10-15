<template>
  <div>
    <v-container>
      <v-switch
        v-model="showAllLists"
        label="Show all Items"
        class="pa-3"
      ></v-switch>
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
              ref="nodeTab"
              dense
              v-model="selected.nodes[nodeTab]"
              class="elevation-1"
              :headers="headers('nodes',Object.keys(nodes)[nodeTab])"
              :items="showAllLists ? nodes[Object.keys(nodes)[nodeTab]]: selected.nodes[nodeTab]"
              item-key="id"
              show-select
            >
              <template v-slot:top>
                <v-switch
                  v-if="showAllLists"
                  v-on:click="selectAll('nodes',nodeTab)"
                  v-model="selectAllModel.nodes[nodeTab]"
                  label="Select All"
                  class="pa-3"
                ></v-switch>
              </template>
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
          <v-tabs
            next-icon="mdi-arrow-right-bold-box-outline"
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
            <v-data-table
              ref="edgeTab"
              fixed-header
              v-model="selected.edges[edgeTab]"
              dense
              class="elevation-1"
              :headers="headers('edges',Object.keys(edges)[edgeTab])"
              :items="showAllLists ? edges[Object.keys(edges)[edgeTab]] : selected.edges[edgeTab]"
              item-key="id"
              show-select
            >
              <template v-slot:top>
                <v-switch
                  v-show="showAllLists"
                  v-on:click="selectAll('edges',edgeTab)"
                  v-model="selectAllModel.edges[edgeTab]"
                  label="Select All"
                  class="pa-3"
                ></v-switch>
              </template>
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
      selected: {nodes: {}, edges: {}},
      selectAllModel: {nodes: {}, edges: {}},
      nodepage: {},
      showAllLists: true
    }
  },
  methods: {
    clearLists: function () {
      this.loadList(undefined)
      this.selected = {nodes: {}, edges: {}}
      this.selectAllModel = {nodes: {}, edges: {}}
      this.nodepage = {}
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
        this.selected["nodes"] = {}
        this.selected["edges"] = {}
        for (let ni = 0; ni < Object.keys(this.attributes.nodes).length; ni++) {
          this.selected.nodes[ni] = []
          this.selectAllModel.nodes[ni] = false
          this.nodepage[ni] = 0
        }
        for (let ei = 0; ei < Object.keys(this.attributes.edges).length; ei++) {
          this.selected.edges[ei] = []
          this.selectAllModel.edges[ei] = false
        }
        this.edges = data.edges;
        this.nodes = data.nodes;
        this.nodeTab = 0
        this.edgeTab = 0
      }
    },
    changedPage: function (number) {
      this.nodepage[this.nodeTab] = number;
      console.log("changed to page " + number)
    },
    printSelection: function () {
      console.log(this.selected)
    },
    selectAll: function (type, tab) {
      let data = {nodes: this.nodes, edges: this.edges}
      let model = this.selected[type][tab]
      let items = data[type][Object.keys(data[type])[tab]]
      model.length = 0
      if (this.selectAllModel[type][tab]) {
        items.forEach(item => {
          if (type === "nodes")
            this.$refs.nodeTab.select(item, true)
          else
            this.$refs.edgeTab.select(item, true)
          model.push(item)
        })
      } else {
        items.forEach(item => {
          if (type === "nodes")
            this.$refs.nodeTab.select(item, false)
          else
            this.$refs.edgeTab.select(item, false)
        })

      }
    },
    // selectNodeTab: function (nodeName){
    //   console.log("setting node tab= "+nodeName)
    //   let nodeTypes = Object.keys(this.attributes.nodes);
    //   for(let idx in nodeTypes){
    //     console.log("is nodeTypes.idx === nodeName? "+ (nodeTypes[idx] === nodeName))
    //     if(nodeTypes[idx] === nodeName)
    //       this.nodeTab=idx;
    //   }
    //   console.log(this.nodeTab+" -> "+nodeTypes[this.nodeTab])
    // },
    nodeDetails: function (nodeId) {
      this.$emit("selectionEvent", {type: "node", name: Object.keys(this.nodes)[this.nodeTab], id: nodeId})
    },
    edgeDetails: function (id1, id2) {
      this.$emit("selectionEvent", {type: "edge", name: Object.keys(this.edges)[this.edgeTab], id1: id1, id2: id2})
    },
    headers: function (entity, node) {
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
