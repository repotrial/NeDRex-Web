<template>
  <v-container>
    <template v-if="modus===-1">
      <div style="display:flex; justify-content: center">
        <div style="justify-content: center; justify-self: flex-start;">
          <PipelineCard :image="getConfig().STATIC_PATH+'/assets/module_ident.png'" title="Module Identification">

            <template slot="description">
              <div style="font-size: 0.9em">
                Identify disease modules by selecting <b><i>seed genes</i></b> or <b><i>proteins</i></b> by hand, suggestions using
                information about e.g. <b><i>association to disorders</i></b> or by uploading a <b><i>list of ids</i></b>. Tune the parameters of the algorithms
                (<b>Basic Mode</b>) or just use defaults for even less necessary input work (<b>Quick Mode</b>).
              </div>
            </template>
            <template slot="actions">
              <div>
                <v-btn plain @click="start(1,false)" dark>
                  <v-icon left>
                    fas fa-angle-double-right
                  </v-icon>
                 Run Module Identification
                </v-btn>
              </div>
              <div>
                <v-btn plain @click="start(1,true)" dark>
                  <v-icon left>
                    fas fa-bolt
                  </v-icon>
                  Quick Mode
                </v-btn>
              </div>
            </template>
          </PipelineCard>
        </div>
        <div v-show="hoverDrugRep" style="display: flex; align-content: center">
          <v-icon size="2vw">
            fas fa-plus
          </v-icon>
        </div>
        <div style="display: flex; justify-content: center; justify-self: flex-end">
          <PipelineCard :image="getConfig().STATIC_PATH+'/assets/drug_prio.png'" title="Drug Prioritization">

            <template slot="description">
              <div style="font-size:0.9em">
                Based on a set of given <b><i>seed genes</i></b> or <b><i>proteins</i></b> <b>drug candidates</b> can be identified through the
                application of <b>ranking</b> algorithms in combination with the <b><i>protein or gene interaction network</i></b> and
                their known <b>drug associations</b>.
              </div>
            </template>
            <template slot="actions">
              <div>
                <v-btn plain @click="start(2,false)" dark>
                  <v-icon left>
                    fas fa-angle-double-right
                  </v-icon>
                  Run Drug Prioritization
                </v-btn>
              </div>
              <div>
                <v-btn plain @click="start(2,true)" dark>
                  <v-icon left>
                    fas fa-bolt
                  </v-icon>
                  Quick Mode
                </v-btn>
              </div>
            </template>
          </PipelineCard>
        </div>
      </div>
      <div style="display: flex; justify-content: center">
        <v-icon size="4vw">fas fa-chevron-down</v-icon>
      </div>
      <div style="display: flex; justify-content: center;">
        <div @mouseenter="hoverDrugRep=true" @mouseleave="hoverDrugRep=false">
          <PipelineCard :image="getConfig().STATIC_PATH+'/assets/drug_rep.png'" title="Drug Repurposing" subtitle="Module Identification + Drug Prioritization">
            <template slot="description">
              <div style="font-size: 0.9em">
                Identify disease modules as in the standalone mode (<b>Module Identification</b>) but with
                direct use in a subsequent
                <b>drug prioritization</b> analysis. Using the <b>Quick Mode</b> will use default values for the algorithmic
                choices, limiting the users decisions to identifying the starting genes.
              </div>
            </template>
            <template slot="actions">
              <div>
                <v-btn plain @click="start(0,false)" dark>
                  <v-icon left>
                    fas fa-angle-double-right
                  </v-icon>
                  Run Drug Repurposing
                </v-btn>
              </div>
              <div>
                <v-btn plain @click="start(0,true)" dark>
                  <v-icon left>
                    fas fa-bolt
                  </v-icon>
                  Quick Mode
                </v-btn>
              </div>
            </template>
          </PipelineCard>
        </div>
      </div>
    </template>
    <v-row>
      <v-col>
        <CombinedRepurposing v-if="modus===0" :blitz="blitz" @resetEvent="modus=-1"
                             @printNotificationEvent="printNotification"
                             @graphLoadEvent="loadGraph"
                             @graphLoadNewTabEvent="loadGraphNewTab"
                             @focusEvent="focusTop"
                             ref="drugRepurposing">

        </CombinedRepurposing>
        <ModuleIdentification v-if="modus===1" :blitz="blitz" @resetEvent="modus=-1"
                              @printNotificationEvent="printNotification"
                              @graphLoadEvent="loadGraph"
                              @graphLoadNewTabEvent="loadGraphNewTab"
                              @loadDrugTargetEvent="loadDrugTarget"
                              @focusEvent="focusTop"
                              ref="moduleIdentification"
        ></ModuleIdentification>
        <DrugRepurposing v-if="modus===2" :blitz="blitz" @resetEvent="modus=-1"
                         @printNotificationEvent="printNotification"
                         @graphLoadEvent="loadGraph"
                         @graphLoadNewTabEvent="loadGraphNewTab"
                         @focusEvent="focusTop"
                         ref="drugRanking"
        ></DrugRepurposing>
      </v-col>
    </v-row>
  </v-container>
</template>
<script>

import ModuleIdentification from "./quick/ModuleIdentification";
import DrugRepurposing from "./quick/DrugPrioritization";
import CombinedRepurposing from "./quick/DrugRepurposing";
import PipelineCard from "@/components/app/start/PipelineCard";

export default {
  name: "Quick",
  data() {
    return {
      modus: -1,
      blitz: false,
      hoverDrugRep: false,
    }
  },
  created() {
    this.modus = -1
    this.blitz = false
  },
  watch: {
    modus: function (val) {
      if (val > -1) {
        this.focusTop()
        this.$emit("showStartSelectionEvent",false)
      }else{
        this.$emit("showStartSelectionEvent",true)
      }
    }


  },

  methods: {
    reset: function () {
      if (this.modus === 0 && this.$refs.drugRepurposing)
        this.$refs.drugRepurposing.reset()
      if (this.modus === 1 && this.$refs.moduleIdentification)
        this.$refs.moduleIdentification.reset()
      if (this.modus === 2 && this.$refs.drugRanking)
        this.$refs.drugRanking.reset()
      this.modus = -1
    },
    start: function (modus, blitz) {
      this.modus = modus;
      this.blitz = blitz;
      this.$emit("clearURLEvent")
    },
    getConfig() {
      return this.$config
    },

    printNotification: function (message, type) {
      this.$emit("printNotificationEvent", message, type)
    },

    loadGraph: function (gid) {
      this.$emit("graphLoadEvent", gid)
    },
    loadGraphNewTab: function (gid) {
      this.$emit("graphLoadNewTabEvent", gid)
    },
    loadDrugTarget: function (blitz, seeds, type, origin) {
      this.blitz = blitz;
      this.modus = 2
      setTimeout(function () {
        this.$refs.drugRanking.setSeeds(seeds, type, origin)
      }.bind(this), 500)

    },
    focusTop: function () {
      this.$emit("focusEvent")
    }
  },


  components: {
    PipelineCard,
    DrugRepurposing,
    ModuleIdentification,
    CombinedRepurposing
  }
}
</script>

<style scoped>

</style>
