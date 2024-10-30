<template>
  <v-card elevation="3" style="margin:15px">
    <v-list-item @click="show=!show">
      <v-list-item-title>
        <v-icon left>{{ show ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
        Algorithms
      </v-list-item-title>
    </v-list-item>
    <div v-show="show">
      <v-divider></v-divider>
      <div>
        <div style="display: flex; justify-content: center">
          <v-radio-group v-model="nodeModel" dense row>
            <v-radio label="Gene" value="gene"></v-radio>
            <v-radio label="Protein" vale="protein"></v-radio>
          </v-radio-group>
        </div>
        <LabeledSwitch label-on="Use selection" label-off="Use all" :disabled="nodeModel==null"
                       v-model="selectionSwitch">
          <template v-slot:tooltip>
            <div> If disabled, uses all current nodes of the selected type as seeds. If enabled, only nodes from the
              users selection are used.
            </div>
          </template>
        </LabeledSwitch>
        <div>
          <v-chip style="margin: 8px" outlined :disabled="nodeModel==null"
                 @click="$emit('openAlgorithmDialogEvent',{selection:selectionSwitch, type:nodeModel, algorithms:'mi'})">
            <v-icon left small color="primary">fas fa-cog</v-icon>Module Identification
          </v-chip>
          <v-chip style="margin: 8px" outlined :disabled="nodeModel==null"
                 @click="$emit('openAlgorithmDialogEvent',{selection:selectionSwitch, type:nodeModel, algorithms:'dp'})">
            <v-icon left small color="primary">fas fa-cog</v-icon>Drug Ranking
          </v-chip>
        </div>
      </div>
    </div>

  </v-card>
</template>

<script>

import LabeledSwitch from "@/components/app/input/LabeledSwitch";

export default {
  name: "Algorithms",
  components: {LabeledSwitch},
  data() {
    return {
      show: false,
      nodeModel: undefined,
      selectionSwitch: false,

    }
  },
  methods: {

    resetAlgorithms: function () {
      this.nodeModel = undefined
      this.selectionSwitch = false
    },
  }


}
</script>

<style scoped>

</style>
