<template>
  <div style="margin-top:20px">
    <template v-for="item in graphButtons">
      <v-btn :name=item.id :outlined="item.active" :dark="!item.active" v-on:click="loadGraph(item.id)"
             :color=item.color style="margin:5px">
        {{ item.label }}
      </v-btn>
    </template>
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
    test:"unset"
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
      for (let index in this.graphButtons) {
        if (this.graphButtons[index].id === id) {
          if (this.graphButtons[index].active)
            return;
          this.graphButtons[index].active = true
          // this.graphButtons[index].color = this.colors.buttons.graphs.active;
        } else
          this.graphButtons[index].active = false
        // this.graphButtons[index].color = this.colors.buttons.graphs.inactive;
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
