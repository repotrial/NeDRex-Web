<template>
  <v-navigation-drawer app right style="width: 30%">
    <v-list-item>
      <v-list-item-content>
        <v-navigation-drawer>
          <v-btn v-on:click="hide">
            <v-icon>fas fa-window-minimize</v-icon>
          </v-btn>
          <v-btn v-if="tabId === 1" v-on:click="setAllSelected()">
            <v-icon>fas fa-globe</v-icon>
          </v-btn>
        </v-navigation-drawer>
      </v-list-item-content>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title class="title">
          {{ title }}
        </v-list-item-title>
        <v-list-item-subtitle>
          {{ description }}
        </v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>
    <v-simple-table fixed-header v-if="tabId === 0" id="dropdown">
      <template v-slot:default>
        <thead>
        <tr>
          <th class="text-center">Type</th>
          <th class="text-center">Filter</th>
          <th class="text-center">Operation</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(item,index) in filters" :key="item.type+item.expression">
          <td>{{ item.type }}</td>
          <td>{{ item.expression }}</td>
          <td>
            <v-btn v-on:click="removeFilter(index)">
              <v-icon dense>fas fa-trash</v-icon>
            </v-btn>
          </td>
        </tr>
        <tr>
          <td>
            <v-select
              ref="typeSelect"
              v-model="filterTypeModel"
              :items="filterTypes"
              label="type"
              solo
            ></v-select>
          </td>
          <td>
            <v-text-field
              v-model="filterModel"
              :label="filterLabel"
              solo
            ></v-text-field>
          </td>
          <td>
            <v-btn v-on:click="saveFilter">
              <v-icon dense>fas fa-plus-circle</v-icon>
            </v-btn>
          </td>
        </tr>
        </tbody>
      </template>


    </v-simple-table>

    <v-simple-table fixed-header
                    v-if="tabId === 1 && (selectedNode !== undefined || (neighborNodes !== undefined && neighborNodes.length>0))">
      <template v-slot:default>
        <thead>
        <tr>
          <th class="text-center">ID</th>
          <th class="text-center">Label</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="selectedNode !== undefined" :key="selectedNode.id">
          <td><b>{{ selectedNode.id }}</b></td>
          <td><b>{{ selectedNode.title }}</b></td>
        </tr>
        <tr v-for="item in neighborNodes" :key="item.id" v-on:click="setSelectedNode(item.id)">
          <td>{{ item.id }}</td>
          <td>{{ item.title }}</td>
        </tr>
        </tbody>
      </template>
    </v-simple-table>


    <v-card v-if="tabId===2 && detailedObject !== undefined">
      <v-list class="transparent">
        <v-list-item
          v-for="item in Object.keys(detailedObject)"
          :key="item"
        >
          <v-list-item-title>{{ item }}</v-list-item-title>


          <v-list-item-subtitle class="text-right">
            {{ detailedObject[item] }}
          </v-list-item-subtitle>
          <!--          <v-list-item-icon>-->
          <!--            <v-icon>{{ item.icon }}</v-icon>-->
          <!--          </v-list-item-icon>-->
        </v-list-item>
      </v-list>


    </v-card>
    <!--    </v-card>-->
  </v-navigation-drawer>
</template>

<script>
export default {
  name: "SideCard",
  title: "",
  description: "",
  selectedNode: undefined,
  neighborNodes: [],
  filterView: false,
  filters: undefined,
  filterLabel: "",
  filterType: "",
  filterTypes: [],
  filterTypeModel: [],
  filterModel: "",
  filterName: "",
  tabId: 0,
  detailedObject: undefined,

  data() {
    return {
      selectedNode: this.selectedNode,
      neighborNodes: this.neighborNodes,
      title: this.title,
      description: this.description,
      filterView: this.filterView,
      filters: this.filters,
      filterLabel: this.filterLabel,
      filterTypes: this.filterTypes,
      filterTypeModel: this.filterTypeModel,
      filterModel: this.filterModel,
      tabId: this.tabId,
      detailedObject: this.detailedObject
    }
  },
  created() {
    this.filterView = false;
    this.filters = []
    this.filterTypes = ['startsWith', 'contain', 'match'
    ]
  },

  methods: {
    setAllSelected: function () {
      this.$emit("nodeSelectionEvent")
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
    loadFilter: function (data) {
      if (data !== undefined) {
        this.filterView = true;
        this.filters = data.filters
        if (this.filters === undefined)
          this.filters = []
        this.filterName = data.name
      } else
        this.filterView = false;

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
        this.$http.get("getEdgeDetails?name=" + data.name + "&id1=" + data.id1 + "&id2=" + data.id2).then(response => {
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
    hide: function () {
      this.$emit("hideEvent")
    },
    clearModels: function () {
      this.filterModel = ""
      this.filterTypeModel = ""
    },
    saveFilter: function () {
      let data = {type: this.filterTypeModel, expression: this.filterModel};
      let add = true
      this.filters.forEach(f => {
        if (f.type === this.filterTypeModel && f.expression === this.filterModel)
          add = false
      })
      if (add) {
        this.filters.push(data)
      }
      this.filterTypeModel = ""
      this.filterModel = ""
    },
    removeFilter: function (idx) {
      this.filters.splice(idx, 1)
      // this.$emit("removeFilterEvent", {name: this.filterName, index: idx})
    },
    setTitle: function (data) {
      this.title = data.title;
      this.description = data.description
      this.filterView = false
    },
    setTabId: function (tabId) {
      this.tabId = tabId;
      if (tabId === 0) {
        this.filterView = true
      } else if (tabId === 1) {

      }
    }
  },

}
</script>

<style scoped>

</style>
