<template>
  <v-card ref="tools" elevation="3">
    <v-container v-show="show">
      <v-list ref="list" style="margin-top: 30px;">
<!--        <v-tooltip top>-->
<!--          <template v-slot:activator="{on, attrs}">-->
<!--            <v-list-item>-->
<!--              <v-list-item-action>-->
<!--                <v-chip outlined v-on:click="$emit('clickOptionEvent','fit')" v-on="on" v-bind="attrs">-->
<!--                  <v-icon left>fas fa-globe</v-icon>-->
<!--                  Overview-->
<!--                </v-chip>-->
<!--              </v-list-item-action>-->
<!--            </v-list-item>-->
<!--          </template>-->
<!--          <span>Fits the view to the whole network.</span>-->
<!--        </v-tooltip>-->
        <ToolSwitch v-model="nodeShadows" icon="fas fa-eye" label="Node shadow"
                    @click="$emit('toggleOptionEvent','shadow',nodeShadows)">
          <template v-slot:tooltip:disabled>
            <span>This option is disabled because there are too many <br>entities in the graph. As a result your browser might crash.</span>
          </template>
          <template v-slot:tooltip>
            <span>Enable or Disable shadows behind the nodes.</span>
          </template>
        </ToolSwitch>
        <ToolDropdown label="Node Shapes" icon="fas fa-shapes" :items="[{text:'Default', value:'shapes'},{text:'Only ellipses', value:'ellipse'},{text:'Only dots',value:'dot'}]" @change="switchNodeStyle">
        <template v-slot:tooltip>
          <span>Changes the shape of all nodes.</span>
        </template>
        </ToolDropdown>

<!--        <ToolSwitch v-model="loopsOn" icon="fas fa-undo-alt" label="Show Self-loops" @click="$emit('toggleOptionEvent','loops',loopsOn)">-->
<!--          <template v-slot:tooltip>-->
<!--            <span>Enable or disable the visibility of edges having the same source and target node.</span>-->
<!--          </template>-->
<!--        </ToolSwitch>-->
<!--        <ToolSwitch v-model="unconnectedOn" icon="fas fa-unlink" label="Show Unconnected"-->
<!--                    @click="$emit('toggleOptionEvent','unconnected',unconnectedOn)">-->
<!--          <template v-slot:tooltip>-->
<!--            <span>Enable or disable the visualization of unconnected nodes.</span>-->
<!--          </template>-->
<!--        </ToolSwitch>-->
<!--        <ToolSwitch v-model="isolationOn" :disabled="selectedNodeId==null" label="Isolate Component" icon="fas fa-link" @click="emitIsolation()">-->
<!--          <template v-slot:tooltip>-->
<!--            <span>Reduce the visibility to the connected component of the currently selected node only. <br> If no node is selected this option is disabled.</span>-->
<!--          </template>-->
<!--          <template v-slot:tooltip:disabled>-->
<!--            <span>This option is disabled because no node is selected to define a connected component on.</span>-->
<!--          </template>-->
<!--        </ToolSwitch>-->
      </v-list>
    </v-container>
  </v-card>
</template>
<script>


import ToolSwitch from "@/components/views/graph/tools/ToolSwitch";
import ToolDropdown from "@/components/views/graph/tools/ToolDropdown";

export default {
  components: {ToolDropdown, ToolSwitch},
  props: {
    options: Object,
  },
  name: "VisualizationOptions",
  data() {
    return {
      show: true,
      nodeShadows: true,
    }
  },
  created() {
  },
  methods: {
    switchNodeStyle: function(style){
      this.$emit('switchOptionEvent','nodeStyle',style)
    }
  }

}
</script>

<style scoped>

</style>
