<template>
  <v-card ref="tools" elevation="3">
    <v-container v-show="show">
      <v-list ref="list" style="margin-top: 30px;">
        <ToolSwitch v-model="nodeShadows" icon="fas fa-eye" label="Node shadow"
                    @click="$emit('toggleOptionEvent','shadow',nodeShadows)">
          <template v-slot:tooltip>
            <span>Enable or Disable shadows behind the nodes.</span>
          </template>
        </ToolSwitch>
        <ToolSwitch v-model="nodeLabels" icon="fas fa-tags" label="Node labels" :disabled="style==='ellipse'" @click="$emit('toggleOptionEvent','label',nodeLabels)">
          <template v-slot:tooltip:disabled>
            <span>Hiding the labels for the current node shape style does not make much sense.</span>
          </template>
          <template v-slot:tooltip>
            <span>Enable or Disable the node labels.</span>
          </template>
        </ToolSwitch>
        <ToolDropdown v-model="shapeModel" label="Node Shapes" icon="fas fa-shapes" :items="[{text:'Default', value:'shapes'},{text:'Only ellipses', value:'ellipse'},{text:'Only dots',value:'dot'}]" @change="switchNodeStyle">
        <template v-slot:tooltip>
          <span>Changes the shape of all nodes.</span>
        </template>
        </ToolDropdown>
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
      nodeLabels: true,
      style: "shapes",
      shapeModel: "shapes",
    }
  },
  created() {
  },
  methods: {
    switchNodeStyle: function(style){
      this.stye = style;
      this.$emit('switchOptionEvent','nodeStyle',style)
    }
  }

}
</script>

<style scoped>

</style>
