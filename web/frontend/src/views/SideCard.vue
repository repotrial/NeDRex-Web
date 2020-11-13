<template>
  <v-container style="position: fixed; width: 25%">

    <v-card elevation="3" style="padding-top: 15px; overflow-y: auto; max-height: 75vh">
      <v-card ref="options" elevation="3" style="margin:15px">

        <v-list-item @click="show.options=!show.options">
          <v-list-item-title>
            <v-icon left>{{ show.options ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
            Options
          </v-list-item-title>
        </v-list-item>
        <v-divider></v-divider>
        <v-list>
          <v-container v-if="show.options">
            <template v-if="selectedTab===0">
              <template v-if="options.start!==undefined">
                <v-list-item>
                  <v-chip outlined @click="$emit('applyEvent')">
                    <v-icon left>far fa-check-circle</v-icon>
                    Apply Selection
                  </v-chip>
                </v-list-item>
                <v-list-item>
                  <v-switch v-model="options.start.onlyConnected" label="Hide unconnected"></v-switch>
                </v-list-item>
                <v-list-item>
                  <v-switch v-model="options.start.skipVis" label="Skip visualisation"></v-switch>
                </v-list-item>
              </template>
              <v-progress-circular v-else>
              </v-progress-circular>
            </template>
            <template v-if="selectedTab===1">
              <template v-if="options.graph.visualized">
                <v-list-item>
                  <v-chip outlined v-on:click="setAllSelected()">
                    <v-icon left>fas fa-globe</v-icon>
                    Overview
                  </v-chip>
                </v-list-item>
                <v-list-item>
                  <v-switch
                    v-model="options.graph.physics"
                    @click="$emit('updatePhysicsEvent')"
                    label="Enable physics"
                  >
                  </v-switch>
                </v-list-item>
              </template>
              <template v-else>
                <v-chip outlined @click="visualizeGraph">
                  <v-icon left>fas fa-check</v-icon>
                  Visualize Graph
                </v-chip>
              </template>
            </template>
          </v-container>
        </v-list>
      </v-card>

      <v-card ref="filters" elevation="3" style="margin:15px" v-if="selectedTab===0">

        <v-list-item @click="show.filters=!show.filters">
          <v-list-item-title>
            <v-icon left>{{ show.filters ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
            Filters
          </v-list-item-title>
        </v-list-item>
        <v-divider></v-divider>

        <v-container v-show="show.filters">
          <v-row>
            <v-col>
              <v-list-item>
                <v-chip outlined
                        @click="show.filterAdd=!show.filterAdd; show.filterSelectDisabled=false, filterEntity=''">
                  <v-icon left>{{ show.filterAdd ? "fas fa-check" : "fas fa-filter" }}</v-icon>
                  {{ show.filterAdd ? "Done" : "Edit Filters" }}
                </v-chip>
              </v-list-item>
            </v-col>
            <v-col>
              <v-select v-if="show.filterAdd "
                        v-model="filterEntity"
                        :items="options.start.selectedElements"
                        :autofocus="show.filterAdd"
                        item-text="name"
                        item-value="name"
                        @focusout="show.filterSelectDisabled=true"
                        :disabled="show.filterSelectDisabled"
                        @change="setFiltering"
              >Select Entity
              </v-select>
            </v-col>
          </v-row>
          <v-simple-table fixed-header ref="filterTable" v-if="filterEntity.length>0">
            <template v-slot:default>
              <thead>
              <tr>
                <th class="text-center">Type</th>
                <th class="text-center">Filter</th>
                <th class="text-center">Operation</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="(item,index) in filters[filterEntity]" :key="item.type+item.expression">
                <td>{{ item.type }}</td>
                <td>{{ item.expression }}</td>
                <td>
                  <v-chip outlined v-on:click="removeFilter(index)">
                    <v-icon dense>fas fa-trash</v-icon>
                  </v-chip>
                </td>
              </tr>
              <tr v-if="show.filterAdding">
                <td>
                  <v-select
                    v-model="filterTypeModel"
                    :items="filterTypes"
                    label="type"
                  ></v-select>
                </td>
                <td>
                  <v-text-field
                    v-model="filterModel"
                    :label="filterLabel"
                    placeholder="Pattern"
                  ></v-text-field>
                </td>
                <td>
                  <v-chip outlined v-on:click="saveFilter"
                          :disabled="filterModel ===undefined|| filterModel.length ===0 ||filterTypeModel ===undefined">
                    <v-icon dense>fas fa-plus</v-icon>
                  </v-chip>
                </td>
              </tr>
              </tbody>
            </template>
          </v-simple-table>
        </v-container>
      </v-card>


      <v-card ref="legend" elevation="3" style="margin:15px" v-if="(selectedTab===1 && options.graph.visualized)">
        <v-list-item @click="show.legend=!show.legend">
          <v-list-item-title>
            <v-icon left>{{ show.legend ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
            Legend
          </v-list-item-title>
        </v-list-item>
        <v-divider></v-divider>


      </v-card>

      <v-card ref="info" elevation="3" style="margin:15px" v-if="(selectedTab===1 && options.graph.visualized)">

        <v-list-item @click="show.info=!show.info">
          <v-list-item-title>
            <v-icon left>{{ show.info ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
            Information
          </v-list-item-title>
        </v-list-item>
        <v-divider></v-divider>

        <v-container v-show="show.info">
          <v-card-text>Select a node to view its neighborhood. Double click the currently selected node to change to
            detail view.
          </v-card-text>
          <v-simple-table fixed-header height="300px" dense
                          v-if="selectedTab === 1 && (selectedNode !== undefined || (neighborNodes !== undefined && neighborNodes.length>0))">
            <template v-slot:default>
              <thead>
              <tr>
                <th class="text-center">ID</th>
                <th class="text-center">Name</th>
              </tr>
              </thead>
              <tbody>
              <tr v-if="selectedNode !== undefined" :key="selectedNode.id" v-on:dblclick="nodeDetails(selectedNode.id)">
                <td><b>{{ selectedNode.id }}</b></td>
                <td><b>{{ selectedNode.label }}
                </b></td>
              </tr>
              <tr v-for="item in neighborNodes" :key="item.id" v-on:click="setSelectedNode(item.id)">
                <td>{{ item.id }}</td>
                <td>{{ item.label }}
                </td>
              </tr>
              </tbody>
            </template>
          </v-simple-table>
          <i v-else>no selection available</i>
        </v-container>
      </v-card>

      <v-card ref="detail" elevation="3" style="margin:15px" v-if="selectedTab===2 && detailedObject !== undefined">

        <v-list-item @click="show.detail=!show.detail">
          <v-list-item-title>
            <v-icon left>{{ show.detail ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
            Detail
          </v-list-item-title>
        </v-list-item>
        <v-divider></v-divider>


        <v-container v-if="show.detail">
          <v-list class="transparent">
            <v-list-item
              v-for="item in Object.keys(detailedObject)"
              :key="item"
            >
              <v-list-item-title>{{ item }}</v-list-item-title>
              <v-list-item-subtitle class="text-right">
                {{ detailedObject[item] }}
              </v-list-item-subtitle>
            </v-list-item>
          </v-list>
        </v-container>

      </v-card>

    </v-card>
  </v-container>

</template>

<script>
export default {
  props: {
    options: Object,
    selectedTab: Number,
    filters: Object,
  },
  name: "SideCard",
  title: "",
  description: "",
  selectedNode: undefined,
  neighborNodes: [],
  filterLabel: "",
  filterType: "",
  filterTypes: [],
  filterTypeModel: [],
  filterModel: "",
  filterName: "",
  detailedObject: undefined,

  data() {
    return {
      show: {
        options: true,
        filters: false,
        filterAdd: false,
        filterAdding: false,
        filterSelectDisabled: false,
        info: false,
        legend: false,
        detail: false,
      },
      selectedNode: this.selectedNode,
      neighborNodes: this.neighborNodes,
      title: this.title,
      description: this.description,
      filterLabel: this.filterLabel,
      filterTypes: this.filterTypes,
      filterTypeModel: this.filterTypeModel,
      filterModel: this.filterModel,
      detailedObject: this.detailedObject,
      filterEntity: "",
    }
  },
  created() {
    this.filterTypes = ['startsWith', 'contain', 'match'
    ]
    console.log(this.options)
  },

  methods: {
    print: function (message) {
      console.log(message)
    },
    setAllSelected: function () {
      this.$emit("nodeSelectionEvent")
    },
    setOptions: function (name, options) {
      this.options[name] = options;
      // this.$refs.options.$forceUpdate()
    },
    setSelectedNode: function (nodeId) {
      this.$emit("nodeSelectionEvent", nodeId)
    },
    loadSelection: function (params) {
      if (params !== undefined) {
        this.selectedNode = params.primary;
        this.neighborNodes = params.neighbors;
      } else {
        this.selectedNode = undefined;
        this.neighborNodes = [];
      }
    },
    visualizeGraph: function () {
      // this.options.graph.visualized = true;
      this.$emit('applyEvent');
      this.show.options = false;
      this.$nextTick(() => {
        this.show.options = true;
      })

    },
    setFiltering: function () {
      this.show.filterAdding = true;
    },
    loadFilter: function (data) {
      if (data !== undefined) {
        this.filters = data.filters
        if (this.filters === undefined)
          this.filters = []
        this.filterName = data.name
      }
    },
    nodeDetails: function (id) {
      let str = id.split("_")
      this.$emit("nodeDetailsEvent", {prefix: str[0], id: str[1]})
    },
    loadDetails: function (data) {
      if (data.type === "node") {
        this.$http.get("getNodeDetails?name=" + data.name + "&id=" + data.id).then(response => {
          if (response.data !== undefined) {
            this.detailedObject = response.data
            this.description = "for " + data.name + " " + this.detailedObject.displayName + " (id:" + this.detailedObject.id + ")"
          }
        }).catch(err => {
          console.log(err)
        })
      } else if (data.type === "edge") {
        this.$http.get("getEdgeDetails?name=" + data.name + "&id1=" + data.id1 + "&id2=" + data.id2 + "&gid=" + this.$cookies.get("gid")).then(response => {
          if (response.data !== undefined) {
            this.detailedObject = response.data
            if (this.detailedObject.sourceId !== undefined)
              this.description = "for " + data.name + " " + this.detailedObject.displayName + " (id:" + this.detailedObject.sourceId + "->" + this.detailedObject.targetId + ")"
            if (this.detailedObject.idOne !== undefined)
              this.description = "for " + data.name + " id:" + this.detailedObject.idOne + "<->" + this.detailedObject.idTwo
          }
        }).catch(err => {
          console.log(err)
        })
      }
    },
    clearModels: function () {
      this.filterModel = ""
      this.filterTypeModel = ""
    },
    saveFilter: function () {
      let data = {type: this.filterTypeModel, expression: this.filterModel};

      if (this.filters[this.filterEntity] === undefined)
        this.filters[this.filterEntity] = []

      if (this.filters[this.filterEntity].filter(f=>(f.type === this.filterTypeModel && f.expression === this.filterModel)).length===0) {
        this.filters[this.filterEntity].push(data)
      }
      this.filterTypeModel = ""
      this.filterModel = ""
    },
    removeFilter: function (idx) {
      this.filters[this.filterEntity].splice(idx, 1)
      this.$refs.filterTable.$forceUpdate()
    },
  },

}
</script>

<style scoped>

</style>
