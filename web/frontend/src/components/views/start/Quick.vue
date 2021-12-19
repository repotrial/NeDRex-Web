<template>
  <v-container style="max-width: 95vw">
    <template v-if="modus===-1">
      <div style="display:flex; justify-content: center">
        <div style="justify-content: center; justify-self: flex-start;">
          <PipelineCard :image="getConfig().STATIC_PATH+'/assets/module_ident.png'" title="Module Identification">

            <template slot="description">
              <div style="font-size: 0.9em">
                Identify disease modules by selecting <b><i>seed genes</i></b> or <b><i>proteins</i></b> by hand,
                suggestions using
                information about e.g. <b><i>association to disorders</i></b> or by uploading a <b><i>list of
                ids</i></b>. Tune the parameters of the algorithms
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
                Based on a set of given <b><i>seed genes</i></b> or <b><i>proteins</i></b> <b>drug candidates</b> can be
                identified through the
                application of <b>ranking</b> algorithms in combination with the <b><i>protein or gene interaction
                network</i></b> and
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
          <PipelineCard :image="getConfig().STATIC_PATH+'/assets/drug_rep.png'" title="Drug Repurposing"
                        subtitle="Module Identification + Drug Prioritization">
            <template slot="description">
              <div style="font-size: 0.9em">
                Identify disease modules as in the standalone mode (<b>Module Identification</b>) but with
                direct use in a subsequent
                <b>drug prioritization</b> analysis. Using the <b>Quick Mode</b> will use default values for the
                algorithmic
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
        <CombinedRepurposing v-if="modus===0" :blitz="blitz" @resetEvent="reset()" :reload="reload"
                             @printNotificationEvent="printNotification"
                             @graphLoadEvent="loadGraph"
                             @jobReloadError="jobReloadError()"
                             @graphLoadNewTabEvent="loadGraphNewTab"
                             @focusEvent="focusTop"
                             ref="drugRepurposing" @newGraphEvent="$emit('newGraphEvent')">

        </CombinedRepurposing>
        <ModuleIdentification v-if="modus===1" :blitz="blitz" @resetEvent="reset()" :reload="reload"
                              @jobReloadError="jobReloadError()"
                              @printNotificationEvent="printNotification"
                              @graphLoadEvent="loadGraph"
                              @graphLoadNewTabEvent="loadGraphNewTab"
                              @loadDrugTargetEvent="loadDrugTarget"
                              @focusEvent="focusTop"
                              ref="moduleIdentification" @newGraphEvent="$emit('newGraphEvent')"
        ></ModuleIdentification>
        <DrugRepurposing v-if="modus===2" :blitz="blitz" @resetEvent="reset()" :reload="reload"
                         @printNotificationEvent="printNotification"
                         @graphLoadEvent="loadGraph"
                         @jobReloadError="jobReloadError()"
                         @graphLoadNewTabEvent="loadGraphNewTab"
                         @focusEvent="focusTop"
                         ref="drugRanking" @newGraphEvent="$emit('newGraphEvent')"
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
      reload: undefined,
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
        this.$emit("showStartSelectionEvent", false)
      } else {
        this.$emit("showStartSelectionEvent", true)
      }
    }


  },

  methods: {
    reset: function () {
      this.jobURLReset()
      if (this.modus === 0 && this.$refs.drugRepurposing)
        this.$refs.drugRepurposing.reset()
      if (this.modus === 1 && this.$refs.moduleIdentification)
        this.$refs.moduleIdentification.reset()
      if (this.modus === 2 && this.$refs.drugRanking)
        this.$refs.drugRanking.reset()
      this.modus = -1
    },

    jobReloadError: function(){
      this.reset()
      this.printNotification("Could not reload the requested result! Either the job result is to old, broken or you might have to try again in a moment!",2,10000)
    },
    start: function (modus, blitz) {
      this.modus = modus;
      this.blitz = blitz;
      this.$emit("clearURLEvent")
    },

    waitForComponent: async function (component,resolve) {
      if (component != null)
        resolve()
      else
        setTimeout(() => {
          this.waitForComponent(component,resolve)
        }, 200)
    },


    reloadJob: function (job) {
      try {
      if(!job.goal){
        this.printNotification("Could not reload the result because your job was created with a too old version!",2,10000)
        this.reset()
        return
      }
        switch (job.goal) {
          case "MODULE_IDENTIFICATION": {
            this.modus = 1;
            this.reload = job;
            break;
          }
          case "DRUG_PRIORITIZATION": {
            this.modus = 2;
            this.reload = job;
            break;
          }
          case "DRUG_REPURPOSING": {
            this.modus = 0
            this.reload = job;
            break;
          }
        }
      }catch (e){
        this.printNotification("Could not reload the requested result! Either the job result is to old, broken or you might have to try again in a moment!",2,10000)
      }
    },
    getConfig() {
      return this.$config
    },

    jobURLReset(){
      this.reload = undefined;
      if(this.$route.fullPath !== location.pathname)
        this.$router.push(location.pathname)
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
    loadDrugTarget: function (data) {
      this.blitz = data.blitz;
      this.jobURLReset()
      this.modus = 2
      setTimeout(function () {
        this.$refs.drugRanking.setSeeds(data.seeds, data.type, data.origin)
        this.$refs.drugRanking.setDisorders(data.disorders)
        this.$refs.drugRanking.setDrugs(data.drugs, data.origin)
        this.$refs.drugRanking.setSuggestions(data.suggestions)
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
