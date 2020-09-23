<template>
  <v-navigation-drawer app>
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
    <v-card
      height="100%"
      width="256"
      class="mx-auto"
    >
      <v-navigation-drawer>

        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title">
              {{title}}
            </v-list-item-title>
            <v-list-item-subtitle>
             {{description}}
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>

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
      </v-navigation-drawer>
    </v-card>

  </v-navigation-drawer>
</template>

<script>
export default {
  name: "SideCard",
  title: "",
  description: "",
  selectedNode: undefined,
  neighborNodes:[],

  data(){
    return{
      selectedNode:this.selectedNode,
      neighborNodes: this.neighborNodes,
      title:this.title,
      description: this.description
    }
  },


  methods: {
    setSelectedNode: function (nodeId){
      this.$emit("nodeSelectionEvent",nodeId)
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
    setTitle: function (data){
      this.title=data.title;
      this.description=data.description
    },
  },

}
</script>

<style scoped>

</style>
