<template>
  <v-container>
    <v-card class="mx-auto">
      <v-container v-if="modus===0">
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
                  Selection of seed Genes/Proteins (manual or BiCon) for algorithmic disease module identification
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-btn plain @click="start(1,false)">
                    <v-icon left>
                      fas fa-angle-double-right
                    </v-icon>
                    Quick Module Identification
                  </v-btn>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-btn plain @click="start(1,true)">
                    <v-icon left>
                      fas fa-bolt
                    </v-icon>
                    Blitz Module Identification
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
                  Drug Repurposing
                </v-list-item-title>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  Starting e.g. with a disorder to derive Genes/Proteins for module identification and subsequent drug
                  ranking (e.g. trustrank)
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-btn plain @click="start(2,false)">
                    <v-icon left>
                      fas fa-angle-double-right
                    </v-icon>
                    Quick Drug Repurposing
                  </v-btn>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-btn plain @click="start(2,true)">
                    <v-icon left>
                      fas fa-bolt
                    </v-icon>
                    Blitz Drug Repurposing
                  </v-btn>
                </v-list-item-content>
              </v-list-item>
            </v-list>
          </v-col>
        </v-row>
      </v-container>
      <ModuleIdentification v-if="modus===1" :blitz="blitz" @resetEvent="modus=0"></ModuleIdentification>
      <DrugRepurposing v-if="modus===2" :blitz="blitz"></DrugRepurposing>
    </v-card>
  </v-container>
</template>

<script>
import ModuleIdentification from "./quick/ModuleIdentification";
import DrugRepurposing from "./quick/DrugRepurposing";

export default {
  name: "Quick",

  data() {
    return {
      modus: 0,
      blitz: false,
    }
  },

  created() {
    this.modus = 0
    this.blitz = false
  },


  methods: {
    start: function (modus, blitz) {
      this.modus = modus;
      this.blitz = blitz;
    }
  },


  components: {
    DrugRepurposing,
    ModuleIdentification
  }
}
</script>

<style scoped>

</style>
