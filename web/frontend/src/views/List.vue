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
              class="elevation-1"
              :headers="headers('nodes',Object.keys(nodes)[nodeTab])"
              :items="showAllLists ? nodes[Object.keys(nodes)[nodeTab]]: filterSelected(nodes[Object.keys(nodes)[nodeTab]])"
              item-key="id"
              :search="filters.nodes.query"
              :custom-filter="filterNode"
            >
              <template v-slot:top>
                <v-container>
                  <v-row>
                    <v-col
                      cols="2"
                      v-show="!filters.nodes.suggestions"
                    >
                      <v-select
                        :items="headerNames('nodes',Object.keys(nodes)[nodeTab])"
                        label="Attribute"
                        v-model="filters.nodes.attribute.name"
                        outlined
                      ></v-select>
                    </v-col>
                    <v-col
                      cols="2"
                      v-if="!filters.nodes.suggestions"
                    >
                      <v-select
                        v-model="filters.nodes.attribute.operator"
                        :items="operatorNames('nodes',Object.keys(nodes)[nodeTab],filters.nodes.attribute.name)"
                        label="Operator"
                        outlined
                      ></v-select>
                    </v-col>
                    <v-col
                      :cols="(filters.nodes.suggestions)?10:6"
                    >
                      <v-text-field
                        clearable
                        v-show="!filters.nodes.suggestions"
                        v-model="filters.nodes.query"
                        label="Query (case sensitive)"
                        class="mx-4"
                      ></v-text-field>
                      <v-autocomplete
                        clearable
                        :search-input.sync="findNodeSuggestions"
                        v-show="filters.nodes.suggestions"
                        :loading="suggestions.nodes.loading"
                        :items="suggestions.nodes.data"
                        v-model="filterNodeModel"
                        label="Query (case sensitive)"
                        class="mx-4"
                      >
                        <template v-slot:item="{ item }" v-on:select="suggestions.nodes.chosen=item">
                          <v-list-item-avatar
                          >
                            <v-icon v-if="item.type==='DOMAIN_ID'">fas fa-fingerprint</v-icon>
                            <v-icon v-if="item.type==='DISPLAY_NAME' || item.type==='SYMBOLS'">fas fa-tv</v-icon>
                            <v-icon v-if="item.type==='ICD10'">fas fa-disease</v-icon>
                            <v-icon v-if="item.type==='SYNONYM'">fas fa-sync</v-icon>
                            <v-icon v-if="item.type==='IUPAC'">mdi-molecule</v-icon>
                            <v-icon v-if="item.type==='ORIGIN'">fas fa-dna</v-icon>
                            <v-icon v-if="item.type==='DESCRIPTION' || item.type==='COMMENTS'">fas fa-info</v-icon>
                            <v-icon v-if="item.type==='INDICATION'">fas fa-pills</v-icon>
                            <v-icon v-if="item.type==='TYPE' || item.type==='GROUP' || item.type==='CATEGORY'">fas
                              fa-layer-group
                            </v-icon>
                          </v-list-item-avatar>
                          <v-list-item-content>
                            <v-list-item-title v-text="item.text"></v-list-item-title>
                            <v-list-item-subtitle
                              v-text="item.type"></v-list-item-subtitle>
                          </v-list-item-content>
                          <v-list-item-action>
                            <v-chip>
                              {{ item.ids.length }}
                            </v-chip>
                          </v-list-item-action>
                        </template>
                      </v-autocomplete>
                    </v-col>
                    <v-col
                      cols="2"
                    >
                      <v-switch
                        label="Suggestions"
                        v-model="filters.nodes.suggestions"
                        v-on:click="toggleSuggestions('nodes')"
                      >
                      </v-switch>
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <v-btn
                        v-if="showAllLists"
                        v-on:click="selectAll('nodes',nodeTab)"
                        v-model="selectAllModel.nodes[nodeTab]"
                        class="pa-3"
                      >Select All
                      </v-btn>
                    </v-col>
                    <v-col>
                      <v-btn
                        v-if="showAllLists"
                        v-on:click="deselectAll('nodes',nodeTab)"
                        v-model="selectAllModel.nodes[nodeTab]"
                        class="pa-3"
                      >Deselect All
                      </v-btn>
                    </v-col>
                  </v-row>
                </v-container>
              </template>
              <template v-slot:item.selected="{item}">
                <v-checkbox
                  :key="item.id"
                  v-model="item.selected"
                  hide-details
                  primary
                ></v-checkbox>
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
              dense
              class="elevation-1"
              :headers="headers('edges',Object.keys(edges)[edgeTab])"
              :items="showAllLists ? edges[Object.keys(edges)[edgeTab]] : filterSelected(edges[Object.keys(edges)[edgeTab]])"
              :search="filters.edges.query"
              :custom-filter="filterEdge"
              item-key="id"
            >
              <template v-slot:top>
                <v-container>
                  <v-row>
                    <v-col
                      cols="2"
                    >
                      <v-select
                        :items="headerNames('edges',Object.keys(edges)[edgeTab])"
                        label="Attribute"
                        v-model="filters.edges.attribute.name"
                        outlined
                      ></v-select>
                    </v-col>
                    <v-col
                      cols="2"
                      v-if="!filters.edges.suggestions"
                    >
                      <v-select
                        v-model="filters.edges.attribute.operator"
                        :items="operatorNames('edges',Object.keys(edges)[edgeTab],filters.edges.attribute.name)"
                        label="Operator"
                        outlined
                      ></v-select>
                    </v-col>
                    <v-col
                      cols="6"
                    >
                      <v-text-field
                        clearable
                        v-model="filters.edges.query"
                        label="Query (case sensitive)"
                        class="mx-4"
                      ></v-text-field>
                    </v-col>
                    <v-col
                      cols="2"
                    >
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <v-btn
                        v-if="showAllLists"
                        v-on:click="selectAll('edges',edgeTab)"
                        v-model="selectAllModel.edges[edgeTab]"
                        class="pa-3"
                      >Select All
                      </v-btn>
                    </v-col>
                    <v-col>
                      <v-btn
                        v-if="showAllLists"
                        v-on:click="deselectAll('edges',edgeTab)"
                        v-model="selectAllModel.edges[edgeTab]"
                        class="pa-3"
                      >Deselect All
                      </v-btn>
                    </v-col>
                  </v-row>
                </v-container>
              </template>
              <template v-slot:item.selected="{item}">
                <v-checkbox
                  :key="item.id"
                  v-model="item.selected"
                  hide-details
                  primary
                ></v-checkbox>
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
                <v-switch v-model="attr.selected" :label="attr.name" :disabled="(attr.name === 'id')">
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
    this.backup = {nodes: {}, edges: {}}
  },

  data() {
    return {
      edges: this.edges,
      nodes: this.nodes,
      nodeTab: this.nodeTab,
      edgeTab: this.edgeTab,
      selectAllModel: {nodes: {}, edges: {}},
      nodepage: {},
      showAllLists: true,
      nodeOptionHover: false,
      edgeOptionHover: false,
      optionDialog: false,
      options: {},
      optionTab: undefined,
      update: {nodes: false, edges: false},
      findNodeSuggestions: null,
      suggestions: {
        nodes: {model: undefined, data: [], loading: false, chosen: undefined},
        edges: {model: undefined, data: [], loading: false, chosen: undefined}
      },
      filters: {
        nodes: {query: '', attribute: {}, suggestions: false},
        edges: {query: '', attribute: {}, suggestions: false}
      },
      filterNodeModel: null,
    }
  },
  watch: {
    filterNodeModel: function (val) {
      this.applySuggestion(val)
    },
    // nodeTab: function (val) {
    //   this.applySuggestion(undefined)
    //   this.findNodeSuggestions = null;
    // },
    findNodeSuggestions: function (val) {
      let type = "nodes";
      let name = Object.keys(this.nodes)[this.nodeTab];
      if (this.suggestions[type].chosen !== undefined)
        return
      this.suggestions[type].loading = true;
      this.suggestions[type].data = []
      this.$nextTick().then(() => {
        this.$http.post("getSuggestions", {
          gid: this.gid,
          type: type,
          name: name,
          query: val,
        }).then(response => {
          if (response.data !== undefined) {
            return response.data
          }
        }).then(data => {
          this.suggestions[type].data = data.suggestions;
        }).catch(err =>
          console.log(err)
        ).finally(() =>
          this.suggestions[type].loading = false
        )
      }).catch(err =>
        console.log(err))
    },
  },
  methods: {
    filterSelected(items) {
      return items.filter(item => item.selected)
    },
    clearLists: function () {
      this.loadList(undefined)
      this.selectAllModel = {nodes: {}, edges: {}}
      this.nodepage = {}
    }
    ,
    loadList: function (data) {
      this.attributes = {};
      this.edges = {};
      this.nodes = {};
      this.nodeTab = 0
      this.edgeTab = 0
      if (data !== undefined) {
        this.attributes = data.attributes;
        for (let ni = 0; ni < Object.keys(this.attributes.nodes).length; ni++) {
          this.selectAllModel.nodes[ni] = false
          this.nodepage[ni] = 0
        }
        for (let ei = 0; ei < Object.keys(this.attributes.edges).length; ei++) {
          this.selectAllModel.edges[ei] = false
        }
        this.edges = data.edges;
        this.nodes = data.nodes;
        Object.keys(this.nodes).forEach(node => {
          for (let idx in this.nodes[node]) {
            this.nodes[node][idx]["selected"] = false;
          }
        })
        Object.keys(this.edges).forEach(edge => {
          for (let idx in this.edges[edge]) {
            this.edges[edge][idx]["selected"] = false;
          }
        })
        this.nodeTab = 0
        this.edgeTab = 0
      }
    }
    ,
    applySuggestion: function (val) {
      if (val !== undefined) {
        let nodes = this.nodes[Object.keys(this.nodes)[this.nodeTab]]
        this.suggestions.nodes.chosen = this.suggestions.nodes.data.filter(item => item.value === val).flatMap(item => item.ids);
        this.backup.nodes[this.nodeTab] = nodes
        this.nodes[Object.keys(this.nodes)[this.nodeTab]] = nodes.filter((i, k) => this.suggestions.nodes.chosen.indexOf(i.id) !== -1)
        this.filters.nodes.query = val
      } else {
        this.nodes[Object.keys(this.nodes)[this.nodeTab]] = this.backup.nodes[this.nodeTab]
        this.suggestions.nodes.chosen = undefined
      }
    },
    toggleSuggestions: function (type) {
      if (!this.filters[type].suggestions) {
        this.filters[type].query = ""
      }
    }
    ,
    filterNode: function (value, search, item) {
      if (!this.filters.nodes.suggestions) {
        let attribute = this.filters.nodes.attribute.name
        return this.filter(item[attribute], search, item, attribute, this.filters.nodes.attribute.operator)
      } else {
        return true;
      }
    }
    ,
    filterEdge: function (value, search, item) {
      let attribute = this.filters.edges.attribute.name
      return this.filter(item[attribute], search, item, attribute, this.filters.edges.attribute.operator)
    },
    filter: function (value, search, item, attribute, operator) {
      if ((attribute === undefined || attribute.length === 0) || (operator === undefined || operator.length === 0))
        return false;
      if (value !== undefined && value != null && typeof value === "object") {
        value.forEach(el => {
          if (this.filter(el, search, item, attribute, operator))
            return true
        })
        return false;
      }
      switch (operator) {
        //TODO between case????
        case "empty":
          return value === undefined || value == null || value === ""
        case  "null":
          return value === undefined || value == null || value === ""
        case "=":
          return value == search
        case "!=":
          return value != search
        case "<":
          return value < Number(search)
        case "<=":
          return value <= Number(search)
        case ">=":
          return value >= Number(search)
        case ">":
          return value >= Number(search)
        case "equals":
          return value === search
        case "contains":
          return value !== undefined && value != null && value.indexOf(search) !== -1
        case "matches":
          return value !== undefined && value != null && value.match(search) != null
        default:
          return false
      }

    },
    operatorNames: function (type, name, attribute) {
      let headers = this.headers(type, name);
      let out = []
      headers.forEach(attr => {
        if (attr.text === attribute) {
          if (attr.numeric) {
            out = ["=", "!="]
            if (!attr.id)
              ["<", "<=", ">", ">=", "<>", "null"].forEach(o => out.push(o))
          } else {
            out = ["equals"]
            if (!attr.id)
              ["contains", "matches", "empty"].forEach(o => out.push(o))
          }
        }
      })
      return out;

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
    }
    ,
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
    }
    ,
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

    }
    ,
    loadSelection: function () {
      let update = {id: this.gid, nodes: {}, edges: {}}
      for (let type in this.nodes) {
        update.nodes[type] = []
        this.nodes[type] = this.filterSelected(this.nodes[type])
        this.nodes[type].forEach(node => update.nodes[type].push(node.id))
      }
      for (let type in this.edges) {
        update.edges[type] = []
        this.edges[type] = this.filterSelected(this.edges[type])
        this.edges[type].forEach(edge => update.edges[type].push(edge.id))
      }
      this.filters.nodes.suggestions = false;
      this.filterNodeModel = null
      this.$http.post("/updateGraph", update).then(response => {
        if (response.data !== undefined)
          return response.data;
      }).then(info => {
        this.$emit("updateInfo", info)
      }).catch(err => {
        console.log(err)
      })
    }
    ,
    selectAll: function (type, tab) {
      let data = {nodes: this.nodes, edges: this.edges}
      let items = data[type][Object.keys(data[type])[tab]]

      let filterActive = this.filters[type].attribute.name !== undefined && this.filters[type].attribute.name.length > 0 && this.filters[type].query !== null && this.filters[type].query.length > 0 && this.filters[type].attribute.operator !== undefined && this.filters[type].attribute.operator.length > 0
      items.forEach(item => {
          if (type === "nodes") {
            if (!filterActive || (filterActive && this.filterNode(undefined, this.filters[type].query, item)))
              item.selected = true;
          } else if (!filterActive || (filterActive && this.filterEdge(undefined, this.filters[type].query, item)))
            item.selected = true
        }
      )
      this.$nextTick().then(() => {
        if ("nodes" === type)
          this.$refs.nodeTab.$forceUpdate()
        else
          this.$refs.edgeTab.$forceUpdate()
      })
    }
    ,
    deselectAll: function (type, tab) {
      let data = {nodes: this.nodes, edges: this.edges}
      let items = data[type][Object.keys(data[type])[tab]]
      let filterActive = this.filters[type].attribute.name !== undefined && this.filters[type].attribute.name.length > 0 && this.filters[type].query !== null && this.filters[type].query.length > 0 && this.filters[type].attribute.operator !== undefined && this.filters[type].attribute.operator.length > 0
      items.forEach(item => {
        if (type === "nodes") {
          if (!filterActive || (filterActive && this.filterNode(undefined, this.filters[type].query, item)))
            item.selected = false;
        } else if (!filterActive || (filterActive && this.filterEdge(undefined, this.filters[type].query, item)))
          item.selected = false;

      })
      this.$nextTick().then(() => {
        if ("nodes" === type)
          this.$refs.nodeTab.$forceUpdate()
        else
          this.$refs.edgeTab.$forceUpdate()
      })
    }
    ,
    nodeDetails: function (nodeId) {
      this.$emit("selectionEvent", {type: "node", name: Object.keys(this.nodes)[this.nodeTab], id: nodeId})
    }
    ,
    edgeDetails: function (id1, id2) {
      this.$emit("selectionEvent", {type: "edge", name: Object.keys(this.edges)[this.edgeTab], id1: id1, id2: id2})
    }
    ,
    headers: function (entity, node) {
      let out = [{text: "select", align: 'start', sortable: false, value: "selected"}]
      this.attributes[entity][node].forEach(attr => {
        if (!attr.list)
          return
        let name = attr.name
        if (name === "id")
          return;
        if (name === "sourceId" || name === "idOne") {
          out.push({
            text: "EdgeId",
            align: 'start',
            sortable: false,
            value: "edgeid",
            list: false,
            id: true,
            numeric: true
          })
        }
        out.push({
          text: name,
          align: 'start',
          sortable: attr.numeric,
          list: attr.array,
          value: name,
          id: attr.id,
          numeric: attr.numeric
        })
      })
      this.update[entity] = false;
      return out
    }
    ,
    headerNames: function (entity, node) {
      let headers = this.headers(entity, node);
      let out = []
      headers.forEach(header => out.push(header.text))
      let idx = out.indexOf("select")
      if (idx > -1)
        out.splice(idx, 1)
      return out;
    }
    ,
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
