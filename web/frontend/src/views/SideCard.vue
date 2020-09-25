<template>
  <v-navigation-drawer app right style="width:30%">
    <!--      <v-card-->
    <!--        height="200"-->
    <!--        width="256"-->
    <!--        class="mx-auto"-->
    <!--      >-->
    <!--        <v-navigation-drawer permanent>-->
    <!--          <v-list-item>-->
    <!--            <v-list-item-content>-->
    <!--              <v-list-item-title class="title">-->
    <!--                Selection Tools-->
    <!--              </v-list-item-title>-->
    <!--              <v-list-item-subtitle>-->
    <!--                discover the graph-->
    <!--              </v-list-item-subtitle>-->
    <!--            </v-list-item-content>-->
    <!--          </v-list-item>-->

    <!--          <v-divider></v-divider>-->

    <!--          <v-list-->
    <!--            dense-->
    <!--            nav-->
    <!--          >-->
    <!--            <v-list-item>-->
    <!--              <v-list-item-icon>-->
    <!--                <v-icon>fas fa-filter</v-icon>-->
    <!--              </v-list-item-icon>-->

    <!--              <v-list-item-content>-->
    <!--                <v-list-item-title>Apply Filter</v-list-item-title>-->
    <!--              </v-list-item-content>-->
    <!--            </v-list-item>-->
    <!--            <v-list-item>-->
    <!--              <v-list-item-icon>-->
    <!--                <v-icon>fas fa-search</v-icon>-->
    <!--              </v-list-item-icon>-->

    <!--              <v-list-item-content>-->
    <!--                <v-list-item-title>Search</v-list-item-title>-->
    <!--              </v-list-item-content>-->
    <!--            </v-list-item>-->
    <!--          </v-list>-->
    <!--        </v-navigation-drawer>-->
    <!--      </v-card>-->
    <!--    <v-card-->
    <!--      height="100%"-->
    <!--      width="100%"-->
    <!--      class="mx-auto"-->
    <!--    >-->
    <!--      <v-navigation-drawer>-->

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
    <v-simple-table fixed-header v-if="filterView" id="dropdown">
      <template v-slot:default>
        <thead>
        <tr>
          <th class="text-center">Type</th>
          <th class="text-center">Filter</th>
          <th class="text-center">Operation</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="item in filters" :key="item.type+item.filter">
          <td>{{ item.type }}</td>
          <td>{{ item.expression }}</td>
          <td>
            <v-btn>
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
            <v-btn v-on:click="addFilter">
              <v-icon>fas fa-plus-circle</v-icon>
            </v-btn>
          </td>
        </tr>
        </tbody>
      </template>


    </v-simple-table>
    <v-simple-table fixed-header v-if="selectedNode !== undefined">
      <template v-slot:default>
        <thead>
        <tr>
          <th class="text-center">ID</th>
          <th class="text-center">Label</th>
        </tr>
        </thead>
        <tbody>
        <tr :key="selectedNode.id">
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
    <!--      </v-navigation-drawer>-->
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
  filterModel:"",
  filterName:"",

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
      filterModel:this.filterModel
    }
  },
  created() {
    this.filterView = false;
    this.filters = []
    this.filterTypes = ['startsWith', 'contain', 'match'
    ]
  },

  methods: {
    setSelectedNode: function (nodeId) {
      this.$emit("nodeSelectionEvent", nodeId)
    },
    loadSelection: function (params) {
      if (params) {
        this.selectedNode = params.primary;
        this.neighborNodes = params.neighbors;
      } else {
        this.selectedNode = undefined;
        this.neighborNodes = [];
      }
    },
    loadFilter: function (data) {
      console.log(data)
      if (data !== undefined) {
        this.filterView = true;
        this.filters = data.filters
        if(this.filters === undefined)
          this.filters=[]
        this.filterName=data.name
      } else
        this.filterView = false;

    },
    addFilter:function (){
      let data = {type:this.filterTypeModel,expression:this.filterModel};
      this.filters.push(data)
      this.$emit("addFilterEvent",{name:this.filterName,filter:data})
    },
    setTitle: function (data) {
      this.title = data.title;
      this.description = data.description
      this.filterView=false
    },
  },

}
</script>

<style scoped>

</style>
