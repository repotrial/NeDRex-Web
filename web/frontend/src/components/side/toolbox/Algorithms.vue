<template>
  <v-card elevation="3" style="margin:15px">
    <v-list-item @click="show=!show">
      <v-list-item-title>
        <v-icon left>{{ show ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
        Algorithms
      </v-list-item-title>
    </v-list-item>
    <v-container v-show="show">
      <v-divider></v-divider>
      <div>
        <div style="display: flex; justify-content: center">
          <v-radio-group v-model="nodeModel" dense row>
            <v-radio label="Gene"></v-radio>
            <v-radio label="Protein"></v-radio>
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
          <v-btn style="margin: 8px" small outlined :disabled="nodeModel==null" color="primary"
                 @click="$emit('openAlgorithmDialogEvent',{selection:selectionSwitch, type:['gene','protein'][nodeModel], algorithms:'mi'})">
            Module Identification
          </v-btn>
          <v-btn style="margin: 8px" small outlined :disabled="nodeModel==null" color="primary"
                 @click="$emit('openAlgorithmDialogEvent',{selection:selectionSwitch, type:['gene','protein'][nodeModel], algorithms:'dp'})">
            Drug Ranking
          </v-btn>
        </div>
      </div>
    </v-container>

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
