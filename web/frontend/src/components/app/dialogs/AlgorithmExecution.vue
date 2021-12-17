<template>
  <v-dialog
    v-model="model"
    max-width="750"
    style="z-index: 1001;"
    scrollable
    persistent
  >
    <v-sheet>
      <v-card>
        <template v-if="type==='mi'">
        <v-card-subtitle class="headline">Module Identification Algorithm Selection</v-card-subtitle>
        <v-card-subtitle style="margin-top: -25px">Select and adjust the algorithm you want to apply on your seeds
          to construct a disease module.
        </v-card-subtitle>
        </template>
        <template v-else>
          <v-card-subtitle class="headline">Drug Prioritization Algorithm Selection</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">Select and adjust the algorithm you want to apply on your seeds
            to identify a ranked list of drug candidates.
          </v-card-subtitle>
        </template>

        <v-divider style="margin: 15px;"></v-divider>
        <div style="max-height: 50vh; overflow-y: auto">
          <div style="padding:16px">
            <MIAlgorithmSelect ref="miAlgorithms" :header="false" v-if="type==='mi'" :seeds="nodes" flat
                               @jobEvent="jobSubmitted"
                               :seed-type-id="['gene','protein'].indexOf(nodeType)" socket-event="jobUpdateEvent"
                               @algorithmSelectedEvent="algorithmSelectedEvent"></MIAlgorithmSelect>
            <DPAlgorithmSelect ref="dpAlgorithms" v-else :header="false" :seeds="nodes" @jobEvent="jobSubmitted" flat
                               :seed-type-id="['gene','protein'].indexOf(nodeType)" socket-event="jobUpdateEvent"
                               @algorithmSelectedEvent="algorithmSelectedEvent"></DPAlgorithmSelect>
          </div>
        </div>
        <v-divider style="margin: 15px;"></v-divider>
        <v-card-actions>
          <ButtonCancel label="Cancel" @click="resolve(false)"></ButtonCancel>
          <ButtonNext label="Run" :disabled="!algoSelected" @click="resolve(true)"></ButtonNext>

        </v-card-actions>
      </v-card>
    </v-sheet>
  </v-dialog>
</template>

<script>
import MIAlgorithmSelect from "@/components/start/quick/MIAlgorithmSelect";
import DPAlgorithmSelect from "@/components/start/quick/DPAlgorithmSelect";
import ButtonNext from "@/components/start/quick/ButtonNext";
import ButtonCancel from "@/components/start/quick/ButtonCancel";

export default {
  name: "AlgorithmExecution",
  components: {ButtonCancel, ButtonNext, DPAlgorithmSelect, MIAlgorithmSelect},
  props: {
    type: String,
    nodeType: String,
    nodes: Array,
  },

  data() {
    return {
      model: false,
      algoSelected: false,
    }
  },
  methods: {

    show: function () {
      this.model = true;
    },

    algorithmSelectedEvent: function (state) {
      this.algoSelected = state;
    },
    resolve: function (bool) {
      if (bool) {
        let algo = this.type === "mi" ? this.$refs.miAlgorithms : this.$refs.dpAlgorithms;
        algo.run()
      } else {
        this.model = false;
      }
      this.algoSelected = false;
    },


    jobSubmitted: function (data) {
      this.$emit("newJobEvent", data)
      this.model = false;
    }
  }

}
</script>

<style scoped>

</style>
