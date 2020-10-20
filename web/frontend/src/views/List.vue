<template>
  <div>
    <v-container>
      <v-card>
        <v-row>
          <v-col>
            <v-switch
              v-model="showAllLists"
              label="Show all Items"
              class="pa-3"
            ></v-switch>
          </v-col>
          <v-col>
            <v-btn
              v-on:click="loadSelection"
            >
              Load Selection
              <v-icon>fas fa-project-diagram</v-icon>
            </v-btn>
          </v-col>
        </v-row>
      </v-card>
      <v-card style="margin:5px">
        <v-card-title v-on:mouseenter="nodeOptionHover=true" v-on:mouseleave="nodeOptionHover=false">Nodes
          <v-tooltip
            right>
            <template v-slot:activator="{ on, attrs }">
              <v-icon
                v-show="nodeOptionHover"
                size="17px"
                color="primary"
                dark
                v-bind="attrs"
                v-on="on"
                v-on:click="nodeOptions"
              >
                fas fa-cog
              </v-icon>
            </template>
            <span>options</span>
          </v-tooltip>
        </v-card-title>
        <i v-if="!update.nodes && Object.keys(nodes).length === 0">no node entries</i>
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
              fixed-header
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
        <v-card-title v-on:mouseenter="edgeOptionHover=true" v-on:mouseleave="edgeOptionHover=false">Edges
          <v-tooltip
            right>
            <template v-slot:activator="{ on, attrs }">
              <v-icon
                v-show="edgeOptionHover"
                size="17px"
                color="primary"
                dark
                v-bind="attrs"
                v-on="on"
                v-on:click="edgeOptions"
              >
                fas fa-cog
              </v-icon>
            </template>
            <span>options</span>
          </v-tooltip>
        </v-card-title>
        <i v-if="Object.keys(edges).length === 0">no edge entries</i>
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
    <v-dialog
      v-model="optionDialog"
      persistent
      max-width="500"
    >
      <!--      <template v-slot:activator="{ on, attrs }">-->
      <!--        <v-btn-->
      <!--          v-show="!visualize"-->
      <!--          color="primary"-->
      <!--          dark-->
      <!--          v-bind="attrs"-->
      <!--          v-on="on"-->
      <!--        >-->
      <!--          Visualize Graph?-->
      <!--        </v-btn>-->
      <!--      </template>-->
      <v-card v-if="options !== undefined && options.type !== undefined">
        <v-card-title class="headline">
          Organize {{ options.title }} Attributes
        </v-card-title>
        <v-card-text>Adjust the attributes of the general item tables.
        </v-card-text>
        <v-divider></v-divider>
        <template v-if="Object.keys(options.type).length>0">
          <v-tabs v-model="optionTab">
            <v-tabs-slider color="blue"></v-tabs-slider>
            <v-tab v-for="name in Object.keys(options.attributes)" :key="name">
              {{ Object.keys(nodes)[name] }}
            </v-tab>
          </v-tabs>
          <v-tabs-items>
            <v-list>
              <v-list-item v-for="attr in options.attributes[optionTab]" :key="attr.name">
                <v-switch v-model="attr.selected" :label="attr.name">
                </v-switch>
              </v-list-item>
            </v-list>
          </v-tabs-items>
        </template>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="green darken-1"
            text
            @click="dialogResolve(false)"
          >
            Cancel
          </v-btn>
          <v-btn
            color="green darken-1"
            text
            @click="dialogResolve(true)"
          >
            Apply
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
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
  gid: undefined,

  created() {
    this.nodes = {}
    this.attributes = {}
    this.edges = {}
  },

  data() {
    return {
      edges: this.edges,
      nodes: this.nodes,
      // listAttributes: {},
      nodeTab: this.nodeTab,
      edgeTab: this.edgeTab,
      selected: {nodes: {}, edges: {}},
      selectAllModel: {nodes: {}, edges: {}},
      nodepage: {},
      showAllLists: true,
      nodeOptionHover: false,
      edgeOptionHover: false,
      optionDialog: false,
      options: {},
      optionTab: undefined,
      update: {nodes: false, edges: false}
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
      this.attributes = {};
      this.edges = {};
      this.nodes = {};
      this.nodeTab = 0
      this.edgeTab = 0
      if (data !== undefined) {
        console.log(data)
        this.selected["nodes"] = {}
        this.selected["edges"] = {}
        this.attributes = data.attributes;
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
    nodeOptions: function () {
      this.optionDialog = true;
      this.optionsTab = 0
      this.options["title"] = (Object.keys(this.nodes).length > 1) ? "Nodes" : "Node"

      this.options["attributes"] = {}
      for (let node in Object.keys(this.attributes.nodes)) {
        let models = []
        this.attributes.nodes[Object.keys(this.attributes.nodes)[node]].forEach(attr => {
          models.push({name: attr.name, selected: attr.list})
        })
        this.options.attributes[node] = models;
      }
      console.log(this.options)
      this.options["type"] = "nodes";
    },
    edgeOptions: function () {
      this.optionDialog = true;
      this.optionsTab = 0
      this.options["title"] = (Object.keys(this.edges).length > 1) ? "Edges" : "Edge"

      this.options["attributes"] = {}
      for (let edge in Object.keys(this.attributes.edges)) {
        let models = []
        this.attributes.edges[Object.keys(this.attributes.edges)[edge]].forEach(attr => {
          models.push({name: attr.name, selected: attr.list})
        })
        this.options.attributes[edge] = models;
      }
      console.log(this.options)
      this.options["type"] = "edges";
    },
    dialogResolve(apply) {
      this.optionDialog = false;
      if (!apply) {
        return;
      }
      let comparison = this.attributes[this.options.type];

      let reloadNeeded = [];
      for (let index in this.options.attributes) {
        let name = Object.keys(comparison)[index]
        for (let attrIndex in this.options.attributes[index]) {
          if (this.options.attributes[index][attrIndex].selected && !comparison[name][attrIndex].sent) {
            reloadNeeded.push(name)
            break;
          }
          comparison[name][attrIndex].list = this.options.attributes[index][attrIndex].selected;
        }
      }
      if (reloadNeeded.length > 0) {
        let payload = {id: this.gid, attributes: {nodes: {}, edges: {}}}
        for (let index in this.options.attributes) {
          let name = Object.keys(comparison)[index]
          if (reloadNeeded.indexOf(name) === -1)
            continue
          let attrs = []
          payload.attributes[this.options.type][name] = attrs
          for (let attrIndex in this.options.attributes[index]) {
            if (this.options.attributes[index][attrIndex].selected)
              attrs.push(this.options.attributes[index][attrIndex].name)
          }
        }
        this.$http.post("/getCustomGraphList", payload).then(response => {
          if (response.data !== undefined)
            return response.data
        }).then(data => {
            //TODO just add new data?
          if (this.options.type === "nodes") {
            for (let name in data.nodes) {
              this.attributes.nodes[name] = data.attributes.nodes[name]
              this.nodes[name] = data.nodes[name]
            }
            this.update.nodes = true;
          } else {
            for (let name in data.edges) {
              this.attributes.edges[name] = data.attributes.edges[name]
              this.edges[name] = data.edges[name]
            }
            this.update.edges = true
          }
        }).catch(err => {
          console.log(err)
        })
      } else {
        this.update.nodes = true;
      }

    },
    changedPage: function (number) {
      this.nodepage[this.nodeTab] = number;
      console.log("changed to page " + number)
    },
    loadSelection: function () {
      let update = {id: this.gid, nodes: {}, edges: {}}
      Object.keys(this.selected.nodes).forEach(n => {
        let type = Object.keys(this.nodes)[n]
        update.nodes[type] = []
        this.selected.nodes[n].forEach(node => update.nodes[type].push(node.id))
      })
      Object.keys(this.selected.edges).forEach(e => {
        let type = Object.keys(this.edges)[e]
        update.edges[type] = []
        this.selected.edges[e].forEach(edge => update.edges[type].push(edge.id))
      })
      this.$http.post("/updateGraph", update).then(response => {
        if (response.data !== undefined)
          return response.data;
      }).then(info => {
        this.$emit("updateInfo", info)
      }).catch(err => {
        console.log(err)
      })
    },
    selectAll: function (type, tab) {
      //TODO maybe find different way to store selection (lagging on >1000 objects already)
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
        if (!attr.list)
          return
        let name = attr.name
        if (name === "id")
          return;
        if (name === "sourceId" || name === "idOne") {
          out.push({text: "EdgeId", align: 'start', sortable: false, value: "edgeid"})
        }
        out.push({text: name, align: 'start', sortable: attr.numeric, value: name})
      })
      this.update[entity] = false;
      return out
    },
    getList: function (gid) {
      this.clearLists()
      this.$http.get("/getGraphList?id=" + gid + "&cached=true").then(response => {
        this.loadList(response.data)
      }).then(() => {
        this.gid = gid;
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
