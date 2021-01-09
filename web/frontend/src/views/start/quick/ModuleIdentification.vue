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
          height="700px"
        >

          <v-card-subtitle class="headline">1. Seed Configuration</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">Add seeds to your list or leave it empty if you plan to select
            BiCon as your Algorithm.
          </v-card-subtitle>
          <v-container style="height: 80%">
            <v-row style="height: 100%">
              <v-col cols="6">
                <v-container>
                  <v-card-title style="margin-left: -25px">Select the seed type</v-card-title>
                  <v-card-text>
                    <v-radio-group row v-model="seedTypeId" :disabled="seeds!==undefined && seeds.length>0">
                      <v-radio label="Gene">
                      </v-radio>
                      <v-radio label="Protein">
                      </v-radio>
                    </v-radio-group>
                  </v-card-text>
                </v-container>
                <v-container v-if="seedTypeId!==undefined">
                  <v-card-title style="margin-left: -25px">Add seeds</v-card-title>
                  <v-row>
                    <v-col cols="3">
                      <v-select :items="getSuggestionSelection()" v-model="suggestionType"
                                placeholder="connected to"></v-select>
                    </v-col>
                    <v-col>
                      <v-autocomplete
                        clearable
                        :search-input.sync="nodeSuggestions"
                        :disabled="suggestionType===undefined || suggestionType<0"
                        :loading="suggestions.loading"
                        :items="suggestions.data"
                        v-model="suggestionModel"
                        label="by suggestions"
                        class="mx-4"
                        return-object
                      >
                        <template v-slot:item="{ item }">
                          <v-list-item-avatar
                          >
                            <v-icon v-if="item.type==='DOMAIN_ID'">fas fa-fingerprint</v-icon>
                            <v-icon v-if="item.type==='DISPLAY_NAME' || item.type==='SYMBOLS'">fas fa-tv</v-icon>
                            <v-icon v-if="item.type==='ICD10'">fas fa-disease</v-icon>
                            <v-icon v-if="item.type==='SYNONYM'">fas fa-sync</v-icon>
                            <v-icon v-if="item.type==='IUPAC'">mdi-molecule</v-icon>
                            <v-icon v-if="item.type==='ORIGIN'">fas fa-dna</v-icon>
                            <v-icon v-if="item.type==='DESCRIPTION' || item.type==='COMMENTS'">fas fa-info</v-icon>
                            <v-icon v-if="item.type==='INDICATION'">fas fa-pills</v-icon>
                            <v-icon v-if="item.type==='TYPE' || item.type==='GROUP' || item.type==='CATEGORY'">fas
                              fa-layer-group
                            </v-icon>
                          </v-list-item-avatar>
                          <v-list-item-content>
                            <v-list-item-title v-text="item.text"></v-list-item-title>
                            <v-list-item-subtitle
                              v-text="item.type"></v-list-item-subtitle>
                          </v-list-item-content>
                          <v-list-item-action>
                            <v-chip>
                              {{ item.ids.length }}
                            </v-chip>
                          </v-list-item-action>
                        </template>
                      </v-autocomplete>
                    </v-col>
                  </v-row>
                  <v-card-subtitle>or</v-card-subtitle>
                  <v-file-input :label="'by '+['entrez','uniprot'][seedTypeId]+' ids'"
                                v-on:change="onFileSelected"
                                show-size
                                prepend-icon="far fa-list-alt"
                                v-model="fileInputModel"
                                dense
                  ></v-file-input>
                </v-container>
              </v-col>

              <v-divider vertical></v-divider>
              <v-col cols="5">
                <v-card-title class="subtitle-1">Selected Seeds ({{ seeds.length }})
                </v-card-title>
                <v-list max-height="45vh" height="45vh" class="overflow-y-auto">
                  <v-list-item v-for="(seed,index) in seeds" :key="seed.id">
                    <v-list-item-icon>
                      <v-icon v-if="highlighted.indexOf(seed.id)!==-1">fas fa-star</v-icon>
                    </v-list-item-icon>
                    <v-list-item-title>{{ seed.displayName }}</v-list-item-title>
                    <v-list-item-action>
                      <v-btn icon @click="seeds.splice(index,1)" color="red">
                        <v-icon>far fa-minus-square</v-icon>
                      </v-btn>
                    </v-list-item-action>
                  </v-list-item>
                </v-list>
                <v-chip outlined v-show="seeds.length>0" style="margin-top:15px" @click="downloadList">
                  <v-icon left>fas fa-download</v-icon>
                  Save
                </v-chip>
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
          height="700px"
        >
          <v-card-subtitle class="headline">2. Algorithm Selection</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">Select and adjust the algorithm you want to use on your seeds to
            identify the disease module.
          </v-card-subtitle>
          <v-container style="height: 80%">
            <v-row style="height: 100%">
              <v-col>
              <v-card-title style="margin-left: -25px">Select the Base-Algorithm</v-card-title>
              <v-radio-group v-model="methodModel" row>
                <v-radio v-for="method in methods"
                         :label="method.label"
                         :key="method.label"
                >
                </v-radio>
              </v-radio-group>
                <template v-if="methodModel!==undefined">
                <v-card-title  style="margin-left:-25px">Configure Parameters</v-card-title>
                  <v-row>
                    <v-col>
                      <v-switch
                      label="Only Experimental Validated Interactions"
                      v-model="experimentalSwitch"
                      >
                      </v-switch>

                    </v-col>


                  </v-row>
                <template v-if="methods[methodModel].id==='bicon'">
                  <v-row>
                    <v-col>
                      <v-range-slider
                        hide-details
                        class="align-center"
                        v-model="models.bicon.lg"
                        min="1"
                        max="1000"
                      >
                        <template v-slot:prepend>
                          <v-text-field
                            v-model="models.bicon.lg[0]"
                            class="mt-0 pt-0"
                            type="number"
                            style="width: 60px"
                            label="min"
                          ></v-text-field>
                        </template>
                        <template v-slot:append>
                          <v-text-field
                            v-model="models.bicon.lg[1]"
                            class="mt-0 pt-0"
                            type="number"
                            style="width: 60px"
                            label="max"
                          ></v-text-field>
                          <v-tooltip left>
                            <template v-slot:activator="{ on, attrs }">
                              <v-icon
                                v-bind="attrs"
                                v-on="on"
                                left> far fa-question-circle
                              </v-icon>
                            </template>
                            <span>Maximal solution subnetwork size.</span>
                          </v-tooltip>
                        </template>
                      </v-range-slider>
                    </v-col>
                  </v-row>

                </template>
                <template v-if="methods[methodModel].id==='diamond'">
                  <v-row>
                    <v-col>
                      <v-slider
                        hide-details
                        class="align-center"
                        v-model="models.diamond.nModel"
                        min="1"
                        max="1000"
                      >
                        <template v-slot:prepend>
                          <v-text-field
                            v-model="models.diamond.nModel"
                            class="mt-0 pt-0"
                            type="number"
                            style="width: 60px"
                            label="n"
                          ></v-text-field>
                        </template>
                        <template v-slot:append>
                          <v-tooltip left>
                            <template v-slot:activator="{ on, attrs }">
                              <v-icon
                                v-bind="attrs"
                                v-on="on"
                                left> far fa-question-circle
                              </v-icon>
                            </template>
                            <span>Desired number of DIAMOnD genes, 200 is a reasonable
                       starting point.</span>
                          </v-tooltip>
                        </template>
                      </v-slider>
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <v-slider
                        hide-details
                        class="align-center"
                        v-model="models.diamond.alphaModel"
                        min="1"
                        max="100"
                      >
                        <template v-slot:prepend>
                          <v-text-field
                            v-model="models.diamond.alphaModel"
                            class="mt-0 pt-0"
                            type="number"
                            style="width: 60px"
                            label="alpha"
                          ></v-text-field>
                        </template>
                        <template v-slot:append>
                          <v-tooltip left>
                            <template v-slot:activator="{ on, attrs }">
                              <v-icon
                                v-bind="attrs"
                                v-on="on"
                                left> far fa-question-circle
                              </v-icon>
                            </template>
                            <span>an integer representing weight of the seeds,default
                       value is set to 1.</span>
                          </v-tooltip>
                        </template>
                      </v-slider>
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <v-slider
                        hide-details
                        class="align-center"
                        v-model="models.diamond.pModel"
                        min="-100"
                        max="0"
                      >
                        <template v-slot:prepend>
                          <v-text-field
                            prefix="10^"
                            v-model="models.diamond.pModel"
                            class="mt-0 pt-0"
                            type="number"
                            style="width: 80px"
                            label="p-cutoff"
                          ></v-text-field>
                        </template>
                        <template v-slot:append>
                          <v-tooltip left>
                            <template v-slot:activator="{ on, attrs }">
                              <v-icon
                                v-bind="attrs"
                                v-on="on"
                                left> far fa-question-circle
                              </v-icon>
                            </template>
                            <span>Choose a cutoff for the resulting p_hyper scores.</span>
                          </v-tooltip>
                        </template>
                      </v-slider>
                    </v-col>
                  </v-row>
                </template>
                <template v-if="methods[methodModel].id==='must'">
                  <v-row>
                    <v-col>
                      <v-slider
                        hide-details
                        class="align-center"
                        v-model="models.must.hubpenalty"
                        min="0"
                        max="1"
                        step="0.01"
                      >
                        <template v-slot:prepend>
                          <v-text-field
                            v-model="models.must.hubpenalty"
                            class="mt-0 pt-0"
                            type="number"
                            style="width: 70px"
                            label="hub-penalty"
                          ></v-text-field>
                        </template>
                        <template v-slot:append>
                          <v-tooltip left>
                            <template v-slot:activator="{ on, attrs }">
                              <v-icon
                                v-bind="attrs"
                                v-on="on"
                                left> far fa-question-circle
                              </v-icon>
                            </template>
                            <span>Choose this option if you want to return multiple results.</span>
                          </v-tooltip>
                        </template>
                      </v-slider>
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <v-switch
                        label="Multiple"
                        v-model="models.must.multiple"
                      >
                        <template v-slot:append>
                          <v-tooltip left>
                            <template v-slot:activator="{ on, attrs }">
                              <v-icon
                                v-bind="attrs"
                                v-on="on"
                                left> far fa-question-circle
                              </v-icon>
                            </template>
                            <span>Specify hub penalty between 0.0 and 1.0. If none is specified, there will be no hub penalty.</span>
                          </v-tooltip>
                        </template>
                      </v-switch>
                    </v-col>
                  </v-row>
                  <v-row v-show="models.must.multiple">
                    <v-col>
                      <v-slider
                        hide-details
                        class="align-center"
                        v-model="models.must.trees"
                        min="2"
                        max="50"
                      >
                        <template v-slot:prepend>
                          <v-text-field
                            v-model="models.must.trees"
                            class="mt-0 pt-0"
                            type="number"
                            style="width: 70px"
                            label="trees"
                          ></v-text-field>
                        </template>
                        <template v-slot:append>
                          <v-tooltip left>
                            <template v-slot:activator="{ on, attrs }">
                              <v-icon
                                v-bind="attrs"
                                v-on="on"
                                left> far fa-question-circle
                              </v-icon>
                            </template>
                            <span>Number of Trees to be returned (Integer).</span>
                          </v-tooltip>
                        </template>
                      </v-slider>
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <v-slider
                        hide-details
                        class="align-center"
                        v-model="models.must.maxit"
                        min="0"
                        max="20"
                      >
                        <template v-slot:prepend>
                          <v-text-field
                            v-model="models.must.maxit"
                            class="mt-0 pt-0"
                            type="number"
                            style="width: 70px"
                            label="iterations"
                          ></v-text-field>
                        </template>
                        <template v-slot:append>
                          <v-tooltip left>
                            <template v-slot:activator="{ on, attrs }">
                              <v-icon
                                v-bind="attrs"
                                v-on="on"
                                left> far fa-question-circle
                              </v-icon>
                            </template>
                            <span>The maximum number of iterations is defined as trees + iterations.</span>
                          </v-tooltip>
                        </template>
                      </v-slider>
                    </v-col>
                  </v-row>
                </template>
                <v-row>
                  <v-col></v-col>
                </v-row>
                </template>
              </v-col>
            </v-row>
          </v-container>
        </v-card>

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
          height="700px"
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
    metagraph: Object,
  },


  data() {
    return {
      seedTypeId: undefined,
      seeds: [],
      method: undefined,
      sourceType: undefined,
      step: 1,
      highlighted: [],
      suggestionType: undefined,
      suggestions: {loading: false, data: []},
      nodeSuggestions: null,
      suggestionModel: null,
      fileInputModel: undefined,
      methods: [{id: "diamond", label: "DIAMOnD"}, {id: "bicon", label: "BiCoN"}, {id: "must", label: "MuST"}],
      methodModel: undefined,
      experimentalSwitch:true,
      models: {
        // advanced: {
        //   keepNodesOnly: true,
        //   addInteractions: true
        // },
        diamond: {
          nModel: 200,
          alphaModel: 1,
          pModel: 0
        },
        bicon: {
          exprFile: undefined,
          lg: [10, 15]
        },
        must: {
          hubpenalty: 0,
          multiple: false,
          trees: 10,
          maxit: 10,
        }
      }
    }
  },
  watch: {

    nodeSuggestions: function (val) {
      let name = this.suggestionType
      if (this.suggestions.chosen !== undefined)
        return
      this.suggestions.loading = true;
      this.suggestions.data = []
      this.$http.post("getSuggestions", {
        name: name,
        query: val,
      }).then(response => {
        if (response.data !== undefined) {
          return response.data
        }
      }).then(data => {
        data.suggestions.sort((e1, e2) => {
          return e2.ids.length - e1.ids.length
        })
        this.suggestions.data = data.suggestions;
      }).catch(err =>
        console.log(err)
      ).finally(() =>
        this.suggestions.loading = false
      )
    },
    suggestionModel: function (val) {
      if (val !== undefined && val != null) {
        this.$http.post("getConnectedNodes", {
          sourceType: this.suggestionType,
          targetType: ["gene", "protein"][this.seedTypeId],
          sourceIds: val.ids,
          noloop: ["gene", "protein"][this.seedTypeId] === this.suggestionType
        }).then(response => {
          if (response.data !== undefined)
            return response.data
        }).then(data => {
          this.addToSelection(data, val.text + "[" + val.type + "]")
        }).then(() => {
          this.suggestionModel = undefined
        }).catch(console.log)
      }
    },

  },

  created() {
    this.init()

    //TODO dev
    this.seedTypeId = 0
    this.seeds = [{
      "primaryDomainId": "entrez.3757",
      "displayName": "KCNH2",
      "id": 19888
    }, {"primaryDomainId": "entrez.5005", "displayName": "ORM2", "id": 54656}, {
      "primaryDomainId": "entrez.4988",
      "displayName": "OPRM1",
      "id": 13457
    }, {"primaryDomainId": "entrez.4985", "displayName": "OPRD1", "id": 13458}, {
      "primaryDomainId": "entrez.4986",
      "displayName": "OPRK1",
      "id": 13459
    }, {"primaryDomainId": "entrez.23643", "displayName": "LY96", "id": 1413}, {
      "primaryDomainId": "entrez.3359",
      "displayName": "HTR3A",
      "id": 29783
    }, {"primaryDomainId": "entrez.57053", "displayName": "CHRNA10", "id": 1177}, {
      "primaryDomainId": "entrez.116443",
      "displayName": "GRIN3A",
      "id": 50124
    }]


  },

  methods: {

    init: function () {
      this.method = undefined;
      this.sourceType = undefined
      this.step = 1
      this.seedTypeId = undefined
      this.seeds = []
      this.highlighted = []
    },

    getSuggestionSelection: function () {
      let type = ["gene", "protein"][this.seedTypeId]
      let nodeId = this.metagraph.nodes.filter(n => n.group === type)[0].id
      return this.metagraph.edges.filter(e => e.from !== e.to && e.from === nodeId || e.to === nodeId).map(e => e.to === nodeId ? e.from : e.to).map(nid => {
        let node = this.metagraph.nodes.filter(n => n.id === nid)[0]
        return {value: node.group, text: node.label}
      })

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

    addToSelection: function (list, nameFrom) {
      let ids = this.seeds.map(seed => seed.id)
      let count = 0
      list.forEach(e => {
        if (ids.indexOf(e.id) === -1) {
          count++
          this.seeds.push(e)
          this.highlighted.push(e.id)
        }
      })
      this.$emit("printNotificationEvent", "Added " + list.length + "from " + nameFrom + " (" + count + " new) seeds!", 1)
    },

    onFileSelected: function (file) {
      if (file == null)
        return
      this.highlighted = []
      Utils.readFile(file).then(content => {
        this.$http.post("mapFileListToItems", {
          type: ['gene', 'protein'][this.seedTypeId],
          file: content
        }).then(response => {
          if (response.data)
            return response.data
        }).then(data => {
          this.addToSelection(data, file.name)
        }).then(() => {
          this.fileInputModel = undefined
        }).catch(console.log)
      }).catch(console.log)
    },

    downloadList: function () {
      this.$http.post("mapToDomainIds", {
        type: ['gene', 'protein'][this.seedTypeId],
        ids: this.seeds.map(s => s.id)
      }).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        let text = "";
        data.forEach(id => text += id + "\n")
        this.download(["gene", "protein"][this.seedTypeId] + "s.tsv", text)
      }).catch(console.log)
    },
    download: function (name, content) {
      let dl = document.createElement('a')
      dl.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(content))
      dl.setAttribute('download', name)
      dl.style.direction = 'none'
      document.body.appendChild(dl)
      dl.click()
      document.body.removeChild(dl)
    }
  }
}
</script>

<style scoped>

</style>
