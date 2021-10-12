<template>
  <v-card style="margin-bottom: 25px">
    <div style="display: flex; justify-content: flex-end; margin-left: auto; ">
      <v-tooltip left>
        <template v-slot:activator="{on, attrs}">
          <v-btn icon style="padding:1em" color="red darker" @click="makeStep(0,'cancel')" v-on="on" v-bind="attrs">
            <v-icon size="2em">far fa-times-circle</v-icon>
          </v-btn>
        </template>
        <div>Close <b>Drug Repurposing</b> and return to the <b>Quick Start</b> menu</div>
      </v-tooltip>
    </div>
    <div style="display: flex; color: dimgray; padding-bottom: 8px">
      <v-card-title style="font-size: 2.5em; justify-content: center; margin-left: auto; margin-right: auto">
        Drug Repurposing
      </v-card-title>
    </div>
    <v-stepper
      alt-labels
      v-model="step"
    >
      <v-stepper-header ref="head">
        <v-stepper-step step="1" :complete="step>1">
          Select Seeds
          <small v-if="seedTypeId!==undefined">{{ ["Gene", "Protein"][seedTypeId] }}
            ({{ $refs.seedTable ? $refs.seedTable.getSeeds().length : 0 }})</small>
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
            v-show="step===1"
            class="mb-4"
            max-height="85vh"
          >

            <v-card-subtitle class="headline">1. Seed Configuration</v-card-subtitle>
            <v-card-subtitle style="margin-top: -25px">Add seeds to your
              list
              <span v-if="!blitz">{{ blitz ? "." : " or use an expression data based algorithm (" }}<a
                @click="seedTypeId=0; moduleMethodModel=1; makeStep(1,'continue')">BiCoN
                <v-icon right size="1em" style="margin-left: 0">fas fa-caret-right</v-icon>
              </a>{{ ")." }}
              </span>
              <span v-else> In Quick Drug Repurposing a
                <v-tooltip bottom>
                <template v-slot:activator="{on, attrs}">
                  <span v-on="on" v-bind="attrs">
                    <a>default configuration <v-icon color="primary" size="1em">far fa-question-circle</v-icon></a>
                  </span>
                </template>
                <span>
                  <v-container>
                    <v-row>
                      <v-col>
                        <i>
                         Default MI Algorithm:
                        </i>
                      </v-col>
                      <v-col>
                        <b>{{ moduleMethods[moduleMethodModel].label }}</b>
                      </v-col>
                    </v-row>
                    <v-row>
                      <v-col>
                        <i>Default MI parameters:</i>
                      </v-col>
                          <v-col style="text-align: start">
                          <b>n = 200</b> (number of additions)
                          <br>
                          <b>alpha = 1</b> (seed weight)
                          <br>
                          <b>p-cutoff = 1</b> (max allowed p-value)
                          <br>
                          Only uses <b>experimentally validated interactions</b>
                        </v-col>
                      </v-row>
                    <v-row>
                      <v-col>
                        <i>
                         Default DP Algorithm:
                        </i>
                      </v-col>
                      <v-col>
                        <b>{{ rankingMethods[rankingMethodModel].label }}</b>
                      </v-col>
                    </v-row>
                    <v-row>
                      <v-col>
                        <i>Default DP parameters:</i>
                      </v-col>
                          <v-col style="text-align: start">
                          <b>topX = 100</b> (return best X results)
                          <br>
                          Only uses <b>experimentally validated interactions</b>
                          <br>
                          Only uses <b>directly connected drugs</b>
                          <br>
                            Only uses <b>approved drugs</b>
                            <br>
                            Filters atoms (Gold, Zinc...) and basic chemical compounds (Cupric Chloride, Sodium chloride)
                        </v-col>
                      </v-row>
                    </v-container>
                </span>
              </v-tooltip> is used.</span>
            </v-card-subtitle>
            <v-divider style="margin: 15px;"></v-divider>
            <v-row>
              <v-col>
                <v-list-item-subtitle v-if="!validationDrugView" class="title">Select the seed type
                </v-list-item-subtitle>
                <v-list-item-subtitle v-else class="title">Select Validation Drugs</v-list-item-subtitle>
                <v-list-item-action>
                  <v-radio-group row v-model="seedTypeId"
                                 :disabled="validationDrugView || (this.seedTypeId !=null && $refs.seedTable !=null && $refs.seedTable.getSeeds()!=null && $refs.seedTable.getSeeds().length>0)">
                    <v-radio label="Gene">
                    </v-radio>
                    <v-radio label="Protein">
                    </v-radio>
                  </v-radio-group>
                </v-list-item-action>
              </v-col>
            </v-row>
            <ExampleSeeds :seedTypeId="seedTypeId" @addSeedsEvent="addToSelection" :disabled="validationDrugView"></ExampleSeeds>
            <v-container style="height: 55vh;margin: 15px;">
              <v-row style="height: 100%">
                <v-col cols="6">
                  <div style="height: 40vh; max-height: 40vh;">
                    <template v-if="seedTypeId!==undefined">
                      <div style="display: flex">
                        <div style="justify-content: flex-start">
                          <v-card-title style="margin-left: -25px;" class="subtitle-1">Add
                            {{validationDrugView ? 'drugs': ['genes', 'proteins'][this.seedTypeId] }} associated to
                          </v-card-title>
                        </div>
                        <v-tooltip top>
                          <template v-slot:activator="{on,attrs}">
                            <div v-on="on" v-bind="attrs" style="justify-content: flex-end; margin-left: auto">
                              <v-switch :label="advancedOptions ? 'Full' :'Limited'"
                                        v-model="advancedOptions"
                                        @click="suggestionType = advancedOptions ? suggestionType : 'disorder'"></v-switch>
                            </div>
                          </template>
                          <div style="width: 300px"><b>Limited Mode:</b><br>The options are limited to the most
                            interesting and generally used ones to not overcomplicate the user interface <br>
                            <b>Full Mode:</b><br> The full mode provides a wider list of options to select from for more
                            specific queries.
                          </div>
                        </v-tooltip>
                      </div>
                      <div style="display: flex">

                        <v-tooltip top>
                          <template v-slot:activator="{on, attrs}">
                            <div v-on="on" v-bind="attrs" style="width: 35%;justify-self: flex-start">
                              <v-select :items="getSuggestionSelection()" v-model="suggestionType"
                                        placeholder="connected to" style="width: 100%"
                                        :disabled="!advancedOptions"></v-select>
                            </div>
                          </template>
                          <div v-if="advancedOptions" style="width: 300px"><b>Full Mode:</b><br>A node type with
                            direct association to {{
                              ['gene', 'protein'][this.seedTypeId]
                            }} nodes can freely be selected and are to add additional seeds.
                          </div>
                          <div v-else style="width: 300px"><b>Limited Mode:</b><br>Disorders can be used to add known {{
                              ['gene', 'protein'][this.seedTypeId]
                            }} associations as seed nodes. For the use of all available node types for the selection
                            through association the 'Limited' switch has to be toggled.
                          </div>
                        </v-tooltip>
                        <SuggestionAutocomplete :suggestion-type="suggestionType" :emit-drugs="!validationDrugView"
                                                @drugsEvent="$refs.validationTable.addDrugs"
                                                :target-node-type="validationDrugView ? 'drug' : ['gene', 'protein'][seedTypeId]"
                                                @addToSelectionEvent="addToSelection"
                                                style="justify-self: flex-end;margin-left: auto"></SuggestionAutocomplete>
                      </div>
                      <NodeInput text="or provide Seed IDs by" @addToSelectionEvent="addToSelection"
                                 :idName="validationDrugView? 'drugbank':['entrez','uniprot'][seedTypeId]"
                                 :nodeType="validationDrugView? 'drug':['gene', 'protein'][this.seedTypeId]"
                                 @printNotificationEvent="printNotification"></NodeInput>
                    </template>
                  </div>
                </v-col>

                <v-divider vertical v-show="seedTypeId!==undefined"></v-divider>
                <v-col cols="6">
                  <v-tooltip left>
                    <template v-slot:activator="{attrs,on}">
                      <v-chip style="position: absolute; left:auto; right:0" v-on="on" v-bind="attrs"
                              v-show="seedTypeId!=null"
                              @click="toggleValidationDrugView()" :color="validationDrugView ? 'green':'primary'">
                        <v-icon left>fas fa-capsules</v-icon>
                        {{ validationDrugCount }}
                      </v-chip>
                    </template>
                    <span>There are {{ validationDrugCount }} drugs that were associated with your query.<br> These are saved for validation purposes later.<br><br><i>To see or even adjust the list, toggle this button!</i></span>
                  </v-tooltip>
                  <SeedTable ref="seedTable" v-show="seedTypeId!=null && !validationDrugView" :download="true"
                             :remove="true"
                             :filter="true"
                             @printNotificationEvent="printNotification"
                             height="40vh"
                             :title="'Selected Seeds ('+($refs.seedTable ? $refs.seedTable.getSeeds().length : 0)+')'"
                             :nodeName="['gene','protein'][seedTypeId]"
                  ></SeedTable>
                  <ValidationDrugTable v-show="seedTypeId!=null && validationDrugView" ref="validationTable"
                                       @printNotificationEvent="printNotification"
                                       @drugCountUpdate="updateDrugCount()"></ValidationDrugTable>
                </v-col>
              </v-row>
            </v-container>


          </v-card>
          <v-btn
            color="primary"
            @click="makeStep(1,'continue')"
            :disabled="seedTypeId<0 || $refs.seedTable == null || $refs.seedTable.getSeeds().length===0"
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
            class="mb-4"
            height="700px"
          >
            <v-card-subtitle class="headline">2. Module Identification Algorithm Selection</v-card-subtitle>
            <v-card-subtitle style="margin-top: -25px">Select and adjust the algorithm you want to apply on your seeds
              to
              construct a disease module.
            </v-card-subtitle>
            <v-divider style="margin: 15px;"></v-divider>
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
                    <div style="display: flex; justify-content: flex-start">
                      <v-switch
                        label="Only use experimentally validated interaction networks"
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
                    </div>
                    <div>

                      <template v-if="moduleMethods[moduleMethodModel].id==='bicon'">
                        <div style="justify-self: flex-end">
                          <v-file-input
                            v-on:change="biconFile"
                            show-size
                            prepend-icon="fas fa-file-medical"
                            label="Expression File"
                            dense
                          >
                          </v-file-input>
                        </div>

                        <div>
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
                        </div>
                      </template>
                      <template v-if="moduleMethods[moduleMethodModel].id==='diamond'">
                        <div>
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
                        </div>
                        <div>
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
                        </div>
                        <div>
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
                        </div>
                      </template>
                      <template v-if="moduleMethods[moduleMethodModel].id==='robust'">
                        <div>
                          <v-slider
                            hide-details
                            class="align-center"
                            v-model="moduleModels.robust.initFract"
                            min="0"
                            step="0.01"
                            max="1"
                          >
                            <template v-slot:prepend>
                              <v-text-field
                                v-model="moduleModels.robust.initFract"
                                class="mt-0 pt-0"
                                type="number"
                                style="width: 80px"
                                label="Initial Fraction"
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
                                <span>initial fraction</span>
                              </v-tooltip>
                            </template>
                          </v-slider>
                        </div>
                        <div>
                          <v-slider
                            hide-details
                            class="align-center"
                            v-model="moduleModels.robust.reductionFactor"
                            min="0"
                            step="0.01"
                            max="1"
                          >
                            <template v-slot:prepend>
                              <v-text-field
                                v-model="moduleModels.robust.reductionFactor"
                                class="mt-0 pt-0"
                                type="number"
                                style="width: 80px"
                                label="Reduction Factor"
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
                                <span>reduction factor</span>
                              </v-tooltip>
                            </template>
                          </v-slider>
                        </div>
                        <div>
                          <v-slider
                            hide-details
                            class="align-center"
                            v-model="moduleModels.robust.threshold"
                            min="-100"
                            max="0"
                          >
                            <template v-slot:prepend>
                              <v-text-field
                                prefix="10^"
                                v-model="moduleModels.robust.threshold"
                                class="mt-0 pt-0"
                                type="number"
                                style="width: 80px"
                                label="threshold"
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
                                <span>Threshold</span>
                              </v-tooltip>
                            </template>
                          </v-slider>
                        </div>
                        <div>
                          <v-slider
                            hide-details
                            class="align-center"
                            v-model="moduleModels.robust.trees"
                            min="2"
                            max="100"
                          >
                            <template v-slot:prepend>
                              <v-text-field
                                v-model="moduleModels.robust.trees"
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
                                <span>Number of steiner trees to be computed (Integer).</span>
                              </v-tooltip>
                            </template>
                          </v-slider>
                        </div>
                      </template>
                      <template v-if="moduleMethods[moduleMethodModel].id==='must'">
                        <div>
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
                        </div>
                        <div>
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
                        </div>
                        <div v-show="moduleModels.must.multiple">
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
                        </div>
                        <div>
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
                        </div>
                      </template>
                    </div>
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
            class="mb-4"
            height="700px"
          >
            <v-card-subtitle class="headline">3. Drug Ranking Algorithm Selection</v-card-subtitle>
            <v-card-subtitle style="margin-top: -25px">Select and adjust the algorithm you want to apply on the
              constructed module to
              identify and rank drug candidates.
            </v-card-subtitle>
            <v-divider style="margin: 15px;"></v-divider>
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
                    <div>
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
                    </div>
                    <div style="display: flex">
                      <div>
                        <v-switch
                          label="Only use experimentally validated interaction networks"
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
                      </div>
                    </div>
                    <div style="display:flex; width: 100%">
                      <div style="justify-self: flex-start">
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
                      <div style="justify-self: center; margin-left: auto">
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
                      </div>
                      <div style="justify-self: flex-end; margin-left: auto">
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
                              <span>Filters:<br><b>chemical elements:</b> <i>Gold</i>, <i>Zinc</i>, ...<br><b>metals and metal cations:</b> <i>Cupric Chloride</i>, <i>Aluminium acetoacetate</i>, ...<br><b>minerals and mineral supplements:</b> <i>Calcium silicate</i>, <i>Sodium chloride</i>, ...</span>
                            </v-tooltip>
                          </template>
                        </v-switch>
                      </div>
                    </div>
                    <div>
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
                    </div>
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
            class="mb-4"
            height="80vh"
          >
            <v-card-subtitle class="headline">4. Drug Repurposing Results</v-card-subtitle>
            <v-divider style="margin: 15px;"></v-divider>
            <v-container>
              <v-row>
                <v-col cols="3" style="padding: 0 50px 0 0; margin-right: -50px">
                  <v-card-title class="subtitle-1">Seeds ({{ seeds.length }}) {{
                      (results.targets.length !== undefined && results.targets.length > 0 ? ("& Module (" + getTargetCount() + ") " + ["Genes", "Proteins"][seedTypeId]) : ": Processing")
                    }}
                    <v-progress-circular indeterminate size="25" v-if="this.results.targets.length===0"
                                         style="margin-left:15px; z-index:50">
                    </v-progress-circular>
                  </v-card-title>
                  <ValidationBox ref="moduleValidation"></ValidationBox>
                  <v-data-table max-height="50vh" height="50vh" class="overflow-y-auto" fixed-header dense item-key="id"
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
                        <span>{{ item.displayName }}</span>
                      </v-tooltip>
                      <span v-else>{{ item.displayName }}</span>
                    </template>
                    <template v-slot:footer>
                      <div style="display: flex; justify-content: center; margin-left: auto">
                        <div style="padding-top: 16px">
                          <ResultDownload v-show="seeds !=null && seeds.length>0 && results.targets !=null" raw results
                                          seeds
                                          @downloadEvent="downloadSeedList"
                                          @downloadResultsEvent="downloadModuleResultList"
                                          @downloadRawEvent="downloadFullResultList(moduleJid)"></ResultDownload>
                        </div>
                      </div>
                    </template>
                  </v-data-table>
                </v-col>
                <v-col>
                  <Network ref="graph" :configuration="graphConfig" :window-style="graphWindowStyle"
                           :progress="resultProgress" :legend="resultProgress===100" :tools="resultProgress===100"
                           :secondaryViewer="true"
                           @loadIntoAdvancedEvent="$emit('graphLoadEvent',{post: {id: rankingGid}})">
                    <template v-slot:legend v-if="results.drugs.length>0">
                      <v-card style="width: 15vw; max-width: 20vw; padding-top: 35px">
                        <v-list dense>
                          <v-list-item>
                            <v-list-item-icon>
                              <div
                                style="display: flex; align-content: center; justify-content: center;margin-left: 12px;margin-top:2px">
                                <v-icon color="#fbe223" style="position: absolute;margin-top:-11px;"
                                        size="42">fas fa-genderless
                                </v-icon>
                                <v-icon size="18" style="position: absolute;margin-top:1px;"
                                        :color="getColoring('nodes',['gene','protein'][seedTypeId],'light')">fas
                                  fa-circle
                                </v-icon>
                              </div>
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
                              <v-icon left :color="getColoring('nodes',['gene','protein'][seedTypeId],'light')"
                                      size="43px">fas fa-genderless
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
                    <template v-slot:tools v-if="results.targets.length>0">
                      <v-card elevation="3" style="width: 15vw; max-width: 17vw; padding-top: 35px">
                        <v-container>
                          <v-list ref="list" style="margin-top: 10px;">
                            <v-tooltip left>
                              <template v-slot:activator="{on, attrs}">
                                <v-list-item v-on="on" v-bind="attrs">
                                  <v-list-item-action>
                                    <v-chip outlined v-on:click="$refs.graph.setSelection()">
                                      <v-icon left>fas fa-globe</v-icon>
                                      Overview
                                    </v-chip>
                                  </v-list-item-action>
                                </v-list-item>
                              </template>
                              <span>Fits the view to the whole network.</span>
                            </v-tooltip>
                            <v-tooltip left>
                              <template v-slot:activator="{on, attrs}">
                                <v-list-item v-on="on" v-bind="attrs">
                                  <v-list-item-action-text>Enable node interactions</v-list-item-action-text>
                                  <v-list-item-action>
                                    <v-switch v-model="physicsOn"
                                              @click="$refs.graph.setPhysics(physicsOn)"></v-switch>
                                  </v-list-item-action>
                                </v-list-item>
                              </template>
                              <span>This option enables a physics based layouting where nodes and <br>edges interact with each other. Be careful on large graphs.</span>
                            </v-tooltip>
                            <v-tooltip left>
                              <template v-slot:activator="{on, attrs}">
                                <v-list-item v-on="on" v-bind="attrs">
                                  <v-list-item-action>
                                    <v-chip outlined v-if="rankingJid!=null && rankingGid !=null"
                                            style="margin-top:15px"
                                            @click="$emit('graphLoadNewTabEvent',{post: {id: rankingGid}})">
                                      <v-icon left>fas fa-angle-double-right</v-icon>
                                      To Advanced View
                                    </v-chip>
                                  </v-list-item-action>
                                </v-list-item>
                              </template>
                              <span>Opens a new tab with an advanced view of the current network.</span>
                            </v-tooltip>
                          </v-list>
                        </v-container>
                      </v-card>
                    </template>
                  </Network>
                </v-col>
                <v-col cols="3" style="padding:0">
                  <v-card-title class="subtitle-1"> Drugs{{
                      (results.drugs.length !== undefined && results.drugs.length > 0 ? (" (" + (results.drugs.length) + ")") : ": Processing")
                    }}
                    <v-progress-circular indeterminate size="25" v-if="results.drugs.length===0"
                                         style="margin-left:15px; z-index:50">
                    </v-progress-circular>
                  </v-card-title>
                  <ValidationBox ref="drugValidation" drugs></ValidationBox>
                  <template v-if="results.drugs.length>0">
                    <v-data-table max-height="50vh" height="50vh" class="overflow-y-auto" fixed-header dense
                                  item-key="id"
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
                          <span>{{ item.displayName }}</span>
                        </v-tooltip>
                        <span v-else>{{ item.displayName }}</span>
                      </template>
                      <template v-slot:footer>
                        <div style="display: flex; justify-content: center">
                          <div style="padding-top: 16px">
                            <ResultDownload v-show="results.drugs.length>0" raw results
                                            @downloadResultsEvent="downloadRankingResultList"
                                            @downloadRawEvent="downloadFullResultList(rankingJid)"></ResultDownload>
                          </div>
                        </div>
                      </template>
                    </v-data-table>
                  </template>
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
  </v-card>
</template>

<script>
import Network from "../../graph/Network";
import * as CONFIG from "../../../../Config"
import SuggestionAutocomplete from "@/components/app/suggestions/SuggestionAutocomplete";
import SeedTable from "@/components/app/tables/SeedTable";
import ResultDownload from "@/components/app/tables/menus/ResultDownload";
import NodeInput from "@/components/app/input/NodeInput";
import ExampleSeeds from "@/components/start/quick/ExampleSeeds";
import ValidationBox from "@/components/start/quick/ValidationBox";
import ValidationDrugTable from "@/components/app/tables/ValidationDrugTable";

export default {
  name: "CombinedRepurposing",
  props: {
    blitz: Boolean,
  },

  sugQuery: undefined,
  validationDrugs: {},
  validationScore: undefined,

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
      advancedOptions: false,
      physicsOn: false,
      moduleMethods: [{
        id: "diamond",
        label: "DIAMOnD",
        scores: [{id: "rank", name: "Rank"}, {id: "p_hyper", name: "P-Value", decimal: true}]
      }, {id: "bicon", label: "BiCoN", scores: []}, {id: "must", label: "MuST", scores: []},
        {id: "domino", label: "DOMINO", scores: []}, {
          id: "robust",
          label: "ROBUST",
          scores: [{id: "occs_abs", name: "Occs (Abs)"}, {id: "occs_rel", name: "Occs (%)", decimal: true}]
        }],
      rankingMethods: [
        {
          id: "trustrank",
          label: "TrustRank",
          scores: [{id: "score", name: "Score", decimal: true}, {id: "rank", name: "Rank"}]
        },
        {
          id: "centrality",
          label: "Closeness Centrality",
          scores: [{id: "score", name: "Score", decimal: true}, {id: "rank", name: "Rank"}]
        }],
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
        }, domino: {}, robust: {
          initFract: 0.25,
          reductionFactor: 0.9,
          trees: 30,
          threshold: -1
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
      validationDrugCount: 0,
      validationDrugView: false,
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
      this.validationDrugCount = 0
    },
    reset: function () {
      this.init()
    },
    getSuggestionSelection: function () {
      let type = ["gene", "protein"][this.seedTypeId]
      let nodeId = this.$global.metagraph.nodes.filter(n => n.group === type)[0].id
      let disorderIdx = -1
      let out = this.$global.metagraph.edges.filter(e => e.from !== e.to && e.from === nodeId || e.to === nodeId).map(e => e.to === nodeId ? e.from : e.to).map(nid => {
        let node = this.$global.metagraph.nodes.filter(n => n.id === nid)[0]
        if (node.label === "Disorder") {
          disorderIdx = -(disorderIdx + 1)
        } else {
          if (disorderIdx < 0)
            disorderIdx--;
        }
        return {value: node.group, text: node.label}
      })
      if (!this.advancedOptions) {
        this.suggestionType = out[disorderIdx].value;
      }
      return out
    },
    makeStep: function (s, button) {
      if (button === "continue") {

        this.step++

        if (this.step === 3)
          this.submitModuleAlgorithm()

        if (this.step === 2) {
          this.seeds = this.$refs.seedTable.getSeeds()
          if (this.blitz) {
            this.submitModuleAlgorithm()
            this.step += 2
          }
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
        this.$socket.unsubscribeJob(this.moduleJid)
        this.$socket.unsubscribeJob(this.rankingJid)
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
      if (method === 'robust') {
        params["trees"] = this.moduleModels.robust.trees;
        params["initFract"] = this.moduleModels.robust.initFract;
        params["threshold"] = Math.pow(10, this.moduleModels.robust.threshold);
        params["reductionFactor"] = this.moduleModels.robust.reductionFactor;
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
        params["nodesOnly"] = false

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
      let payload = {
        userId: this.uid,
        dbVersion: this.$global.metadata.repotrial.version,
        algorithm: algorithm,
        params: params
      }
      payload.selection = true
      payload.experimentalOnly = params.experimentalOnly
      payload["nodes"] = this.seeds.map(n => n.id)
      this.$http.post("/submitJob", payload).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        if (data.state === 'DONE')
          this.readModuleJob(data, true, true)
        else {
          this.$socket.subscribeJob(data.jid, "quickModuleFinishedEvent");
          this.readModuleJob(data, true)
        }
      }).catch(console.error)
    }
    ,
    executeRankingJob: function (algorithm, params) {
      let payload = {
        userId: this.uid,
        dbVersion: this.$global.metadata.repotrial.version,
        graphId: this.moduleGid,
        algorithm: algorithm,
        params: params
      }
      payload.selection = false
      payload.experimentalOnly = params.experimentalOnly
      this.$http.post("/submitJob", payload).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        if (data.state === 'DONE') {
          this.readRankingJob(data, true, true)
        } else {
          this.$socket.subscribeJob(data.jid, "quickRankingFinishedEvent");
          this.readRankingJob(data, true)
        }
      }).catch(console.error)
    },
    readModuleJob: function (result, clean, unsubscribed) {
      this.resultProgress += 5
      let data = clean ? result : JSON.parse(result)
      this.moduleJid = data.jid
      this.moduleGid = data.gid
      if (this.moduleGid != null && data.state === "DONE") {
        if (!unsubscribed)
          this.$socket.unsubscribeJob(this.moduleJid)
        this.loadModuleTargetTable().then(() => {
          this.resultProgress = 25
          this.runModuleValidation()
          this.loadGraph(this.moduleGid, true)
        })
      }
    },
    runModuleValidation: function () {
      if (this.$refs.moduleValidation != null)
        this.$refs.moduleValidation.validate(this.results.targets, this.$refs.validationTable.getDrugs(), false, ["gene", "protein"][this.seedTypeId])
      else
        setTimeout(() => {
          this.runModuleValidation()
        }, 1000)
    }
    ,
    updateDrugCount: function(){
      this.validationDrugCount = this.$refs.validationTable.getDrugs().length;
    },
    readRankingJob: function (result, clean, unsubscribed) {
      this.resultProgress += 5
      let data = clean ? result : JSON.parse(result)
      this.rankingJid = data.jid
      this.rankingGid = data.gid
      if (this.rankingGid != null && data.state === "DONE") {
        this.resultProgress = 75
        if (!unsubscribed)
          this.$socket.unsubscribeJob(this.rankingJid)
        this.loadRankingTargetTable(this.rankingGid).then(() => {
          this.$refs.drugValidation.validate(this.results.drugs, this.$refs.validationTable.getDrugs(), this.rankingModels.onlyApproved)
          this.loadGraph(this.rankingGid)
        })

      }
    },

    addToSelection: function (data) {
      if (this.validationDrugView)
        this.$refs.validationTable.addDrugs(data)
      else
        this.$refs.seedTable.addSeeds(data)
    },
    toggleValidationDrugView: function () {
      this.$set(this, "validationDrugView", !this.validationDrugView)
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

    downloadSeedList: function (names, sep) {
      this.$http.post("mapToDomainIds", {
        type: ['gene', 'protein'][this.seedTypeId],
        ids: this.seeds.map(s => s.id)
      }).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        let text = "#ID" + (names ? sep + "Name" : "") + "\n";
        let dlName = ["gene", "protein"][this.seedTypeId] + "_seeds." + (!names ? "list" : (sep === '\t' ? "tsv" : "csv"))
        if (!names) {
          Object.values(data).forEach(id => text += id + "\n")
        } else {
          this.seeds.forEach(s => text += data[s.id] + sep + s.displayName + "\n")
        }
        this.download(dlName, text)
      }).catch(console.error)
    }
    ,
    downloadModuleResultList: function (s, names) {
      this.$http.post("mapToDomainIds", {
        type: ['gene', 'protein'][this.seedTypeId],
        ids: this.results.targets.map(l => l.id)
      }).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        let sep = s ? s : "\t"
        let text = "#ID" + (names ? sep + "Name" : "");

        this.moduleMethods[this.moduleMethodModel].scores.forEach(s => text += sep + s.name)
        text += "\n"
        this.results.targets.forEach(t => {
            text += data[t.id] + (names ? sep + t.displayName : "")
            this.moduleMethods[this.moduleMethodModel].scores.forEach(s => text += sep + t[s.id])
            text += "\n"
          }
        )
        this.download(["gene", "protein"][this.seedTypeId] + "_module." + (sep === "\t" ? "tsv" : "csv"), text)
      }).catch(console.error)
    },
    downloadRankingResultList: function (sep) {
      this.$http.post("mapToDomainIds", {
        type: 'drug',
        ids: this.results.drugs.map(l => l.id)
      }).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        let text = "#ID" + sep + "Name";
        this.rankingMethods[this.rankingMethodModel].scores.forEach(s => text += sep + s.name)
        text += "\n"
        this.results.drugs.forEach(t => {
            text += data[t.id] + sep + t.displayName
            this.rankingMethods[this.rankingMethodModel].scores.forEach(s => text += sep + t[s.id])
            text += "\n"
          }
        )
        this.download("drug_ranking-Top_" + this.rankingModels.topX + (sep === "\t" ? ".tsv" : ".csv"), text)
      }).catch(console.error)
    },

    saveDrugsForValidation: function (drugs) {
      drugs.forEach(drug => this.validationDrugs[drug.id] = drug);
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
      this.targetColorStyle = {'background-color': this.$global.metagraph.colorMap[seedType].light}
      return this.$http.get("/getGraphList?id=" + this.moduleGid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => this.$utils.roundScores(data, seedType, scoreAttr)).then(data => {
        data.nodes[seedType].forEach(n => n.displayName = this.$utils.adjustLabels(n.displayName))
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

        // let connectedIds = []
        // data.edges[["GeneGeneInteraction", "ProteinProteinInteraction"][this.seedTypeId]].forEach(edge => {
        //   let spl = edge.id.split("-")
        //   let id1 = parseInt(spl[0])
        //   let id2 = parseInt(spl[1])
        //   if (id1 !== id2) {
        //     if (connectedIds.indexOf(id1) === -1)
        //       connectedIds.push(id1)
        //     if (connectedIds.indexOf(id2))
        //       connectedIds.push(id2)
        //   }
        // })
        // return this.results.targets.filter(node => connectedIds.indexOf(node.id) > -1);
      }).catch(console.error)
    },
    loadRankingTargetTable: function () {
      let scoreAttr = this.rankingMethods[this.rankingMethodModel].scores.filter(s => s.decimal)
      this.drugColorStyle = {'background-color': this.$global.metagraph.colorMap['drug'].light}
      return this.$http.get("/getGraphList?id=" + this.rankingGid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => this.$utils.roundScores(data, 'drug', scoreAttr)).then(data => {
        this.results.drugs = data.nodes.drug.sort((e1, e2) => e2.score - e1.score)

        let lastRank = 0;
        let lastScore = 0;
        let step = 0;

        this.results.drugs.forEach(drug => {
          step++
          if (lastRank === 0 || lastScore !== drug.score) {
            lastRank = step;
            lastScore = drug.score;
          }
          drug.rank = lastRank
        })
      }).catch(console.error)
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
    loadGraph: function (graphId, disableSkipToAdvanced) {
      this.getGraph().then(graph => {
        this.resultProgress += 5
        graph.loadNetworkById(graphId, disableSkipToAdvanced).then(() => {
          this.resultProgress += 15
          graph.showLoops(false)
          let seedIds = this.seeds.map(s => s.id)
          this.resultProgress += 3
          graph.modifyGroups(this.results.targets.filter(n => seedIds.indexOf(n.id) > -1).map(n => ["gen_", "pro_"][this.seedTypeId] + n.id), ["seedGene", "seedProtein"][this.seedTypeId])
          this.resultProgress += 2
        })
      })
    },
    printNotification: function (message, type) {
      this.$emit("printNotificationEvent", message, type)
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
      return this.$utils.getColoring(this.$global.metagraph, entity, name, style);
    },

  },

  components: {
    Network,
    SuggestionAutocomplete,
    ValidationDrugTable,
    NodeInput,
    SeedTable,
    ResultDownload,
    ExampleSeeds,
    ValidationBox,
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
