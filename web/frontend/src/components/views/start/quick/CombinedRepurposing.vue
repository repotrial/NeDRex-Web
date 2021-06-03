<template>
  <v-stepper
    alt-labels
    v-model="step"
  >
    <v-stepper-header ref="head">
      <v-stepper-step step="1" :complete="step>1">
        Select Seeds
        <small v-if="seedTypeId!==undefined">{{ ["Gene", "Protein"][seedTypeId] }} ({{ seeds.length }})</small>
      </v-stepper-step>
      <v-divider></v-divider>
      <v-stepper-step step="2" :complete="step>2 || blitz">
        Module Method
        <small v-if="moduleMethodModel>-1">{{ moduleMethods[moduleMethodModel].label }}</small>
      </v-stepper-step>
      <v-divider></v-divider>
      <v-stepper-step step="3" :complete="step>3 || blitz">
        Ranking Method
        <small v-if="rankingMethodModel>-1">{{ rankingMethods[rankingMethodModel].label }}</small>
      </v-stepper-step>
      <v-divider></v-divider>
      <v-stepper-step step="4">
        Results
      </v-stepper-step>
    </v-stepper-header>

    <v-stepper-items>
      <v-stepper-content step="1">
        <v-card
          v-if="step===1"
          class="mb-12"
          max-height="80vh"
        >

          <v-card-subtitle class="headline">1. Seed Configuration</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">Add seeds to your
            list{{ blitz ? "." : " or leave it empty if you plan to select BiCon as your Algorithm." }} Use the
            autocomplete system or the id list upload to add seed entries. The list can be manually adjusted or the
            intersection of multiple sources may be created.
          </v-card-subtitle>
          <v-row v-show="!blitz">
            <v-col>
              <v-list-item-action>
                <v-chip
                  color="primary"
                  @click="seedTypeId=0; methodModel=1; makeStep(1,'continue')"
                  style="margin-top:-25px"
                >
                  Skip to BiCon
                </v-chip>
              </v-list-item-action>
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-list-item-subtitle class="title">Select the seed type</v-list-item-subtitle>
              <v-list-item-action>
                <v-radio-group row v-model="seedTypeId" :disabled="seeds!==undefined && seeds.length>0">
                  <v-radio label="Gene">
                  </v-radio>
                  <v-radio label="Protein">
                  </v-radio>
                </v-radio-group>
              </v-list-item-action>
            </v-col>
          </v-row>
          <v-container style="height: 55vh">
            <v-row style="height: 100%">
              <v-col cols="6">
                <v-container v-if="seedTypeId!==undefined">
                  <v-card-title style="margin-left: -25px">Add seeds associated to</v-card-title>
                  <v-row>
                    <v-col cols="3">
                      <v-select :items="getSuggestionSelection()" v-model="suggestionType"
                                placeholder="connected to" style="width: 100%"></v-select>
                    </v-col>
                    <v-col cols="6">
                      <SuggestionAutocomplete :suggestion-type="suggestionType"
                                              :target-node-type="['gene', 'protein'][this.seedTypeId]"
                                              @addToSelectionEvent="addToSelection"></SuggestionAutocomplete>
                    </v-col>
                  </v-row>
                  <v-card-subtitle>or</v-card-subtitle>
                  <v-file-input :label="'by '+['entrez','uniprot'][seedTypeId]+' ids'"
                                v-on:change="onFileSelected"
                                show-size
                                prepend-icon="far fa-list-alt"
                                v-model="fileInputModel"
                                dense
                                style="width: 75%"
                  ></v-file-input>
                </v-container>
              </v-col>

              <v-divider vertical></v-divider>
              <v-col cols="5">
                <v-card-title class="subtitle-1">Selected Seeds ({{ seeds.length }})
                </v-card-title>
                <v-list ref="seedlist" max-height="40vh" height="40vh" class="overflow-y-auto">
                  <v-list-item v-for="(seed,index) in seeds" :key="seed.id">
                    <v-list-item-title>{{ $utils.adjustLabels(seed.displayName) }}</v-list-item-title>
                    <v-list>
                      <template v-for="o in getOrigins(seed.id)">
                        <v-list-item-subtitle :key="seed.id+o">{{ o }}</v-list-item-subtitle>
                      </template>
                    </v-list>
                    <v-list-item-action>
                      <v-btn icon @click="removeSeed(index,seed.id)" color="red">
                        <v-icon>far fa-trash-alt</v-icon>
                      </v-btn>
                    </v-list-item-action>
                  </v-list-item>
                </v-list>
                <v-chip outlined v-show="seeds.length>0" style="margin-top:15px" @click="downloadSeedList">
                  <v-icon left>fas fa-download</v-icon>
                  Save
                </v-chip>
                <v-chip outlined v-show="seeds.length>0" style="margin-top:15px" @click="clearList">
                  <v-icon left>fas fa-trash-alt</v-icon>
                  Clear
                </v-chip>
                <v-chip outlined v-show="seeds.length>0" style="margin-top:15px" @click="removeNonIntersecting()">
                  <v-icon left>fas fa-minus-square</v-icon>
                  Remove non-intersecting
                </v-chip>
              </v-col>
            </v-row>
          </v-container>


        </v-card>
        <v-btn
          color="primary"
          @click="makeStep(1,'continue')"
          :disabled="seedTypeId<0"
        >
          Continue
        </v-btn>

        <v-btn text @click="makeStep(1,'cancel')">
          Cancel
        </v-btn>
      </v-stepper-content>

      <v-stepper-content step="2">
        <v-card
          v-if="step===2"
          class="mb-12"
          height="700px"
        >
          <v-card-subtitle class="headline">2. Module Identification Algorithm Selection</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">Select and adjust the algorithm you want to apply on your seeds to
            construct a disease module.
          </v-card-subtitle>
          <v-container style="height: 80%">
            <v-row style="height: 100%">
              <v-col>
                <v-card-title style="margin-left: -25px">Select the Base-Algorithm</v-card-title>
                <v-radio-group v-model="moduleMethodModel" row>
                  <v-radio v-for="method in moduleMethods"
                           :label="method.label"
                           :key="method.label"
                           :disabled="method.id!=='bicon'&&seeds.length===0"
                  >
                  </v-radio>
                </v-radio-group>
                <template v-if="moduleMethodModel!==undefined">
                  <v-card-title style="margin-left:-25px">Configure Parameters</v-card-title>
                  <span>
                      <v-switch
                        :label="'Only use experimentally validated '+['Gene', 'Protein'][seedTypeId]+'-'+['Gene', 'Protein'][seedTypeId]+' interactions'"
                        v-model="experimentalSwitch"
                      >
                        <template v-slot:append>
                          <v-tooltip left>
                            <template v-slot:activator="{ on, attrs }">
                              <v-icon
                                v-bind="attrs"
                                v-on="on"
                                left> far fa-question-circle
                                style="flex-grow: 0"
                              </v-icon>
                            </template>
                            <span>Restricts the edges in the {{
                                ['Gene', 'Protein'][seedTypeId] + '-' + ['Gene', 'Protein'][seedTypeId]
                              }} interaction network to experimentally validated ones.</span>
                          </v-tooltip>
                        </template>
                      </v-switch>
                    </span>

                  <template v-if="moduleMethods[moduleMethodModel].id==='bicon'">
                    <v-file-input
                      v-on:change="biconFile"
                      show-size
                      prepend-icon="fas fa-file-medical"
                      label="Expression File"
                      dense
                    >
                    </v-file-input>
                    <v-row>
                      <v-col>
                        <v-range-slider
                          hide-details
                          class="align-center"
                          v-model="moduleModels.bicon.lg"
                          min="1"
                          max="1000"
                        >
                          <template v-slot:prepend>
                            <v-text-field
                              v-model="moduleModels.bicon.lg[0]"
                              class="mt-0 pt-0"
                              type="number"
                              style="width: 60px"
                              label="min"
                            ></v-text-field>
                          </template>
                          <template v-slot:append>
                            <v-text-field
                              v-model="moduleModels.bicon.lg[1]"
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
                  <template v-if="moduleMethods[moduleMethodModel].id==='diamond'">
                    <v-row>
                      <v-col>
                        <v-slider
                          hide-details
                          class="align-center"
                          v-model="moduleModels.diamond.nModel"
                          min="1"
                          max="1000"
                        >
                          <template v-slot:prepend>
                            <v-text-field
                              v-model="moduleModels.diamond.nModel"
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
                          v-model="moduleModels.diamond.alphaModel"
                          min="1"
                          max="100"
                        >
                          <template v-slot:prepend>
                            <v-text-field
                              v-model="moduleModels.diamond.alphaModel"
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
                          v-model="moduleModels.diamond.pModel"
                          min="-100"
                          max="0"
                        >
                          <template v-slot:prepend>
                            <v-text-field
                              prefix="10^"
                              v-model="moduleModels.diamond.pModel"
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
                  <template v-if="moduleMethods[moduleMethodModel].id==='must'">
                    <v-row>
                      <v-col>
                        <v-slider
                          hide-details
                          class="align-center"
                          v-model="moduleModels.must.hubpenalty"
                          min="0"
                          max="1"
                          step="0.01"
                        >
                          <template v-slot:prepend>
                            <v-text-field
                              v-model="moduleModels.must.hubpenalty"
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
                          v-model="moduleModels.must.multiple"
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
                    <v-row v-show="moduleModels.must.multiple">
                      <v-col>
                        <v-slider
                          hide-details
                          class="align-center"
                          v-model="moduleModels.must.trees"
                          min="2"
                          max="50"
                        >
                          <template v-slot:prepend>
                            <v-text-field
                              v-model="moduleModels.must.trees"
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
                          v-model="moduleModels.must.maxit"
                          min="0"
                          max="20"
                        >
                          <template v-slot:prepend>
                            <v-text-field
                              v-model="moduleModels.must.maxit"
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
          :disabled="moduleMethodModel===undefined ||(moduleMethodModel===1 && moduleModels.bicon.exprFile ===undefined)"
        >
          Continue
        </v-btn>

        <v-btn text @click="makeStep(2,'cancel')">
          Cancel
        </v-btn>
      </v-stepper-content>

      <v-stepper-content step="3">
        <v-card
          v-if="step===3"
          class="mb-12"
          height="700px"
        >
          <v-card-subtitle class="headline">3. Drug Ranking Algorithm Selection</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">Select and adjust the algorithm you want to apply on the
            constructed module to
            identify and rank drug candidates.
          </v-card-subtitle>
          <v-container style="height: 80%">
            <v-row style="height: 100%">
              <v-col>
                <v-card-title style="margin-left: -25px">Select the Base-Algorithm</v-card-title>
                <v-radio-group v-model="rankingMethodModel" row>
                  <v-radio v-for="method in rankingMethods"
                           :label="method.label"
                           :key="method.label"
                  >
                  </v-radio>
                </v-radio-group>
                <template v-if="rankingMethodModel!==undefined">
                  <v-card-title style="margin-left:-25px">Configure Parameters</v-card-title>
                  <v-row>
                    <v-col>
                      <v-slider
                        hide-details
                        class="align-center"
                        v-model="rankingModels.topX"
                        step="1"
                        min="1"
                        max="2000"
                      >
                        <template v-slot:prepend>
                          <v-text-field
                            v-model="rankingModels.topX"
                            class="mt-0 pt-0"
                            type="integer"
                            style="width: 100px"
                            label="visualize topX"
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
                            <span>A integer X limiting the visualization to the top X drugs that were found.</span>
                          </v-tooltip>
                        </template>
                      </v-slider>
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <v-switch
                        :label="'Only use experimentally validated '+['Gene', 'Protein'][seedTypeId]+'-'+['Gene', 'Protein'][seedTypeId]+' interactions'"
                        v-model="experimentalSwitch"
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
                            <span>Restricts the edges in the {{
                                ['Gene', 'Protein'][seedTypeId] + '-' + ['Gene', 'Protein'][seedTypeId]
                              }} network to experimentally validated ones.</span>
                          </v-tooltip>
                        </template>
                      </v-switch>

                    </v-col>


                  </v-row>
                  <v-row>
                    <v-col>
                      <div style="">
                        <v-switch
                          label="Only direct Drugs"
                          v-model="rankingModels.onlyDirect"
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
                              <span>If only the drugs interacting directly with seeds should be considered in the ranking,
                             this should be selected. If including the non-direct drugs is desired unselect.</span>
                            </v-tooltip>
                          </template>
                        </v-switch>
                      </div>
                    </v-col>
                    <v-col>
                      <v-switch
                        label="Only approved Drugs"
                        v-model="rankingModels.onlyApproved"
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
                            <span>If only approved drugs should be considered in the ranking,
                             this should be selected. If including all approved and unapproved drugs is desired unselect.</span>
                          </v-tooltip>
                        </template>
                      </v-switch>
                    </v-col>
                    <v-col>
                      <v-switch
                        label="Filter Element 'Drugs'"
                        v-model="rankingModels.filterElements"
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
                            <span>Filter often used drugs like Zinc, Gold, Copper,....</span>
                          </v-tooltip>
                        </template>
                      </v-switch>
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <v-slider
                        v-show="rankingMethodModel===0"
                        hide-details
                        class="align-center"
                        v-model="rankingModels.damping"
                        step="0.01"
                        min="0"
                        max="1"
                      >
                        <template v-slot:prepend>
                          <v-text-field
                            v-model="rankingModels.damping"
                            class="mt-0 pt-0"
                            type="float"
                            style="width: 60px"
                            label="damping-factor"
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
                            <span>A float value between 0-1 to be used as damping factor parameter.</span>
                          </v-tooltip>
                        </template>
                      </v-slider>
                    </v-col>
                  </v-row>

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
          :disabled="moduleMethodModel===undefined"
        >
          Run
        </v-btn>

        <v-btn text @click="makeStep(2,'cancel')">
          Cancel
        </v-btn>
      </v-stepper-content>

      <v-stepper-content step="4">
        <v-card
          v-if="step===4"
          class="mb-12"
          height="80vh"
        >
          <v-card-subtitle class="headline">4. Drug Repurposing Results</v-card-subtitle>
          <v-container>
            <v-row>
              <v-col cols="3" style="padding: 0 50px 0 0; margin-right: -50px">
                <v-card-title class="subtitle-1">Seeds ({{ seeds.length }}) {{
                    (results.targets.length !== undefined && results.targets.length > 0 ? ("& Module (" + getTargetCount() + ")") : ": Processing")
                  }}
                  <v-progress-circular indeterminate v-if="this.results.targets.length===0" style="margin-left:15px">
                  </v-progress-circular>
                </v-card-title>
                <v-data-table max-height="45vh" height="45vh" class="overflow-y-auto" fixed-header dense item-key="id"
                              :items="(!results.targets ||results.targets.length ===0) ?seeds : results.targets"
                              :headers="getHeaders(0)"
                              disable-pagination
                              hide-default-footer @click:row="seedClicked">
                      <template v-slot:item.displayName="{item}">
                        <v-tooltip v-if="item.displayName.length>16" right>
                          <template v-slot:activator="{attr,on }">
                          <span v-bind="attr" v-on="on"
                                style="color: dimgray">{{ item.displayName.substr(0, 16) }}...</span>
                          </template>
                          <span>{{item.displayName}}</span>
                        </v-tooltip>
                        <span v-else>{{ item.displayName }}</span>
                      </template>
                </v-data-table>
                <template v-if="results.targets.length>=0">

                  <v-chip outlined style="margin-top:15px" @click="downloadSeedList">
                    <v-icon left>fas fa-download</v-icon>
                    Save Seeds
                  </v-chip>
                  <v-chip outlined style="margin-top:15px"
                          @click="downloadModuleResultList">
                    <v-icon left>fas fa-download</v-icon>
                    Save Module
                  </v-chip>
                  <v-chip outlined style="margin-top:15px"
                          @click="downloadFullResultList(moduleJid)" v-if="results.targets.length>0">
                    <v-icon left>fas fa-download</v-icon>
                    Save Raw Results
                  </v-chip>
                </template>
              </v-col>
              <v-col>
                <Graph ref="graph" :configuration="graphConfig" :window-style="graphWindowStyle"
                       :progress="resultProgress" :legend="resultProgress===100" :meta="metagraph">
                  <template v-slot:legend v-if="results.drugs.length>0">
                    <v-card style="width: 11vw; max-width: 20vw; padding-top: 35px">
                      <v-list dense>
                        <v-list-item>
                          <v-list-item-icon>
                            <v-icon left :color="getColoring('nodes',['gene','protein'][seedTypeId],'light')"
                                    size="43px">fas fa-genderless
                            </v-icon>
                          </v-list-item-icon>
                          <v-list-item-title style="margin-left:-25px">{{ ['Gene', 'Protein'][seedTypeId] }} (Seed)
                          </v-list-item-title>
                          <v-list-item-subtitle style="margin-right:-25px; margin-left:-25px">{{
                              seeds.length
                            }}
                          </v-list-item-subtitle>
                        </v-list-item>
                        <v-list-item>
                          <v-list-item-icon>
                            <v-icon left :color="getColoring('nodes',['gene','protein'][seedTypeId],'light')">fas
                              fa-circle
                            </v-icon>
                          </v-list-item-icon>
                          <v-list-item-title style="margin-left:-25px">{{ ['Gene', 'Protein'][seedTypeId] }} (Module)
                          </v-list-item-title>
                          <v-list-item-subtitle style="margin-right:-25px; margin-left:-25px">
                            {{ results.targets.length }}
                          </v-list-item-subtitle>
                        </v-list-item>
                        <v-list-item>
                          <v-list-item-icon>
                            <v-icon left :color="getColoring('nodes','drug','light')" size="43px">fas fa-genderless
                            </v-icon>
                          </v-list-item-icon>
                          <v-list-item-title style="margin-left:-25px">Drug</v-list-item-title>
                          <v-list-item-subtitle style="margin-right:-25px; margin-left:-25px">
                            {{ results.drugs.length }}
                          </v-list-item-subtitle>
                        </v-list-item>
                      </v-list>
                    </v-card>
                  </template>

                </Graph>
              </v-col>
              <v-col cols="2" style="padding:0">
                <v-card-title class="subtitle-1"> Drugs{{
                    (results.drugs.length !== undefined && results.drugs.length > 0 ? (" (" + (results.drugs.length) + ")") : ": Processing")
                  }}
                  <v-progress-circular indeterminate v-if="results.drugs.length===0" style="margin-left:15px">
                  </v-progress-circular>
                </v-card-title>
                <template v-if="results.drugs.length>0">
                  <v-data-table max-height="45vh" height="45vh" class="overflow-y-auto" fixed-header dense item-key="id"
                                :items="results.drugs"
                                :headers="getHeaders(1)"
                                disable-pagination
                                hide-default-footer @click:row="drugClicked">
                    <template v-slot:item.displayName="{item}">
                      <v-tooltip v-if="item.displayName.length>16" right>
                        <template v-slot:activator="{attr,on }">
                          <span v-bind="attr" v-on="on"
                                style="color: dimgray">{{ item.displayName.substr(0, 16) }}...</span>
                        </template>
                        <span>{{item.displayName}}</span>
                      </v-tooltip>
                      <span v-else>{{ item.displayName }}</span>
                    </template>
                  </v-data-table>
                  <v-chip outlined style="margin-top:15px"
                          @click="downloadRankingResultList" v-if="results.drugs.length>0">
                    <v-icon left>fas fa-download</v-icon>
                    Save Top {{ rankingModels.topX }} Drugs
                  </v-chip>
                  <v-chip outlined style="margin-top:15px"
                          @click="downloadFullResultList(rankingJid)" v-if="results.drugs.length>0">
                    <v-icon left>fas fa-download</v-icon>
                    Save Complete Drug Ranking
                  </v-chip>
                </template>
              </v-col>
            </v-row>
            <v-divider style="margin-top:10px"></v-divider>
            <v-row>
              <v-col>
                <v-chip outlined v-if="rankingJid!=null && rankingGid !=null"
                        style="margin-top:15px"
                        @click="$emit('graphLoadEvent',{post: {id: rankingGid}})">
                  <v-icon left>fas fa-angle-double-right</v-icon>
                  Load Result into Advanced View
                </v-chip>
              </v-col>
              <v-col>
                <v-switch label="Physics" v-model="graph.physics" @click="updateGraphPhysics()"
                          v-if="resultProgress===100">
                </v-switch>

              </v-col>
            </v-row>
          </v-container>
        </v-card>
        <v-btn
          color="primary"
          @click="makeStep(4,'back')"
        >
          Back
        </v-btn>

        <v-btn text @click="makeStep(4,'cancel')">
          Restart
        </v-btn>
      </v-stepper-content>
    </v-stepper-items>


  </v-stepper>
</template>

<script>
import Graph from "../../graph/Graph";
import * as CONFIG from "../../../../Config"
import SuggestionAutocomplete from "@/components/app/suggestions/SuggestionAutocomplete";

export default {
  name: "CombinedRepurposing",
  props: {
    blitz: Boolean,
    metagraph: Object,

  },

  sugQuery: undefined,


  data() {
    return {
      resultProgress: 0,
      graphWindowStyle: {
        height: '60vh',
        'min-height': '60vh',
      },
      targetColorStyle: {},
      drugColorStyle: {},
      graphConfig: {visualized: false},
      uid: undefined,
      seedTypeId: undefined,
      seeds: [],
      seedOrigin: {},
      method: undefined,
      sourceType: undefined,
      step: 1,
      suggestionType: undefined,
      fileInputModel: undefined,
      moduleMethods: [{
        id: "diamond",
        label: "DIAMOnD",
        scores: [{id: "rank", name: "Rank"}, {id: "p_hyper", name: "P-Value", decimal: true}]
      }, {id: "bicon", label: "BiCoN", scores: []}, {id: "must", label: "MuST", scores: []}],
      rankingMethods: [
        {id: "trustrank", label: "TrustRank", scores: [{id: "score", name: "Score", decimal: true}]},
        {id: "centrality", label: "Closeness Centrality", scores: [{id: "score", name: "Score", decimal: true}]}],
      graph: {physics: false},
      moduleMethodModel: undefined,
      rankingMethodModel: undefined,
      experimentalSwitch: true,
      results: {seeds: [], targets: []},
      moduleModels: {
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
      },
      rankingModels: {
        topX: 100,
        onlyApproved: true,
        onlyDirect: true,
        damping: 0.85,
        filterElements: true,
      },
      drugTargetPopup: false,
      rankingSelect: 1,
      moduleGid: undefined,
      moduleJid: undefined,
      rankingGid: undefined,
      rankingJid: undefined,
    }
  },

  created() {
    this.$socket.$on("quickModuleFinishedEvent", this.readModuleJob)
    this.$socket.$on("quickRankingFinishedEvent", this.readRankingJob)
    this.uid = this.$cookies.get("uid")
    this.list
    this.init()
  },
  destroyed() {

  },

  methods: {

    init: function () {
      this.method = undefined;
      this.sourceType = undefined
      this.step = 1
      this.seedTypeId = undefined
      this.seeds = []
      this.moduleMethodModel = undefined
      if (this.blitz) {
        this.moduleMethodModel = 0
      }
      this.rankingMethodModel = undefined
      if (this.blitz) {
        this.rankingMethodModel = 1
      }
      this.results.targets = []
      this.results.drugs = []
      this.seedOrigin = {}
      if (this.$refs.graph)
        this.$refs.graph.reload()
      this.moduleJid = undefined
      this.moduleGid = undefined
      this.rankingJid = undefined
      this.rankingGid = undefined
      this.resultProgress = 0
    },
    reset: function () {
      this.init()
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

        if (this.step === 3)
          this.submitModuleAlgorithm()

        if (this.step === 2 && this.blitz) {
          this.submitModuleAlgorithm()
          this.step += 2
        }

        if (this.step === 4)
          this.submitRankingAlgorithm()
        this.focus()

      }
      if (button === "back") {
        this.step--

        if (this.step === 3) {
          this.results.drugs = []
          this.rankingGid = undefined
          this.$refs.graph.reload()
          this.resultProgress = 50
          if (this.rankingJid !== undefined)
            this.$socket.unsubscribeJob(this.rankingJid)
        }

        if (this.step === 2 || (this.step === 3 && this.blitz)) {
          this.resultProgress = 0
          this.results.targets = []
          if (this.moduleJid !== undefined)
            this.$socket.unsubscribeJob(this.moduleJid)
        }
        if (this.step === 3 && this.blitz)
          this.step -= 2

      }

      if (button === "cancel") {
        this.init()
        this.$emit("resetEvent")
      }
    },

    focusNode: function (id) {
      if (this.$refs.graph === undefined)
        return
      this.$refs.graph.setSelection([id])
      this.$refs.graph.zoomToNode(id)
    },
    clearList: function () {
      this.seeds = []
      this.seedOrigin = {}
    },
    removeNonIntersecting: function () {
      let remove = []
      Object.keys(this.seedOrigin).forEach(seed => {
        if (this.seedOrigin[seed].length < 2) {
          delete this.seedOrigin[seed]
          remove.push(parseInt(seed))
        }
      })
      this.seeds = this.seeds.filter(s => remove.indexOf(s.id) === -1)
    },
    // loadDrugTargets: function () {
    //   this.rankingSelect = 1
    //   this.drugTargetPopup = true
    // },
    // resolveRankingDialog: function (apply) {
    //   this.drugTargetPopup = false;
    //   if (!apply)
    //     return
    //
    //   let drugSeeds = []
    //
    //   if (this.rankingSelect === 0) {
    //     drugSeeds = this.seeds.map(s => s.id)
    //   } else {
    //     drugSeeds = this.results.targets.map(t => t.id)
    //     if (this.rankingSelect === 2) {
    //       this.seeds.forEach(s => drugSeeds.splice(drugSeeds.indexOf(s.id), 1))
    //     }
    //   }
    //   this.$emit("loadDrugTargetEvent", this.blitz, drugSeeds, ["gene", "protein"][this.seedTypeId])
    //   this.init()
    // }
    // ,
    biconFile: function (file) {
      this.moduleModels.bicon.exprFile = file
    }
    ,
    updateGraphPhysics: function () {
      this.$refs.graph.setPhysics(this.graph.physics)
    }
    ,
    getTargetCount: function () {
      let seedids = this.seeds.map(s => s.id)
      return this.results.targets.filter(t => seedids.indexOf(t.id) === -1).length
    },
    submitModuleAlgorithm: function () {
      let params = {}
      let method = this.moduleMethods[this.moduleMethodModel].id
      params.experimentalOnly = this.experimentalSwitch

      params["addInteractions"] = true
      params["nodesOnly"] = true
      if (method === 'bicon') {
        this.seeds = []
        params['lg_min'] = this.moduleModels.bicon.lg[0];
        params['lg_max'] = this.moduleModels.bicon.lg[1];
        params['type'] = "gene"
        this.$utils.readFile(this.moduleModels.bicon.exprFile).then(content => {
          params['exprData'] = content
          this.executeModuleJob(method, params)
        })
        return
      }

      if (method === 'diamond') {
        params['n'] = this.moduleModels.diamond.nModel;
        params['alpha'] = this.moduleModels.diamond.alphaModel;
        params['pcutoff'] = this.moduleModels.diamond.pModel;
      }

      if (method === 'must') {
        params["penalty"] = this.moduleModels.must.hubpenalty;
        params["multiple"] = this.moduleModels.must.multiple
        params["trees"] = this.moduleModels.must.trees
        params["maxit"] = this.moduleModels.must.maxit
      }
      params['type'] = ["gene", "protein"][this.seedTypeId]
      this.executeModuleJob(method, params)
    }
    ,
    waitForModuleJob: function (resolve) {
      if (this.moduleGid == null)
        setTimeout(this.waitForModuleJob, 100, resolve)
      else
        resolve()
    },
    submitRankingAlgorithm: function () {
      new Promise(resolve => this.waitForModuleJob(resolve)).then(() => {
        let params = {}
        let method = this.rankingMethods[this.rankingMethodModel].id
        params.experimentalOnly = this.experimentalSwitch

        params["addInteractions"] = true
        params["nodesOnly"] = true

        params['direct'] = this.rankingModels.onlyDirect;
        params['approved'] = this.rankingModels.onlyApproved;
        params['topX'] = this.rankingModels.topX;
        params['elements'] = !this.rankingModels.filterElements;
        if (method === "trustrank")
          params['damping'] = this.rankingModels.damping;

        params['type'] = ["gene", "protein"][this.seedTypeId]
        this.executeRankingJob(method, params)
      })
    },

    executeModuleJob: function (algorithm, params) {
      this.resultProgress = 0
      let payload = {userId: this.uid, algorithm: algorithm, params: params}
      payload.selection = true
      payload.experimentalOnly = params.experimentalOnly
      payload["nodes"] = this.seeds.map(n => n.id)
      this.$http.post("/submitJob", payload).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.$socket.subscribeJob(data.jid, "quickModuleFinishedEvent");
        this.readModuleJob(data, true)
      }).catch(console.log)
    }
    ,
    executeRankingJob: function (algorithm, params) {
      let payload = {userId: this.uid, graphId: this.moduleGid, algorithm: algorithm, params: params}
      payload.selection = false
      payload.experimentalOnly = params.experimentalOnly
      this.$http.post("/submitJob", payload).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.$socket.subscribeJob(data.jid, "quickRankingFinishedEvent");
        this.readRankingJob(data, true)
      }).catch(console.log)
    },
    readModuleJob: function (result, clean) {
      this.resultProgress += 5
      let data = clean ? result : JSON.parse(result)
      this.moduleJid = data.jid
      this.moduleGid = data.gid
      if (this.moduleGid != null && data.state === "DONE") {
        this.$socket.unsubscribeJob(this.moduleJid)
        this.loadModuleTargetTable().then(() => {
          this.resultProgress = 25
          this.loadGraph(this.moduleGid)
        })
      }
    }
    ,
    readRankingJob: function (result, clean) {
      this.resultProgress += 5
      let data = clean ? result : JSON.parse(result)
      this.rankingJid = data.jid
      this.rankingGid = data.gid
      if (this.rankingGid != null && data.state === "DONE") {
        this.resultProgress = 75
        this.$socket.unsubscribeJob(this.rankingJid)
        this.loadRankingTargetTable(this.rankingGid).then(() => {
          this.loadGraph(this.rankingGid)
        })

      }
    },

    addToSelection: function (list, nameFrom) {
      let ids = this.seeds.map(seed => seed.id)
      let count = 0
      list.forEach(e => {
        if (ids.indexOf(e.id) === -1) {
          count++
          this.seeds.push(e)
        }
        if (this.seedOrigin[e.id] !== undefined) {
          if (this.seedOrigin[e.id].indexOf(nameFrom) === -1)
            this.seedOrigin[e.id].push(nameFrom)
        } else
          this.seedOrigin[e.id] = [nameFrom]

      })
      this.$emit("printNotificationEvent", "Added " + list.length + "from " + nameFrom + " (" + count + " new) seeds!", 1)
    }
    ,
    moduleMethodScores: function () {
      if (this.moduleMethodModel !== undefined && this.moduleMethodModel > -1)
        return this.moduleMethods[this.moduleMethodModel].scores;
      return []
    }
    ,
    rankingMethodScores: function () {
      if (this.rankingMethodModel !== undefined && this.rankingMethodModel > -1)
        return this.rankingMethods[this.rankingMethodModel].scores;
      return []
    },

    getOrigins: function (id) {
      if (this.seedOrigin[id] === undefined)
        return ["?"]
      else
        return this.seedOrigin[id]
    }
    ,
    removeSeed: function (index, id) {
      this.seeds.splice(index, 1)
      delete this.seedOrigin[id]
    }
    ,

    onFileSelected: function (file) {
      if (file == null)
        return
      this.$utils.readFile(file).then(content => {
        this.$http.post("mapFileListToItems", {
          type: ['gene', 'protein'][this.seedTypeId],
          file: content
        }).then(response => {
          if (response.data)
            return response.data
        }).then(data => {
          this.addToSelection(data, "FILE:" + file.name)
        }).then(() => {
          this.fileInputModel = undefined
        }).catch(console.log)
      }).catch(console.log)
    }
    ,

    downloadSeedList: function () {
      this.$http.post("mapToDomainIds", {
        type: ['gene', 'protein'][this.seedTypeId],
        ids: this.seeds.map(s => s.id)
      }).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        let text = "";
        Object.values(data).forEach(id => text += id + "\n")
        this.download(["gene", "protein"][this.seedTypeId] + "_seeds.tsv", text)
      }).catch(console.log)
    }
    ,
    downloadModuleResultList: function () {
      this.$http.post("mapToDomainIds", {
        type: ['gene', 'protein'][this.seedTypeId],
        ids: this.results.targets.map(l => l.id)
      }).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        let text = "id";
        this.moduleMethods[this.moduleMethodModel].scores.forEach(s => text += "\t" + s.name)
        text += "\n"
        this.results.targets.forEach(t => {
            text += data[t.id]
            this.moduleMethods[this.moduleMethodModel].scores.forEach(s => text += "\t" + t[s.id])
            text += "\n"
          }
        )
        this.download(["gene", "protein"][this.seedTypeId] + "_module.tsv", text)
      }).catch(console.log)
    },
    downloadRankingResultList: function () {
      this.$http.post("mapToDomainIds", {
        type: 'drug',
        ids: this.results.drugs.map(l => l.id)
      }).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        let text = "id";
        this.rankingMethods[this.rankingMethodModel].scores.forEach(s => text += "\t" + s.name)
        text += "\n"
        this.results.drugs.forEach(t => {
            text += data[t.id]
            this.rankingMethods[this.rankingMethodModel].scores.forEach(s => text += "\t" + t[s.id])
            text += "\n"
          }
        )
        this.download("drug_ranking-Top_" + this.rankingModels.topX + ".tsv", text)
      }).catch(console.log)
    },

    downloadFullResultList: function (jid) {
      window.open(CONFIG.HOST_URL + CONFIG.CONTEXT_PATH + '/api/downloadJobResult?jid=' + jid)
    }
    ,
    download: function (name, content) {
      let dl = document.createElement('a')
      dl.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(content))
      dl.setAttribute('download', name)
      dl.style.direction = 'none'
      document.body.appendChild(dl)
      dl.click()
      document.body.removeChild(dl)
    }
    ,
    loadModuleTargetTable: function () {
      let seedType = ['gene', 'protein'][this.seedTypeId]
      let scoreAttr = this.moduleMethods[this.moduleMethodModel].scores.filter(s => s.decimal)
      this.targetColorStyle = {'background-color': this.metagraph.colorMap[seedType].light}
      return this.$http.get("/getGraphList?id=" + this.moduleGid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => this.$utils.roundScores(data, seedType, scoreAttr)).then(data => {
        if (this.moduleMethodModel === 0)
          this.results.targets = data.nodes[seedType].sort((e1, e2) => {
            if (e1.rank && e2.rank)
              return e1.rank - e2.rank
            if (e1.rank)
              return -1
            return 1
          })
        else
          this.results.targets = data.nodes[seedType]
      }).catch(console.log)
    },
    loadRankingTargetTable: function () {
      let scoreAttr = this.rankingMethods[this.rankingMethodModel].scores.filter(s => s.decimal)
      this.drugColorStyle = {'background-color': this.metagraph.colorMap['drug'].light}
      return this.$http.get("/getGraphList?id=" + this.rankingGid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => this.$utils.roundScores(data, 'drug', scoreAttr)).then(data => {
        this.results.drugs = data.nodes.drug.sort((e1, e2) => e2.score - e1.score)
      }).catch(console.log)
    },
    waitForGraph: function (resolve) {
      if (this.$refs.graph === undefined)
        setTimeout(this.waitForGraph, 100, resolve)
      else
        resolve()
    },
    getGraph: function () {
      return new Promise(resolve => this.waitForGraph(resolve)).then(() => {
        return this.$refs.graph;
      })
    },
    loadGraph: function (graphId) {
      this.getGraph().then(graph => {
        this.resultProgress += 5
        graph.setLoading(true)
        graph.show(graphId).then(() => {
          this.resultProgress += 15
          graph.showLoops(false)
          let seedIds = this.seeds.map(s => s.id)
          this.resultProgress += 3
          graph.modifyGroups(this.results.targets.filter(n => seedIds.indexOf(n.id) === -1).map(n => ["gen_", "pro_"][this.seedTypeId] + n.id), ["geneModule", "proteinModule"][this.seedTypeId])
          graph.setLoading(false)
          this.resultProgress += 2
        })
      })
    },
    getHeaders: function (table) {
      let headers = [{text: "Name", align: "start", sortable: true, value: "displayName"}]
      let scores = []
      if (table === 0)
        scores = this.moduleMethodScores()
      else
        scores = this.rankingMethodScores();
      scores.forEach(e => headers.push({
        text: e.name,
        align: e.decimal ? "start" : "end",
        sortable: true,
        value: e.id,
      }))

      return headers
    },
    seedClicked: function (item) {
      this.focusNode(['gen_', 'pro_'][this.seedTypeId] + item.id)
    },
    drugClicked: function (item) {
      this.focusNode(['dru_'] + item.id)
    },
    focus: function () {
      this.$emit("focusEvent")
    },
    getColoring: function (entity, name, style) {
      return this.$utils.getColoring(this.metagraph, entity, name, style);
    },

  },

  components: {
    Graph,
    SuggestionAutocomplete
  }
}
</script>

<style lang="sass">

.td-name
  max-width: 4vw

.td-score
  max-width: 4vw

.td-rank
  max-width: 3vw !important

.td-result
  font-size: smaller !important

</style>
