<template>
  <div style="margin-top:20px">
    <v-container id="dropdown-example">
      <v-card class="mx-auto">
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title">
              Predefined Graphs
            </v-list-item-title>
            <v-list-item-subtitle>
              Start exploring by choosing a start example
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
        <v-divider></v-divider>
        <v-btn-toggle v-model="text" tile group>
          <template v-for="item in graphButtons">
            <v-btn :name=item.id :outlined="item.active" v-on:click="loadGraph(item.id)"
                   :color=item.color style="margin:5px">
              {{ item.label }}
            </v-btn>
          </template>
        </v-btn-toggle>
      </v-card>
    </v-container>
  </div>
</template>

<script>
export default {
  name: "Start",
  graphButtons: [],
  props: {
    colors: {
      type: Object
    },
  },
  data() {
    return {
      graphButtons: this.graphButtons,
    }
  },
  created() {
    this.graphButtons = [
      {id: 0, label: "Default Graph", color: this.colors.buttons.graphs.inactive, active: true},
      {id: 1, label: "Cancer-Comorbidity Graph", color: this.colors.buttons.graphs.inactive, active: false},
      {id: 2, label: "Neuro-Drug Graph", color: this.colors.buttons.graphs.inactive, active: false}
    ]
  },
  methods: {

    loadGraph: function (id) {
      console.log(id)
      for (let index in this.graphButtons) {
        if (this.graphButtons[index].id === id) {
          if (this.graphButtons[index].active)
            return;
          this.graphButtons[index].active = true
        } else
          this.graphButtons[index].active = false
      }
      if (id === 0) {
        this.graphLoad = {name: "default"}
      } else if (id === 1) {
        this.graphLoad = {get: "/getExampleGraph1"}
      } else if (id === 2) {
        this.graphLoad = {
          post: {
            nodes: {
              disorder: {
                filters: [
                  {
                    type: "match",
                    expression: ".*((neur)|(prion)|(brain)|(enceph)|(cogni)).*"
                  }
                ]
              }, drug: {}
            },
            edges: {"DrugHasIndication": {}},
            connectedOnly: true
          }
        }
      }
      this.$emit("graphLoadEvent", this.graphLoad)
    },
  }
}
</script>

<style scoped>

</style>
