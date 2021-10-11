<template>
  <v-card style="margin-bottom: 25px">
    <div style="display: flex; justify-content: flex-end; margin-left: auto; ">
      <v-tooltip left>
        <template v-slot:activator="{on, attrs}">
          <v-btn icon style="padding:1em" color="red darker" @click="makeStep(0,'cancel')" v-on="on" v-bind="attrs">
            <v-icon size="2em">far fa-times-circle</v-icon>
          </v-btn>
        </template>
        <div>Close <b>Module Identification</b> and return to the <b>Quick Start</b> menu</div>
      </v-tooltip>
    </div>
    <div style="display: flex; color: dimgray; padding-bottom: 8px">
      <v-card-title style="font-size: 2.5em; justify-content: center; margin-left: auto; margin-right: auto">
        Module
        Identification
      </v-card-title>
    </div>
    <v-stepper
      alt-labels
      v-model="step"
      flat
    >
      <v-stepper-header ref="head">
        <v-stepper-step step="1" :complete="step>1">
          Select Seeds
          <small v-if="seedTypeId!==undefined">{{ ["Gene", "Protein"][seedTypeId] }}
            ({{ $refs.seedTable ? $refs.seedTable.getSeeds().length : 0 }})</small>
        </v-stepper-step>
        <v-divider></v-divider>
        <v-stepper-step step="2" :complete="step>2 || blitz">
          Select Method
          <small v-if=" methodModel>-1">{{ methods[methodModel].label }}</small>
        </v-stepper-step>
        <v-divider></v-divider>
        <v-stepper-step step="3">
          Results
        </v-stepper-step>
      </v-stepper-header>
      <v-stepper-items>
        <v-stepper-content step="1">
          <v-card
            v-show="step===1"
            class="mb-4"
            min-height="80vh"
          >
            <v-card-subtitle class="headline">1. Seed Configuration</v-card-subtitle>
            <v-card-subtitle style="margin-top: -25px">Add seeds to your
              list
              <span v-if="!blitz">{{ blitz ? "." : " or use an expression data based algorithm (" }}<a
                @click="seedTypeId=0; methodModel=1; makeStep(1,'continue')">BiCoN
                <v-icon right size="1em" style="margin-left: 0">fas fa-caret-right</v-icon>
              </a>{{ ")." }}
              </span>
              <span v-else> In Quick Module Identification a
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
                  <b>{{ methods[methodModel].label }}</b>
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
            <ExampleSeeds :seedTypeId="seedTypeId" @addSeedsEvent="addToSelection"
                          :disabled="validationDrugView"></ExampleSeeds>
            <v-container style="height: 55vh; margin-top: 15px">
              <v-row style="height: 100%">
                <v-col cols="6">
                  <div style="height: 40vh; max-height: 40vh;">
                    <template v-if="seedTypeId!==undefined">
                      <div style="display: flex">
                        <div style="justify-content: flex-start">
                          <v-card-title style="margin-left: -25px;" class="subtitle-1">Add
                            {{ validationDrugView ? 'drugs':['genes', 'proteins'][this.seedTypeId] }} associated to
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
                                 :nodeType="validationDrugView? 'drug':['gene', 'protein'][seedTypeId]"
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
            min-height="80vh"
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
                  <v-radio-group v-model="methodModel" row>
                    <v-radio v-for="method in methods"
                             :label="method.label"
                             :key="method.label"
                             :disabled="method.id!=='bicon'&&seeds.length===0"
                    >
                    </v-radio>
                  </v-radio-group>
                  <template v-if="methodModel!==undefined">
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

                      <template v-if="methods[methodModel].id==='bicon'">
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
                        </div>
                      </template>
                      <template v-if="methods[methodModel].id==='diamond'">
                        <div>
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
                        </div>
                        <div>
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
                        </div>
                        <div>
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
                        </div>
                      </template>
                      <template v-if="methods[methodModel].id==='robust'">
                        <div>
                          <v-slider
                            hide-details
                            class="align-center"
                            v-model="models.robust.initFract"
                            min="0"
                            step="0.01"
                            max="1"
                          >
                            <template v-slot:prepend>
                              <v-text-field
                                v-model="models.robust.initFract"
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
                            v-model="models.robust.reductionFactor"
                            min="0"
                            step="0.01"
                            max="1"
                          >
                            <template v-slot:prepend>
                              <v-text-field
                                v-model="models.robust.reductionFactor"
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
                            v-model="models.robust.threshold"
                            min="-100"
                            max="0"
                          >
                            <template v-slot:prepend>
                              <v-text-field
                                prefix="10^"
                                v-model="models.robust.threshold"
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
                            v-model="models.robust.trees"
                            min="2"
                            max="100"
                          >
                            <template v-slot:prepend>
                              <v-text-field
                                v-model="models.robust.trees"
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
                      <template v-if="methods[methodModel].id==='must'">
                        <div>
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
                        </div>
                        <div>
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
                        </div>
                        <div v-show="models.must.multiple">
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
                        </div>
                        <div>
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
            :disabled="methodModel===undefined ||(methodModel===1 && models.bicon.exprFile ===undefined)"
          >
            Run
          </v-btn>

          <v-btn text @click="makeStep(2,'cancel')">
            Cancel
          </v-btn>
        </v-stepper-content>

        <v-stepper-content step="3">
          <v-card
            v-if="step===3"
            class="mb-4"
            min-height="80vh"
          >
            <v-card-subtitle class="headline">3. Module Identification Results</v-card-subtitle>
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

                  <ValidationBox ref="validation"></ValidationBox>

                  <template v-if="!loadingResults">
                    <v-data-table max-height="50vh" height="50vh" fixed-header dense item-key="id"
                                  :items="results.targets" :headers="getHeaders()" disable-pagination
                                  hide-default-footer @click:row="rowClicked">
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
                            <ResultDownload v-show="seeds && seeds.length>0" raw results seeds
                                            @downloadEvent="downloadList" @downloadResultsEvent="downloadResultList"
                                            @downloadRawEvent="downloadFullResultList"></ResultDownload>
                          </div>
                        </div>
                      </template>
                    </v-data-table>

                  </template>
                  <v-data-table v-else max-height="45vh" height="45vh" max-width="100%" fixed-header dense item-key="id"
                                :items="seeds" :headers="getHeaders(true)" disable-pagination
                                hide-default-footer @click:row="rowClicked"></v-data-table>
                </v-col>
                <v-col>
                  <Network ref="graph" :configuration="graphConfig" :window-style="graphWindowStyle"
                           :legend="results.targets.length>0" :tools="results.targets.length>0" :secondaryViewer="true"
                           @loadIntoAdvancedEvent="$emit('graphLoadEvent',{post: {id: currentGid}})">
                    <template v-slot:legend v-if="results.targets.length>0">
                      <v-card style="width: 15vw; max-width: 17vw; padding-top: 35px">
                        <v-list>
                          <v-list-item>
                            <v-list-item-icon>
                              <v-icon left color="#fbe223" size="42">fas fa-genderless
                              </v-icon>
                            </v-list-item-icon>
                            <v-list-item-title style="margin-left: -25px">Seed {{ ['Gene', 'Protein'][seedTypeId] }}
                            </v-list-item-title>
                            <v-list-item-subtitle>{{ seeds.length }}</v-list-item-subtitle>
                          </v-list-item>
                          <v-list-item style="margin-top: -15px">
                            <v-list-item-icon>
                              <v-icon size="42" left
                                      :color="getColoring('nodes',['gene','protein'][seedTypeId],'light')"
                              >fas fa-genderless
                              </v-icon>
                            </v-list-item-icon>
                            <v-list-item-title style="margin-left: -25px">Module {{ ['Gene', 'Protein'][seedTypeId] }}
                            </v-list-item-title>
                            <v-list-item-subtitle>{{ results.targets.length - seeds.length }}</v-list-item-subtitle>
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
                                    <v-chip outlined v-if="currentGid"
                                            style="margin-top:15px"
                                            @click="$emit('graphLoadNewTabEvent',{post: {id: currentGid}})">
                                      <v-icon left>fas fa-angle-double-right</v-icon>
                                      To Advanced View
                                    </v-chip>
                                  </v-list-item-action>
                                </v-list-item>
                              </template>
                              <span>Opens a new tab with an advanced view of the current network.</span>
                            </v-tooltip>
                            <v-tooltip left>
                              <template v-slot:activator="{on, attrs}">
                                <v-list-item v-on="on" v-bind="attrs">
                                  <v-list-item-action>
                                    <v-chip outlined v-show="results.targets.length>0" style="margin-top:15px"
                                            @click="loadDrugTargets">
                                      <v-icon left>fas fa-angle-double-right</v-icon>
                                      Continue to Drug-Ranking
                                    </v-chip>
                                  </v-list-item-action>
                                </v-list-item>
                              </template>
                              <span>Opens a dialog for the selection of seed nodes for a subsequent drug prioritization execution.</span>
                            </v-tooltip>
                          </v-list>
                        </v-container>
                      </v-card>
                    </template>
                  </Network>
                </v-col>
              </v-row>
            </v-container>
          </v-card>
          <v-btn
            color="primary"
            @click="makeStep(3,'back')"
          >
            Back
          </v-btn>

          <v-btn text @click="makeStep(3,'cancel')">
            Restart
          </v-btn>
        </v-stepper-content>
      </v-stepper-items>
      <v-dialog
        v-model="drugTargetPopup"
        persistent
        max-width="500"
      >
        <v-card>
          <v-card-title>Continue to Drug-Ranking</v-card-title>
          <v-card-text>Do you want to use the whole module as input for the drug ranking or just a subset?
          </v-card-text>
          <v-card-actions>
            <v-radio-group v-model="rankingSelect" row>
              <v-radio :label="'Original seeds ('+seeds.length+')'">

              </v-radio>
              <v-radio :label="'whole Module ('+(getTargetCount()+seeds.length)+')'">
              </v-radio>
              <v-radio :label="'non-seeds only ('+getTargetCount()+')'">
              </v-radio>
            </v-radio-group>
          </v-card-actions>
          <v-divider></v-divider>

          <v-card-actions>
            <v-btn
              text
              @click="resolveRankingDialog(false)"
            >
              Cancel
            </v-btn>
            <v-btn
              color="green darken-1"
              text
              @click="resolveRankingDialog(true)"
            >
              Accept
            </v-btn>
          </v-card-actions>
        </v-card>

      </v-dialog>
    </v-stepper>
  </v-card>
</template>

<script>
import Network from "../../graph/Network";
import * as CONFIG from "../../../../Config"
import SuggestionAutocomplete from "@/components/app/suggestions/SuggestionAutocomplete";
import SeedDownload from "@/components/app/tables/menus/SeedDownload";
import SeedRemove from "@/components/app/tables/menus/SeedRemove";
import SeedTable from "@/components/app/tables/SeedTable";
import ResultDownload from "@/components/app/tables/menus/ResultDownload";
import HeaderBar from "@/components/app/Header";
import NodeInput from "@/components/app/input/NodeInput";
import ExampleSeeds from "@/components/start/quick/ExampleSeeds";
import ValidationBox from "@/components/start/quick/ValidationBox";
import ValidationDrugTable from "@/components/app/tables/ValidationDrugTable";

export default {
  name: "ModuleIdentification",

  props: {
    blitz: Boolean,
  },
  sugQuery: undefined,

  data() {
    return {
      graphWindowStyle: {
        height: '60vh',
        'min-height': '60vh',
      },
      targetColorStyle: {},
      currentJid: undefined,
      currentGid: undefined,
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
      seedInput: false,
      methods: [{
        id: "diamond",
        label: "DIAMOnD",
        scores: [{id: "rank", name: "Rank"}, {id: "p_hyper", name: "P-Value", decimal: true}]
      }, {id: "bicon", label: "BiCoN", scores: []}, {id: "must", label: "MuST", scores: []},
        {id: "domino", label: "DOMINO", scores: []}, {
          id: "robust",
          label: "ROBUST",
          scores: [{id: "occs_abs", name: "Occs (Abs)"}, {id: "occs_rel", name: "Occs (%)", decimal: true}]
        }
      ],
      graph: {physics: false},
      methodModel: undefined,
      rankingMethodModel: undefined,
      experimentalSwitch: true,
      results: {targets: [], drugs: []},
      loadingResults: true,
      advancedOptions: false,
      physicsOn: false,
      models: {
        diamond: {
          nModel: 200,
          alphaModel: 1,
          pModel: 0
        },
        domino: {},
        robust: {
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
      drugTargetPopup: false,
      rankingSelect: 1,
      validationDrugCount: 0,
      validationDrugView: false,
    }

  },

  created() {
    this.$socket.$on("quickFinishedEvent", this.convertJobResult)
    this.uid = this.$cookies.get("uid")
    this.init()
  },

  methods: {
    init: function () {
      this.method = undefined;
      this.sourceType = undefined
      this.step = 1
      this.seedTypeId = undefined
      this.seeds = []
      this.currentJid = undefined
      this.currentGid = undefined
      this.methodModel = undefined
      if (this.blitz) {
        this.methodModel = 0
      }
      this.results.targets = []
      this.results.drugs = []
      this.seedOrigin = {}
      this.validationDrugCount = 0
      if (this.$refs.graph)
        this.$refs.graph.reload()
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
    focusNode: function (id) {
      if (this.$refs.graph === undefined)
        return
      this.$refs.graph.setSelection([id])
      this.$refs.graph.zoomToNode(id)
    },

    makeStep: function (s, button) {
      if (button === "continue") {
        this.step++
        if (this.step === 2)
          this.seeds = this.$refs.seedTable.getSeeds()

        if (this.blitz)
          this.step++
      }
      if (button === "back") {
        if (this.step === 3) {
          this.results.targets = []
          this.$refs.graph.reload()
          this.$socket.unsubscribeJob(this.currentJid)
        }
        this.step--
        if (this.step === 2 && this.blitz)
          this.step--
      }
      if (button === "cancel") {
        this.$socket.unsubscribeJob(this.currentJid)
        this.init()
        this.$emit("resetEvent")
      }
      if (this.step === 3)
        this.submitAlgorithm()
    },
    loadDrugTargets: function () {
      this.rankingSelect = 1
      this.drugTargetPopup = true
    },
    rowClicked: function (item) {
      this.focusNode(['gen_', 'pro_'][this.seedTypeId] + item.id)

    },
    resolveRankingDialog: function (apply) {
      this.drugTargetPopup = false;
      if (!apply)
        return

      let drugSeeds = []

      if (this.rankingSelect === 0) {
        drugSeeds = this.seeds.map(s => {
          return {id: s.id, displayName: s.displayName}
        })
      } else {
        drugSeeds = this.results.targets.map(t => {
          return {id: t.id, displayName: t.displayName}
        })
        if (this.rankingSelect === 2) {
          this.seeds.forEach(s => drugSeeds.splice(drugSeeds.indexOf(s.id), 1))
        }
      }
      this.$emit("loadDrugTargetEvent", this.blitz, drugSeeds, ["gene", "protein"][this.seedTypeId], "Module Identification: " + this.methods[this.methodModel].label)
      this.init()
    }
    ,
    biconFile: function (file) {
      this.models.bicon.exprFile = file
    }
    ,
    getHeaders: function (seeds) {
      let headers = [{text: "Name", align: "start", sortable: true, value: "displayName"}]
      if (!seeds)
        this.methodScores().forEach(e => headers.push({
          text: e.name,
          align: e.decimal ? "start" : "end",
          sortable: true,
          value: e.id,
        }))
      return headers
    },
    updateGraphPhysics: function () {
      this.$refs.graph.setPhysics(this.graph.physics)
    }
    ,

    loadExample: async function (nr) {
      let example = await this.$http.getQuickExample(nr, ["gene", "protein"][this.seedTypeId])
      let origin = "Example " + (nr + 1) + ": ("
      switch (nr) {
        case 0:
          origin += "Alzheimer)";
          break;
        case 1:
          origin += "Somatostatine and Analogues";
          break;
        case 2:
          origin += "Cancer Genes";
          break;
      }
      origin += ")"
      this.addToSelection({data: example, origin: origin})
    },

    submitAlgorithm: function () {
      let params = {}
      let method = this.methods[this.methodModel].id
      params.experimentalOnly = this.experimentalSwitch
      params["addInteractions"] = true
      params["nodesOnly"] = true
      if (method === 'bicon') {
        this.seeds = []
        params['lg_min'] = this.models.bicon.lg[0];
        params['lg_max'] = this.models.bicon.lg[1];
        params['type'] = "gene"
        this.$utils.readFile(this.models.bicon.exprFile).then(content => {
          params['exprData'] = content
          this.executeJob(method, params)
        })
        return
      }
      if (method === 'diamond') {
        params['n'] = this.models.diamond.nModel;
        params['alpha'] = this.models.diamond.alphaModel;
        params['pcutoff'] = this.models.diamond.pModel;
      }
      if (method === 'must') {
        params["penalty"] = this.models.must.hubpenalty;
        params["multiple"] = this.models.must.multiple
        params["trees"] = this.models.must.trees
        params["maxit"] = this.models.must.maxit
      }
      if (method === 'robust') {
        params["trees"] = this.models.robust.trees;
        params["initFract"] = this.models.robust.initFract;
        params["threshold"] = Math.pow(10, this.models.robust.threshold);
        params["reductionFactor"] = this.models.robust.reductionFactor;
      }
      params['type'] = ["gene", "protein"][this.seedTypeId]
      this.executeJob(method, params)
    }
    ,
    executeJob: function (algorithm, params) {
      let payload = {
        userId: this.uid,
        dbVersion: this.$global.metadata.repotrial.version,
        algorithm: algorithm,
        params: params
      }
      payload.selection = true
      payload.experimentalOnly = params.experimentalOnly
      payload["nodes"] = this.seeds.map(n => n.id)
      // if (algorithm !== "bicon") {
      //   if (this.seeds.length === 0) {
      //     this.printNotification("Cannot execute " + algorithm + " without seed nodes!", 1)
      //     return;
      //   }
      // }
      this.$http.post("/submitJob", payload).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        if (data.state === "DONE") {
          this.readJob(data, true)
        } else {
          this.$socket.subscribeJob(data.jid, "quickFinishedEvent");
          this.readJob(data)
        }
      }).catch(console.error)
    }
    ,
    readJob: function (data, notSubbed) {
      let jid = data.jid
      this.currentJid = jid
      this.currentGid = data.gid
      if (this.currentGid != null && data.state === "DONE") {
        if (!notSubbed)
          this.$socket.unsubscribeJob(jid)
        this.loadTargetTable(this.currentGid).then(() => {
          this.$refs.validation.validate(this.results.targets, this.$refs.validationTable.getDrugs(), false, ["gene", "protein"][this.seedTypeId])
          this.loadGraph(this.currentGid)
        })

      }
    }
    ,
    updateDrugCount: function(){
      this.validationDrugCount = this.$refs.validationTable.getDrugs().length;
    },
    getTargetCount: function () {
      let seedids = this.seeds.map(s => s.id)
      return this.results.targets.filter(t => seedids.indexOf(t.id) === -1).length
    },

    addToSelection: function (data) {
      if (this.validationDrugView)
        this.$refs.validationTable.addDrugs(data)
      else
        this.$refs.seedTable.addSeeds(data)
    }
    ,
    methodScores: function () {
      if (this.methodModel !== undefined && this.methodModel > -1)
        return this.methods[this.methodModel].scores;
      return []
    }
    ,
    toggleValidationDrugView: function () {
      this.$set(this, "validationDrugView", !this.validationDrugView)
    },

    downloadList: function (names, sep) {
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
    },
    downloadFullResultList: function () {
      window.open(CONFIG.HOST_URL + CONFIG.CONTEXT_PATH + '/api/downloadJobResult?jid=' + this.currentJid)
    }
    ,
    downloadResultList: function (s, names) {
      this.$http.post("mapToDomainIds", {
        type: ['gene', 'protein'][this.seedTypeId],
        ids: this.results.targets.map(l => l.id)
      }).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        let sep = s ? s : "\t"
        let text = "#ID" + (names ? sep + "Name" : "");

        this.methods[this.methodModel].scores.forEach(s => text += sep + s.name)
        text += "\n"
        this.results.targets.forEach(t => {
            text += data[t.id] + (names ? sep + t.displayName : "")
            this.methods[this.methodModel].scores.forEach(s => text += sep + t[s.id])
            text += "\n"
          }
        )
        this.download(["gene", "protein"][this.seedTypeId] + "_module." + (sep === "\t" ? "tsv" : "csv"), text)
      }).catch(console.error)
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
    ,
    convertJobResult: function (res) {
      let data = JSON.parse(res)
      this.readJob(data)
    }
    ,
    loadTargetTable: function (gid) {
      let seedType = [["gene", "protein"][this.seedTypeId]]
      let scoreAttr = this.methods[this.methodModel].scores.filter(s => s.decimal)
      this.targetColorStyle = {'background-color': this.$global.metagraph.colorMap[seedType].light}
      return this.$http.get("/getGraphList?id=" + gid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => this.$utils.roundScores(data, seedType, scoreAttr)).then(data => {
        data.nodes[seedType].forEach(n => n.displayName = this.$utils.adjustLabels(n.displayName))
        if (this.methodModel === 0)
          this.$set(this.results, 'targets', data.nodes[seedType].sort((e1, e2) => {
            if (e1.rank && e2.rank)
              return e1.rank - e2.rank
            if (e1.rank)
              return -1
            return 1
          }))
        else
          this.$set(this.results, 'targets', data.nodes[seedType])
        this.loadingResults = false;

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

    waitForGraph: function (resolve) {
      if (this.$refs.graph === undefined)
        setTimeout(this.waitForGraph, 100)
      else
        resolve()
    },
    printNotification: function (message, type) {
      this.$emit("printNotificationEvent", message, type)
    },
    getGraph: function () {
      return new Promise(resolve => this.waitForGraph(resolve)).then(() => {
        return this.$refs.graph;
      })
    },
    getColoring: function (entity, name, style) {
      return this.$utils.getColoring(this.$global.metagraph, entity, name, style);
    },
    focus: function () {
      this.$emit("focusEvent")
    },
    loadGraph: function (graphId) {
      this.getGraph().then(graph => {
        graph.loadNetworkById(graphId).then(() => {
          graph.showLoops(false)
          let seedIds = this.seeds.map(s => s.id)
          graph.modifyGroups(this.results.targets.filter(n => seedIds.indexOf(n.id) > -1).map(n => ["gen_", "pro_"][this.seedTypeId] + n.id), ["seedGene", "seedProtein"][this.seedTypeId])
        })
      })
    },
  }
  ,

  components: {
    ValidationBox,
    ValidationDrugTable,
    HeaderBar,
    SuggestionAutocomplete,
    NodeInput,
    SeedDownload,
    SeedRemove,
    SeedTable,
    ResultDownload,
    Network,
    ExampleSeeds
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
