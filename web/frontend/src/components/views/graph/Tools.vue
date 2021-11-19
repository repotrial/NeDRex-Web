<template>
  <v-card ref="tools" elevation="3">
    <v-container v-show="show">
      <v-list ref="list" style="margin-top: 30px;">
        <v-tooltip top>
          <template v-slot:activator="{on, attrs}">
            <v-list-item>
              <v-list-item-action>
                <v-chip outlined v-on:click="$emit('clickOptionEvent','fit')" v-on="on" v-bind="attrs">
                  <v-icon left>fas fa-globe</v-icon>
                  Overview
                </v-chip>
              </v-list-item-action>
            </v-list-item>
          </template>
          <span>Fits the view to the whole network.</span>
        </v-tooltip>
        <ToolSwitch v-model="physicsOn" :disabled="physicsDisabled" icon="fas fa-exchange-alt" label="Node interactions"
                    @click="$emit('toggleOptionEvent','physics',physicsOn)" ref="physicsSwitch">
          <template v-slot:tooltip:disabled>
            <span>This option is disabled because there are too many <br>entities in the graph. As a result your browser might crash.</span>
          </template>
          <template v-slot:tooltip>
            <span>This option enables a physics based layouting where nodes and <br>edges interact with each other. Be careful on large graphs.</span>
          </template>
        </ToolSwitch>

        <ToolSwitch v-model="loopsOn" icon="fas fa-undo-alt" label="Show Self-loops" @click="$emit('toggleOptionEvent','loops',loopsOn)" v-if="loops">
          <template v-slot:tooltip>
            <span>Enable or disable the visibility of edges having the same source and target node.</span>
          </template>
        </ToolSwitch>
        <ToolSwitch v-model="unconnectedOn" icon="fas fa-unlink" label="Show Unconnected"
                    @click="$emit('toggleOptionEvent','unconnected',unconnectedOn)">
          <template v-slot:tooltip>
            <span>Enable or disable the visualization of unconnected nodes.</span>
          </template>
        </ToolSwitch>
        <ToolSwitch v-model="isolationOn" :disabled="selectedNodeId==null" label="Isolate Component" icon="fas fa-link" @click="emitIsolation()" v-if="cc">
          <template v-slot:tooltip>
            <span>Reduce the visibility to the connected component of the currently selected node only. <br> If no node is selected this option is disabled.</span>
          </template>
          <template v-slot:tooltip:disabled>
            <span>This option is disabled because no node is selected to define a connected component on.</span>
          </template>
        </ToolSwitch>
        <slot name="append"></slot>
      </v-list>
    </v-container>
  </v-card>
</template>
<script>


import ToolSwitch from "@/components/views/graph/tools/ToolSwitch";

export default {
  components: {ToolSwitch},
  props: {
    countMap: undefined,
    entityGraph: undefined,
    physicsDisabled: Boolean,
    options: Object,
    cc:{
      default: true,
      type: Boolean
    },
    loops:{
      default: true,
      type: Boolean
    }
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
    isLoops: function () {
      return this.loopsOn;
    },

    setPhysics:function(state){
      this.$set(this,"physicsOn",state)
    }
  }

}
</script>

<style scoped>

</style>
