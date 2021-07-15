<template>
  <v-card ref="tools" elevation="3">
    <v-container v-show="show">
      <v-list ref="list" style="margin-top: 30px;">
        <v-tooltip left>
          <template v-slot:activator="{on, attrs}">
            <v-list-item v-on="on" v-bind="attrs">
              <v-list-item-action>
                <v-chip outlined v-on:click="$emit('clickOptionEvent','fit')">
                  <v-icon left>fas fa-globe</v-icon>
                  Overview
                </v-chip>
              </v-list-item-action>
            </v-list-item>
          </template>
          <span>Fits the view to the whole network.</span>
        </v-tooltip>
        <v-tooltip left>
          <template v-slot:activator="{on, attrs}">
            <v-list-item v-on="on" v-bind="attrs">
              <v-list-item-action-text>Enable node interactions</v-list-item-action-text>
              <v-list-item-action>
                <v-switch v-model="physicsOn" :disabled="physicsDisabled"
                          @click="$emit('toggleOptionEvent','physics',physicsOn)"></v-switch>
              </v-list-item-action>
            </v-list-item>
          </template>
          <span v-if="physicsDisabled">This option is disabled because there are too many <br>entities in the graph. As a result your browser might crash.</span>
          <span v-else>This option enables a physics based layouting where nodes and <br>edges interact with each other. Be careful on large graphs.</span>
        </v-tooltip>
        <v-tooltip left>
          <template v-slot:activator="{on, attrs}">
            <v-list-item v-on="on" v-bind="attrs">
              <v-list-item-action-text>Show Self-loops</v-list-item-action-text>
              <v-list-item-action>
                <v-switch v-model="loopsOn" @click="$emit('toggleOptionEvent','loops',loopsOn)"></v-switch>
              </v-list-item-action>
            </v-list-item>
          </template>
          <span>Enable or disable the visibility of edges having the same source and target node.</span>
        </v-tooltip>
        <v-tooltip left>
          <template v-slot:activator="{on, attrs}">
            <v-list-item v-on="on" v-bind="attrs">
              <v-list-item-action-text>Show Unconnected Nodes</v-list-item-action-text>
              <v-list-item-action>
                <v-switch v-model="unconnectedOn"
                          @click="$emit('toggleOptionEvent','unconnected',unconnectedOn)"></v-switch>
              </v-list-item-action>
            </v-list-item>
          </template>
          <span>Enable or disable the visualization of unconnected nodes.</span>
        </v-tooltip>
        <v-tooltip left>
          <template v-slot:activator="{on, attrs}">
            <v-list-item v-on="on" v-bind="attrs">
              <v-list-item-action-text>Isolate Connected component</v-list-item-action-text>
              <v-list-item-action>
                <v-switch :disabled="selectedNodeId==null" v-model="isolationOn" @click="emitIsolation()"></v-switch>
              </v-list-item-action>
            </v-list-item>
          </template>
          <span>Reduce the visibility to the connected component of the currently selected node only.<br> If no node is selected this option is disabled.</span>
        </v-tooltip>
      </v-list>
    </v-container>
  </v-card>
</template>

<script>


export default {
  props: {
    countMap: undefined,
    entityGraph: undefined,
    physicsDisabled: Boolean,
    options: Object,
  },
  name: "Tools",
  data() {
    return {
      show: true,
      tabModel: 0,
      physicsOn: false,
      loopsOn: false,
      unconnectedOn: true,
      isolationOn: false,
      selectedNodeId: undefined,
    }
  },
  created() {
  },
  methods: {
    setSelectedNodeId: function (nodeID) {
      this.selectedNodeId = nodeID;
    },
    emitIsolation: function () {
      this.$emit('toggleOptionEvent', 'isolation', {
        event: 'isolate',
        selected: this.selectedNodeId,
        state: this.isolationOn
      })
    },
    isLoops: function(){
      return this.loopsOn;
    }
  }
}
</script>

<style scoped>

</style>
