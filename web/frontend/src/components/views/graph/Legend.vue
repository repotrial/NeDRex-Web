<template>
  <v-card ref="legend" elevation="3" v-if="entityGraph!==undefined">
    <v-container v-show="show">
      <v-card-title style="margin-top:.5rem;padding-bottom:0">Nodes</v-card-title>
      <v-list ref="list">
        <v-list-item v-for="node in Object.values(countMap.nodes)" :key="node.name" style="min-height: 30px; height:2rem">
          <v-list-item-avatar width="20" height="20">
            <v-icon :color="getColoring('nodes',node.name,'light')" style="max-height: 1rem">fas fa-genderless</v-icon>
          </v-list-item-avatar>
          <v-list-item-title>{{ node.name }}</v-list-item-title>
          <v-list-item-subtitle style="font-size: small">{{ node.total }}</v-list-item-subtitle>
          <LegendAction
            @click="toggleNode(node.name)"
            :color="isToggled('nodes',node.name) ? 'primary' : 'gray'"
            :icon="isToggled('nodes',node.name) ?'far fa-eye' : 'far fa-eye-slash'"
          >
            <template v-slot:tooltip>
              Toggle the visualization of the <i><b>{{ node.name }}</b></i> nodes.<br>Current state:
              <b>{{ isToggled('nodes', node.name) ? 'on' : 'off' }}</b><br>
            </template>
          </LegendAction>
          <LegendAction icon="fas fa-thumbtack">
            <template v-slot:tooltip>
              Fixating nodes for interaction simulation will be added in the future.
            </template>
          </LegendAction>
          <LegendAction icon="fas fa-download" color="primary" @click="startDownload('nodes',node.name)">
            <template v-slot:tooltip>
              Download list of all <i>{{ node.name }}</i> nodes.
            </template>
          </LegendAction>
        </v-list-item>
        <v-list-item v-for="node in nodeMap" :key="node.label" ref="custom">
          <v-chip outlined @click="toggleNode(node.name)">
            <v-icon left :color="options.toggled.nodes[node.name] ? node.color :'gray'" size="15px">fas fa-circle
            </v-icon>
            {{ node.label }}
          </v-chip>
        </v-list-item>
      </v-list>
      <v-card-title style="margin-top:0;padding-bottom:0">Edges</v-card-title>
      <v-list>
        <v-list-item v-for="edge in Object.values(countMap.edges)" :key="edge.name" style="min-height: 30px; height:2rem">
          <v-list-item-avatar min-width="65" height="20">
            <span>
            <v-icon class="edge-icon" :color="getColoring('edges',edge.name,'light')[0]">fas fa-genderless</v-icon>
              <template v-if="direction(edge.name)===0">
                <v-icon class="edge-icon" left>fas fa-undo-alt</v-icon>
              </template>
              <template v-else>
                <v-icon class="edge-icon" v-if="direction(edge.name)===1">fas fa-long-arrow-alt-right</v-icon>
                <v-icon class="edge-icon" v-else>fas fa-arrows-alt-h</v-icon>
                <v-icon class="edge-icon" :color="getColoring('edges',edge.name,'light')[1]">fas fa-genderless</v-icon>
              </template>
              </span>
          </v-list-item-avatar>
          <v-tooltip bottom>
            <template v-slot:activator="{on, attrs}">
              <v-list-item-title style="font-size: small" v-on="on" v-bind="attrs">{{ edge.name }}</v-list-item-title>
            </template>
            <span>{{ edge.name }}</span>
          </v-tooltip>

          <v-list-item-subtitle style="font-size: small; max-width: 4rem">{{ edge.total }}</v-list-item-subtitle>
          <LegendAction :color="isToggled('edges',edge.name) ? 'primary' : 'gray'"
                        @click="toggleEdge(edge.name)"
                        :icon="isToggled('edges',edge.name)? 'far fa-eye': 'far fa-eye-slash'">
            <template v-slot:tooltip>
              Toggle the visualization of the <i><b>{{ edge.name }}</b></i> edges.<br>Current state:
              <b>{{ isToggled('edges', edge.name) ? 'on' : 'off' }}</b><br>
              Also removes node attraction when node interactions are activated.
            </template>
          </LegendAction>
          <LegendAction icon="fas fa-download" color="primary" @click="startDownload('edges',edge.name)">
            <template v-slot:tooltip>
              Download list of all <i>{{ edge.name }}</i> edges.
            </template>
          </LegendAction>
        </v-list-item>
      </v-list>
    </v-container>
  </v-card>
</template>

<script>

import LegendAction from "@/components/views/graph/legend/LegendAction";

export default {
  props: {
    countMap: undefined,
    entityGraph: undefined,
    options: Object,
  },
  name: "Legend",

  data() {
    return {
      show: true,
      tabModel: 0,
      nodeMap: {}
    }
  },
  created() {
    if (this.options.toggled === undefined) {
      this.options.toggled = {}
    }
    console.log(this.$global.metagraph)
  },
  methods: {
    toggleNode: function (nodeName) {
      this.graphChangeVisEvent('nodes', nodeName)
    },
    graphChangeVisEvent(type, name) {
      this.options.toggled[type][name] = !this.options.toggled[type][name]
      this.$forceUpdate()
      this.$emit("graphViewEvent", {
        event: "toggle",
        params: {type: type, name: name, state: !this.options.toggled[type][name]}
      })
    },
    toggleEdge: function (edgeName) {
      this.graphChangeVisEvent('edges', edgeName)
    },

    isToggled: function (entity, name) {
      if (this.options.toggled[entity] === undefined)
        this.options.toggled[entity] = {}
      if (this.options.toggled[entity][name] === undefined)
        this.options.toggled[entity][name] = true;
      return this.options.toggled[entity][name];
    },

    getColoring: function (entity, name, style) {
      if (!this.isToggled(entity, name))
        return "gray";
      return this.$utils.getColoringExtended(this.$global.metagraph, this.entityGraph, entity, name, style);
    },
    direction: function (edge) {
      return this.$utils.directionExtended(this.entityGraph, edge)
    },
    getCounts: function (entity) {
      let objects = Object.values(this[entity]);
      return objects === undefined || objects.length === 0 ? 0 : objects.map(e => e.length).reduce((i, j) => i + j)
    },
    addColoring: function (request) {
      if (this.options.toggled.nodes === undefined)
        this.options.toggled.nodes = {}
      if (this.options.toggled.nodes[request.name] === undefined)
        this.options.toggled.nodes[request.name] = true;
      if (this.nodeMap[request.name] !== undefined)
        this.nodeMap[request.name].color = request.color
      else
        this.nodeMap[request.name] = {label: request.name, name: request.name.toLowerCase(), color: request.color}
      this.$forceUpdate()
    },

    startDownload: function (entity, name) {
      this.$emit("downloadEntries",entity,name)
    },
  },
  components: {
    LegendAction,
  }
}
</script>

<style scoped>

.v-list-item__action {
  margin-left: 4px !important;
  margin-right: 0;
  margin-top:0;
  margin-bottom:0;
}

.v-list-item__avatar, .v-list-item__avatar.v-list-item__avatar--horizontal {
  margin:0;
}

.edge-icon {
  margin-left: 2px;
  margin-right: 2px;
  max-height: 2rem;
}

</style>
