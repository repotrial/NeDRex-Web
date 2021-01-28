<template>
  <v-card ref="legend" elevation="3" style="margin:15px" v-if="metagraph!==undefined && entityGraph!==undefined">
<!--    <v-list-item @click="show=!show">-->
<!--      <v-list-item-title>-->
<!--        <v-icon left>{{ show ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>-->
<!--        Legend-->
<!--      </v-list-item-title>-->
<!--    </v-list-item>-->
<!--    <v-divider></v-divider>-->
    <v-container v-show="show">
<!--      <v-tabs-->
<!--        fixed-tabs-->
<!--        v-model="tabModel"-->
<!--      >-->
<!--        <v-tabs-slider></v-tabs-slider>-->
<!--        <v-tab class="primary&#45;&#45;text">-->
<!--         Nodes-->
<!--        </v-tab>-->
<!--        <v-tab class="primary&#45;&#45;text">-->
<!--          Edges-->
<!--        </v-tab>-->
<!--      </v-tabs>-->
      <v-card-title>Nodes</v-card-title>
          <v-list>
            <v-list-item v-for="node in Object.values(countMap.nodes)" :key="node.name">
              <v-chip outlined @click="toggleNode(node.name)">
                <v-icon left :color="getColoring('nodes',node.name)">fas fa-genderless</v-icon>
                {{ node.name }} ({{ node.total }})
              </v-chip>
            </v-list-item>
          </v-list>
      <v-card-title>Edges</v-card-title>
          <v-list>
            <v-list-item v-for="edge in Object.values(countMap.edges)" :key="edge.name">
              <v-chip outlined @click="toggleEdge(edge.name)">
                <v-icon left :color="getColoring('edges',edge.name)[0]" >fas fa-genderless</v-icon>
                <template v-if="direction(edge.name)===0">
                  <v-icon left>fas fa-undo-alt</v-icon>
                </template>
                <template v-else>
                  <v-icon v-if="direction(edge.name)===1" left>fas fa-long-arrow-alt-right</v-icon>
                  <v-icon v-else left>fas fa-arrows-alt-h</v-icon>
                  <v-icon left :color="getColoring('edges',edge.name)[1]">fas fa-genderless</v-icon>
                </template>
                {{ edge.name }} ({{ edge.total }})
              </v-chip>
            </v-list-item>
          </v-list>
    </v-container>
  </v-card>
</template>

<script>

import Utils from "../../scripts/Utils"

export default {
  props: {
    metagraph: undefined,
    countMap: undefined,
    entityGraph: undefined,
    options:Object,
  },
  name: "Legend",

  data() {
    return {
      show: true,
      tabModel:0,
    }
  },
  created(){
    if(this.options.toggled === undefined){
      this.options.toggled = {}
    }
  },
  methods: {

    toggleNode: function (nodeName) {
      this.graphChangeVisEvent('nodes', nodeName)

    },
    graphChangeVisEvent(type, name) {
      this.options.toggled[type][name]=!this.options.toggled[type][name]
      this.$forceUpdate()
      this.$emit("graphViewEvent", {event: "toggle",params: {type: type, name: name, state: !this.options.toggled[type][name]}})
    },
    toggleEdge: function (edgeName) {
      this.graphChangeVisEvent('edges', edgeName)
    },
    getColoring: function (entity, name) {
      if (this.options.toggled[entity] === undefined)
        this.options.toggled[entity] = {}
      if (this.options.toggled[entity][name] === undefined)
        this.options.toggled[entity][name] = true;
      if(!this.options.toggled[entity][name])
        return "gray";
      return Utils.getColoringExtended(this.metagraph, this.entityGraph, entity, name);
    },
    direction: function (edge) {
      return Utils.directionExtended(this.entityGraph, edge)
    },

    getCounts: function (entity) {
      let objects = Object.values(this[entity]);
      return objects === undefined || objects.length === 0 ? 0 : objects.map(e => e.length).reduce((i, j) => i + j)
    },
  }

}
</script>

<style scoped>

</style>
