<template>
  <v-stepper
    alt-labels
    v-model="step"
  >
    <v-stepper-header>
      <v-stepper-step step="1" :complete="step>1">
        Select Seeds
        <small v-if="seedTypeId!==undefined">{{ ["Gene", "Protein"][seedTypeId] }} ({{ seeds.length }})</small>
      </v-stepper-step>
      <v-divider></v-divider>
      <v-stepper-step step="2" :complete="step>2 || blitz">
        Select Method
        <small v-if="method!==undefined">{{ method }}</small>
      </v-stepper-step>
      <v-divider></v-divider>
      <v-stepper-step step="3">
        Results
      </v-stepper-step>
    </v-stepper-header>

    <v-stepper-items>
      <v-stepper-content step="1">
        <v-card
          class="mb-12"
          height="500px"
        >

          <v-card-subtitle class="title">1. Seed Configuration</v-card-subtitle>
          <v-container style="height: 80%">
            <v-row style="height: 100%">
              <v-col cols="6">
                <v-container>
                  <v-card-title class="subtitle-1">Select the seed type</v-card-title>
                  <v-card-text>
                    <v-radio-group row v-model="seedTypeId">
                      <v-radio label="Gene">
                      </v-radio>
                      <v-radio label="Protein">
                      </v-radio>
                    </v-radio-group>
                  </v-card-text>
                </v-container>
                <v-container v-show="seedTypeId!==undefined">
                  <v-card-title class="subtitle-1">Add seeds</v-card-title>

                  <v-text-field label="by suggestions"></v-text-field>
                  <v-card-subtitle>or</v-card-subtitle>
                  <v-file-input :label="'by '+['entrez','uniprot'][seedTypeId]+' ids'"
                                v-on:change="onFileSelected"
                                show-size
                                prepend-icon="far fa-list-alt"
                                dense
                  ></v-file-input>
                </v-container>
              </v-col>

              <v-divider vertical></v-divider>
              <v-col cols="5">
                <v-card-title class="subtitle-1">Selected Seeds</v-card-title>
                <v-list>
                  <v-list-item v-for="(seed,index) in seeds" :key="seed.id">
                    <v-list-item-icon ><v-icon v-if="highlighted.indexOf(seed.id)!==-1">fas fa-star</v-icon></v-list-item-icon>
                    <v-list-item-title>{{seed.displayName}}</v-list-item-title>
                    <v-list-item-action>
                      <v-btn icon @click="seeds.splice(index,1)">
                        <v-icon>far fa-minus-square</v-icon>
                      </v-btn>

                    </v-list-item-action>
                  </v-list-item>

                </v-list>
              </v-col>
            </v-row>
          </v-container>


        </v-card>
        <v-btn
          color="primary"
          @click="makeStep(1,'continue')"
        >
          Continue
        </v-btn>

        <v-btn text @click="makeStep(1,'cancel')">
          Cancel
        </v-btn>
      </v-stepper-content>

      <v-stepper-content step="2">
        <v-card
          class="mb-12"
          color="grey lighten-1"
          height="500px"
        ></v-card>

        <v-btn text @click="makeStep(2,'back')">
          Back
        </v-btn>

        <v-btn
          @click="makeStep(2,'continue')"
          color="primary"
        >
          Continue
        </v-btn>

        <v-btn text @click="makeStep(2,'cancel')">
          Cancel
        </v-btn>
      </v-stepper-content>

      <v-stepper-content step="3">
        <v-card
          class="mb-12"
          color="grey lighten-1"
          height="500px"
        ></v-card>
        <v-btn
          color="primary"
          @click="makeStep(3,'back')"
        >
          Back
        </v-btn>

        <v-btn text @click="makeStep(3,'restart')">
          Restart
        </v-btn>
      </v-stepper-content>
    </v-stepper-items>


  </v-stepper>
</template>

<script>
import Utils from "../../../scripts/Utils";

export default {
  name: "ModuleIdentification",

  props: {
    blitz: Boolean,
  },


  data() {
    return {
      seedTypeId: undefined,
      seeds: [],
      method: undefined,
      sourceType: undefined,
      step: 1,
      highlighted: []
    }
  },

  created() {
    this.init()
  },

  methods: {

    init: function () {
      this.sources = undefined;
      this.method = undefined;
      this.sourceType = undefined
      this.step = 1
      this.seedTypeId = undefined
      this.seeds = []
      this.highlighted =[]
    },

    makeStep: function (s, button) {
      if (button === "continue") {
        this.step++
        if (this.step === 2 && this.blitz)
          this.step++
      }
      if (button === "back") {
        this.step--
        if (this.step === 2 && this.blitz)
          this.step--
      }

      if (button === "cancel") {
        this.init()
        this.$emit("resetEvent")
      }

    },

    onFileSelected: function (file) {
      if(file==null)
        return
      this.highlighted=[]
      Utils.readFile(file).then(content => {
        this.$http.post("mapFileListToItems", {
          type: ['gene', 'protein'][this.seedTypeId],
          file: content
        }).then(response => {
          if (response.data)
            return response.data
        }).then(data => {
          let ids = this.seeds.map(seed=>seed.id)
          data.forEach(e => {
            if (ids.indexOf(e.id) === -1) {
              this.seeds.push(e)
              this.highlighted.push(e.id)
            }
          })
        }).catch(console.log)
      }).then(()=>{
        console.log(this.seeds)
      }).catch(console.log)
    }
  }
}
</script>

<style scoped>

</style>
