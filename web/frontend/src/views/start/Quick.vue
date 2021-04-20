<template>
  <v-container>
    <template v-if="modus===-1">
      <v-row>
        <v-col cols="3"></v-col>
        <v-col cols="6">
          <v-card>
            <v-list>
              <v-list-item>
                <v-list-item-title class="title">
                  Drug Repurposing
                </v-list-item-title>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  Identify disease modules like in the standalone Agile Start module but with direct use in a subsequent
                  drug prioritization analysis. Using the Minimal mode will use default values for the algorithmic
                  choices,
                  so only the starting genes will have to be defined.
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-btn plain @click="start(0,false)">
                    <v-icon left>
                      fas fa-angle-double-right
                    </v-icon>
                    Agile Drug Repurposing
                  </v-btn>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-btn plain @click="start(0,true)">
                    <v-icon left>
                      fas fa-bolt
                    </v-icon>
                    Quick Drug Repurposing
                  </v-btn>
                </v-list-item-content>
              </v-list-item>
            </v-list>
          </v-card>
        </v-col>
      </v-row>
      <v-row>
        <v-card class="mx-auto">
          <v-container>
            <v-row>
              <v-col cols="5">
                <v-list>
                  <v-list-item>
                    <v-list-item-title class="title">
                      Disease Modules
                    </v-list-item-title>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-content>
                      Identify disease modules by selecting seed genes or protein by hand, suggestions using information
                      about e.g. association to disorders or by uploading a list. Tune the parameters of the algorithms
                      (Agile) or just use the default for even less necessary input work (Quick).
                    </v-list-item-content>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-content>
                      <v-btn plain @click="start(1,false)">
                        <v-icon left>
                          fas fa-angle-double-right
                        </v-icon>
                        Agile Module Identification
                      </v-btn>
                    </v-list-item-content>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-content>
                      <v-btn plain @click="start(1,true)">
                        <v-icon left>
                          fas fa-bolt
                        </v-icon>
                        Quick Module Identification
                      </v-btn>
                    </v-list-item-content>
                  </v-list-item>
                </v-list>
              </v-col>
              <v-col cols="2">
                <v-divider vertical></v-divider>
              </v-col>
              <v-col cols="5">
                <v-list>
                  <v-list-item>
                    <v-list-item-title class="title">
                      Drug Prioritization
                    </v-list-item-title>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-content>
                      Starting e.g. with a disorder to derive Genes/Proteins for module identification and subsequent
                      drug
                      ranking (e.g. TrustRank)
                    </v-list-item-content>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-content>
                      <v-btn plain @click="start(2,false)">
                        <v-icon left>
                          fas fa-angle-double-right
                        </v-icon>
                        Agile Drug Prioritization
                      </v-btn>
                    </v-list-item-content>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-content>
                      <v-btn plain @click="start(2,true)">
                        <v-icon left>
                          fas fa-bolt
                        </v-icon>
                        Quick Drug Repurposing
                      </v-btn>
                    </v-list-item-content>
                  </v-list-item>
                </v-list>
              </v-col>
            </v-row>
          </v-container>
        </v-card>
      </v-row>
    </template>
    <v-row>
      <v-col>
        <CombinedRepurposing v-if="modus===0" :blitz="blitz" @resetEvent="modus=-1" :metagraph="metagraph"
                             @printNotificationEvent="printNotification"
                             @graphLoadEvent="loadGraph"
                             @focusEvent="focusTop"
        >

        </CombinedRepurposing>
        <ModuleIdentification v-if="modus===1" :blitz="blitz" @resetEvent="modus=-1" :metagraph="metagraph"
                              @printNotificationEvent="printNotification"
                              @graphLoadEvent="loadGraph"
                              @loadDrugTargetEvent="loadDrugTarget"
                              @focusEvent="focusTop"
        ></ModuleIdentification>
        <DrugRepurposing v-if="modus===2" :blitz="blitz" @resetEvent="modus=-1" :metagraph="metagraph"
                         @printNotificationEvent="printNotification"
                         @graphLoadEvent="loadGraph"
                         @focusEvent="focusTop"
        ></DrugRepurposing>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import ModuleIdentification from "./quick/ModuleIdentification";
import DrugRepurposing from "./quick/DrugRepurposing";
import CombinedRepurposing from "./quick/CombinedRepurposing";

export default {
  name: "Quick",
  props: {
    metagraph: Object,
  },
  data() {
    return {
      modus: -1,
      blitz: false,
    }
  },

  created() {
    this.modus = -1
    this.blitz = false
  },
  watch:{
    modus: function(val){
        if(val>-1){
         this.focusTop()
        }
    }


  },

  methods: {
    start: function (modus, blitz) {
      this.modus = modus;
      this.blitz = blitz;
      this.$emit("clearURLEvent")
    },


    printNotification: function (message, type) {
      this.$emit("printNotificationEvent", message, type)
    },

    loadGraph: function (gid) {
      this.$emit("graphLoadEvent", gid)
      this.modus=-1
    },
    loadDrugTarget: function(blitz, seeds, type){
      this.blitz = blitz;
      this.modus = 2
      setTimeout(function(){this.$refs.drugTargeting.setSeeds(seeds,type)}.bind(this),500)

    },
    focusTop: function(){
      this.$emit("focusEvent")
    }
  },


  components: {
    DrugRepurposing,
    ModuleIdentification,
    CombinedRepurposing
  }
}
</script>

<style scoped>

</style>
