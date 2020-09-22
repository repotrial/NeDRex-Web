<template>
  <div style="margin-top:20px">
    <v-container>
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
        <v-btn-toggle tile group>
          <template v-for="item in graphButtons">
            <v-btn :name=item.id :outlined="item.active" v-on:click="loadGraph(item.id)"
                   :color=item.color style="margin:5px">
              {{ item.label }}
            </v-btn>
          </template>
        </v-btn-toggle>
      </v-card>
    </v-container>
    <v-container>
      <v-card class="mx-auto">
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title">
              Customized Exploration
            </v-list-item-title>
            <v-list-item-subtitle>
              Query a specified starting graph.
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
        <v-divider></v-divider>

        <v-list-item style="align-content: center">
          <v-container>
            <v-row>
              <v-col>
                <v-list-item-group multiple color="indigo" v-model="nodeModel">
                  <v-list-item v-for="item in nodes" :name=item.id v-on:click="toggleNode(item.id)" :key="item.id"
                               :selectable="nodeModel.indexOf(item.id)===-1">
                    <v-list-item-content>
                      <v-list-item-title v-text="item.label"></v-list-item-title>
                    </v-list-item-content>
                  </v-list-item>
                </v-list-item-group>
              </v-col>
              <v-col>
                <v-list-item-group  multiple color="indigo" v-model="edgeModel">
                  <v-list-item v-for="item in edges" :name=item.id v-on:click="toggleEdge(item.id)" :key="item.id"
                               :selectable="edgeModel.indexOf(item.id)===-1">
                    <v-list-item-content>
                      <v-list-item-title v-text="item.label"></v-list-item-title>
                    </v-list-item-content>
                  </v-list-item>
                </v-list-item-group>
              </v-col>
            </v-row>
          </v-container>
        </v-list-item>
        <v-simple-table>
          <template v-slot:default>
            <thead>
            <tr>
              <th class="text-center">Type</th>
              <th class="text-center">Name</th>
              <th class="text-center">Filter</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="item in selectedElements" :key="item.name">
              <td>{{ item.type }}</td>
              <td>{{ item.name }}</td>
              <td>{{ item.filter }}</td>
            </tr>
            </tbody>
          </template>

        </v-simple-table>
      </v-card>
    </v-container>
  </div>
</template>

<script>
export default {
  name: "Start",
  graphButtons: [],
  selectedElements: [],
  nodes: [],
  edges: [],
  nodeModel: [],
  edgeModel: [],
  props: {
    colors: {
      type: Object
    },
  },
  data() {
    return {
      graphButtons: this.graphButtons,
      selectedElements: this.selectedElements,
      nodes: this.nodes,
      edges: this.edges,
      nodeModel: this.nodeModel,
      edgeModel: this.edgeModel,
    }
  },
  created() {
    this.selectedElements = []
    this.nodeModel = []
    this.edgeModel = []
    this.graphButtons = [
      {id: 0, label: "Default Graph", color: this.colors.buttons.graphs.inactive, active: true},
      {id: 1, label: "Cancer-Comorbidity Graph", color: this.colors.buttons.graphs.inactive, active: false},
      {id: 2, label: "Neuro-Drug Graph", color: this.colors.buttons.graphs.inactive, active: false}
    ]
    this.nodes = [
      {id: 0, label: "Drug"},
      {id: 1, label: "Gene"},
      {id: 2, label: "Protein"},
      {id: 3, label: "Disorder"},
      {id: 4, label: "Pathway"}
    ]
    this.edges = [
      {id: 0, label: "DrugHasTargetGene", selected: false, depends: [0, 1]},
      {id: 1, label: "DrugHasTargetProtein", selected: false, depends: [0, 2]},
      {id: 2, label: "DrugHasIndication", selected: false, depends: [0, 3]},
      {id: 3, label: "GeneAssociatedWithDisorder", selected: false, depends: [1, 3]},
      {id: 4, label: "ProteinEncodedBy", selected: false, depends: [1, 2]},
      {id: 5, label: "ProteinInteractsWithProtein", selected: false, depends: [2]},
      {id: 6, label: "ProteinInPathway", selected: false, depends: [2, 4]},
      {id: 7, label: "ProteinAssociatedWithDisorder", selected: false, depends: [2, 3]},
      {id: 8, label: "DisorderIsADisorder", selected: false, depends: [3]},
      {id: 9, label: "DisorderComorbidWithDisorder", selected: false, depends: [3]}
    ]
  },
  methods: {

    loadGraph: function (id) {
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
    toggleNode: function (nodeId) {
      let index = this.nodeModel.indexOf(nodeId)
      if (index >= 0) {
        let remove = -1;
        for (let idx in this.selectedElements) {
          if (this.selectedElements[idx].type === "node" && this.selectedElements[idx].id === nodeId) {
            remove = idx;
            break;
          }
        }
        //TODO check dependence on edge on removal
        this.selectedElements.splice(remove, 1)
      } else {
        this.selectedElements.push({id: nodeId, type: "node", name: this.nodes[nodeId].label, filter: "todo"})
      }
    },
    toggleEdge: function (edgeId) {
      console.log(edgeId)
      let index = this.edgeModel.indexOf(edgeId)
      if (index >= 0) {
        let remove = -1;
        for (let idx in this.selectedElements) {
          if (this.selectedElements[idx].type === "edge" && this.selectedElements[idx].id === edgeId) {
            remove = idx;
            break;
          }
        }
        this.selectedElements.splice(remove, 1)
      } else {

        let depending = []
        for (let idx in this.edges[edgeId].depends) {
          if (this.nodeModel.indexOf(this.nodes[this.edges[edgeId].depends[idx]]) === -1) {
            depending.push(this.edges[edgeId].depends[idx])
          }
        }
        //TODO add popup (alert) question for adding
        for (let idx in depending) {
          this.toggleNode(depending[idx])
          this.nodeModel.push(depending[idx])
        }
        this.selectedElements.push({id: edgeId, type: "edge", name: this.edges[edgeId].label, filter: "todo"})
      }
    }
  },

}
</script>

<style scoped>

</style>
