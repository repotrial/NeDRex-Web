<template>
  <v-container>
    <template v-if="modus===-1">
      <div style="display:flex; justify-content: center">
        <div style="justify-content: center; justify-self: flex-start;">
          <PipelineCard :image="getConfig().STATIC_PATH+'/assets/module_ident.png'" title="Module Identification">

            <template slot="description">
              <div>
                Identify disease modules by selecting seed genes or proteins by hand, suggestions using
                information
                about e.g. association to disorders or by uploading a list. Tune the parameters of the algorithms
                (Agile) or just use the default for even less necessary input work (Quick).
              </div>
            </template>
            <template slot="actions">
              <div>
                <v-btn plain @click="start(1,false)" dark>
                  <v-icon left>
                    fas fa-angle-double-right
                  </v-icon>
                  Module Identification
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
          <v-icon>
            fas fa-plus
          </v-icon>
        </div>
        <div style="display: flex; justify-content: center; justify-self: flex-end">
          <PipelineCard :image="getConfig().STATIC_PATH+'/assets/drug_prio.png'" title="Drug Prioritization">

            <template slot="description">
              <div>
                Based on a set of given input genes or proteins drug candidates can be identified through the
                application of ranking algorithms in combination with the protein or gene interaction network and
                their known drug associations.
              </div>
            </template>
            <template slot="actions">
              <div>
                <v-btn plain @click="start(2,false)" dark>
                  <v-icon left>
                    fas fa-angle-double-right
                  </v-icon>
                  Drug Prioritization
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
        <v-divider style="margin: 16px 30vw 16px 30vw"></v-divider>
      <div style="display: flex; justify-content: center;">
        <div @mouseenter="hoverDrugRep=true" @mouseleave="hoverDrugRep=false">
          <PipelineCard :image="getConfig().STATIC_PATH+'/assets/drug_rep.png'" title="Drug Repurposing">
            <template slot="description">
              <div>
                Identify disease modules as in the standalone mode (Agile/Quick Module Identification) but with
                direct
                use in a subsequent
                drug prioritization analysis. Using the Quick mode will use default values for the algorithmic
                choices, limiting the users decisions to identifying the starting genes.
              </div>
            </template>
            <template slot="actions">
              <div>
                <v-btn plain @click="start(0,false)" dark>
                  <v-icon left>
                    fas fa-angle-double-right
                  </v-icon>
                  Drug Repurposing
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
        <CombinedRepurposing v-if="modus===0" :blitz="blitz" @resetEvent="modus=-1" :metagraph="metagraph"
                             @printNotificationEvent="printNotification"
                             @graphLoadEvent="loadGraph"
                             @focusEvent="focusTop"
                             ref="drugRepurposing">

        </CombinedRepurposing>
        <ModuleIdentification v-if="modus===1" :blitz="blitz" @resetEvent="modus=-1" :metagraph="metagraph"
                              @printNotificationEvent="printNotification"
                              @graphLoadEvent="loadGraph"
                              @loadDrugTargetEvent="loadDrugTarget"
                              @focusEvent="focusTop"
                              ref="moduleIdentification"
        ></ModuleIdentification>
        <DrugRepurposing v-if="modus===2" :blitz="blitz" @resetEvent="modus=-1" :metagraph="metagraph"
                         @printNotificationEvent="printNotification"
                         @graphLoadEvent="loadGraph"
                         @focusEvent="focusTop"
                         ref="drugRanking"
        ></DrugRepurposing>
      </v-col>
    </v-row>
  </v-container>
</template>
<script>

import ModuleIdentification from "./quick/ModuleIdentification";
import DrugRepurposing from "./quick/DrugRepurposing";
import CombinedRepurposing from "./quick/CombinedRepurposing";
import PipelineCard from "@/components/app/start/PipelineCard";

export default {
  name: "Quick",
  props: {
    metagraph: Object,
  },
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
