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
          v-if="step===1"
          class="mb-12"
          min-height="80vh"
        >

          <v-card-subtitle class="headline">1. Seed Configuration</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">Add seeds to your
            list{{ blitz ? "." : " or use an expression data based algorithm (" }}<a
              @click="seedTypeId=0; methodModel=1; makeStep(1,'continue')">BiCoN
              <v-icon right size="1em" style="margin-left: 0">fas fa-caret-right</v-icon>
            </a>{{ ")." }}
          </v-card-subtitle>
          <v-divider style="margin: 15px;"></v-divider>
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
          <v-container style="height: 55vh; margin-top: 15px">
            <v-row style="height: 100%">
              <v-col cols="6">
                <div style="height: 40vh; max-height: 40vh;">
                  <template v-if="seedTypeId!==undefined">
                    <v-card-title style="margin-left: -25px" class="subtitle-1">Add
                      {{ ['genes', 'proteins'][this.seedTypeId] }} associated to
                    </v-card-title>
                    <div style="display: flex">

                      <v-select :items="getSuggestionSelection()" v-model="suggestionType"
                                placeholder="connected to" style="width: 35%; justify-self: flex-start"></v-select>
                      <SuggestionAutocomplete :suggestion-type="suggestionType"
                                              :target-node-type="['gene', 'protein'][this.seedTypeId]"
                                              @addToSelectionEvent="addToSelection"
                                              style="justify-self: flex-end;margin-left: auto"></SuggestionAutocomplete>
                    </div>
                    <v-card-subtitle>or</v-card-subtitle>
                    <div style="justify-content: center; display: flex; width: 100%">
                      <v-file-input :label="'by '+['entrez','uniprot'][seedTypeId]+' ids'"
                                    v-on:change="onFileSelected"
                                    show-size
                                    prepend-icon="far fa-list-alt"
                                    v-model="fileInputModel"
                                    dense
                                    style="width: 75%; max-width: 75%"
                      ></v-file-input>
                    </div>
                  </template>
                </div>
              </v-col>

              <v-divider vertical v-show="seedTypeId!==undefined"></v-divider>
              <v-col cols="6">
                <v-data-table max-height="40vh" height="40vh" class="overflow-y-auto overflow-x-hidden" fixed-header
                              dense item-key="id"
                              :items="seeds"
                              :headers="[{text: 'Name', align: 'start', sortable: true, value: 'displayName'},{text: 'Origin', align: 'start',sortable: true, value: 'origin'},{text: 'Action', align: 'end', sortable: false, value: 'action'}]"
                              disable-pagination
                              hide-default-footer
                              v-show="seedTypeId!==undefined"
                style="margin-top: 16px">
                  <template v-slot:top>
                    <div style="display: flex">
                      <v-card-title style="justify-self: flex-start" class="subtitle-1">Selected Seeds ({{
                          seeds.length
                        }})
                      </v-card-title>
                    </div>
                  </template>
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
                  <template v-slot:item.origin="{item}">
                    <template v-for="o in getOrigins(item.id)">
                      <v-tooltip bottom :key="item.id+o">
                        <template v-slot:activator="{attr,on }">
                          <v-chip style="font-size: smaller; color: gray" pill v-on="on" v-bind="attr">{{
                              o[0]
                            }}
                          </v-chip>
                        </template>
                        <span v-if="o[2]">Connected to <b>{{ o[2] }}</b>:<br><b>{{ o[1] }}</b></span>
                        <span v-else-if="o[0]==='FILE'">Added from user file:<br><b>{{ o[1] }}</b></span>
                        <span v-else>Returned by method:<br><b>{{ o[1] }}</b></span>
                      </v-tooltip>
                    </template>
                  </template>
                  <template v-slot:header.origin="{header}">
                    <v-tooltip bottom>
                      <template v-slot:activator="{attr,on }">
                          <span v-bind="attr" v-on="on">
                          Origin
                          </span>

                      </template>
                      <span>Holds the sources the seed node was added by:<br><b>SUG=</b>Suggestion, <b>FILE</b>=File input or <b>METH</b>=Other method</span>
                    </v-tooltip>
                  </template>
                  <template v-slot:header.displayName="{header}">
                    <v-tooltip bottom>
                      <template v-slot:activator="{attr,on }">
                          <span v-bind="attr" v-on="on">
                          Name
                          </span>
                      </template>
                      <span>Holds the common name of the seed {{
                          ["genes", "proteins"][seedTypeId]
                        }} if available.</span>
                    </v-tooltip>
                  </template>
                  <template v-slot:item.action="{item}">
                    <v-tooltip right>
                      <template v-slot:activator="{attr,on }">
                        <v-btn icon @click="removeSeed(item.id)" color="red">
                          <v-icon>far fa-trash-alt</v-icon>
                        </v-btn>
                      </template>
                      <span>Remove the current entry from the seed selection!</span>
                    </v-tooltip>
                  </template>
                  <template v-slot:footer>
                    <div style="display: flex; justify-content: center;padding-top: 16px" v-if="seeds && seeds.length>0">
                      <div >
                        <v-menu top offset-y transition="slide-y-reverse-transition">
                          <template v-slot:activator="{on,attrs}">
                            <v-btn small outlined right v-bind="attrs" v-on="on">
                              <v-icon left color="primary">
                                fas fa-download
                              </v-icon>
                              Download
                            </v-btn>
                          </template>
                          <v-list style="font-size: smaller; color: gray" dense>
                            <v-list-item @click="downloadList()" style="cursor:pointer">
                              <v-icon left size="1em">
                                fas fa-download
                              </v-icon>
                              IDs only
                            </v-list-item>
                            <v-menu right offset-x transition="slide-x-transition" open-on-hover>
                              <template v-slot:activator="{on,attrs}">
                                <v-list-item v-bind="attrs" v-on="on">
                                  With names
                                  <v-icon right>fas fa-caret-right</v-icon>
                                </v-list-item>
                              </template>
                              <v-list dense>
                                <v-list-item @click="downloadList(true,'\t')"
                                             style="cursor:pointer; font-size: smaller; color: gray">
                                  <v-icon left size="1em">
                                    fas fa-download
                                  </v-icon>
                                  As .tsv
                                </v-list-item>
                                <v-list-item @click="downloadList(true,',')"
                                             style="cursor:pointer; font-size: smaller; color: gray">
                                  <v-icon left size="1em">
                                    fas fa-download
                                  </v-icon>
                                  As .csv

                                </v-list-item>
                              </v-list>
                            </v-menu>
                          </v-list>
                        </v-menu>
                      </div>
                      <div style="margin-left: 5px;">
                        <v-menu top offset-y transition="slide-y-reverse-transition">
                          <template v-slot:activator="{on,attrs}">
                            <v-btn small outlined right v-bind="attrs" v-on="on">
                              <v-icon left color="primary">
                                fas fa-trash-alt
                              </v-icon>
                              Remove
                            </v-btn>
                          </template>
                          <v-list style="font-size: smaller; color: gray" dense>
                            <v-list-item @click="clearList" style="cursor:pointer">
                              All
                            </v-list-item>
                            <v-list-item @click="removeNonIntersecting" style="cursor:pointer">
                              With one origin only
                            </v-list-item>
                          </v-list>
                        </v-menu>
                      </div>
                    </div>
                  </template>
                </v-data-table>
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
          min-height="80vh"
        >
          <v-card-subtitle class="headline">2. Module Identification Algorithm Selection</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">Select and adjust the algorithm you want to apply on your seeds to
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
          class="mb-12"
          min-height="80vh"
        >
          <v-card-subtitle class="headline">3. Module Identification Results</v-card-subtitle>
          <v-divider style="margin: 15px;"></v-divider>
          <v-container>
            <v-row>
              <v-col cols="3" style="padding: 0 50px 0 0; margin-right: -50px">
                <v-card-title class="subtitle-1">Seeds ({{ seeds.length }}) {{
                    (results.targets.length !== undefined && results.targets.length > 0 ? ("& Module (" + getTargetCount() + ")") : ": Processing")
                  }}
                  <v-progress-circular indeterminate v-if="this.results.targets.length===0" style="margin-left:15px">
                  </v-progress-circular>
                </v-card-title>
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
                          <v-menu top offset-y transition="slide-y-reverse-transition" v-if="seeds && seeds.length>0">
                            <template v-slot:activator="{on,attrs}">
                              <v-btn small outlined right v-bind="attrs" v-on="on">
                                <v-icon left color="primary">
                                  fas fa-download
                                </v-icon>
                                Download
                              </v-btn>
                            </template>
                            <v-list style="font-size: smaller; color: gray" dense>
                              <v-menu right offset-x transition="slide-x-transition" open-on-hover>
                                <template v-slot:activator="{on,attrs}">
                                  <v-list-item v-bind="attrs" v-on="on">
                                    Seeds
                                    <v-icon right>fas fa-caret-right</v-icon>
                                  </v-list-item>
                                </template>
                                <v-list style="color: gray" dense>
                                  <v-list-item @click="downloadList(true,'\t')"
                                               style="cursor:pointer; font-size: smaller; color: gray">
                                    <v-icon left size="1em">
                                      fas fa-download
                                    </v-icon>
                                    As .tsv
                                  </v-list-item>
                                  <v-list-item @click="downloadList(true,',')"
                                               style="cursor:pointer; font-size: smaller; color: gray">
                                    <v-icon left size="1em">
                                      fas fa-download
                                    </v-icon>
                                    As .csv
                                  </v-list-item>
                                </v-list>
                              </v-menu>
                              <v-menu right offset-x transition="slide-x-transition" open-on-hover>
                                <template v-slot:activator="{on,attrs}">
                                  <v-list-item v-bind="attrs" v-on="on">
                                    Module
                                    <v-icon right>fas fa-caret-right</v-icon>
                                  </v-list-item>
                                </template>
                                <v-list style="color: gray" dense>
                                  <v-list-item @click="downloadResultList('\t',true)"
                                               style="cursor:pointer; font-size: smaller; color: gray">
                                    <v-icon left size="1em">
                                      fas fa-download
                                    </v-icon>
                                    As .tsv
                                  </v-list-item>
                                  <v-list-item @click="downloadResultList(',',true)"
                                               style="cursor:pointer; font-size: smaller; color: gray">
                                    <v-icon left size="1em">
                                      fas fa-download
                                    </v-icon>
                                    As .csv
                                  </v-list-item>
                                </v-list>
                              </v-menu>
                              <v-list-item @click="downloadFullResultList">
                                <v-icon left size="1em">
                                  fas fa-download
                                </v-icon>
                                Raw Results
                              </v-list-item>
                            </v-list>
                          </v-menu>
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
                <Graph ref="graph" :configuration="graphConfig" :window-style="graphWindowStyle"
                       :legend="results.targets.length>0" :meta="metagraph">
                  <template v-slot:legend v-if="results.targets.length>0">
                    <v-card style="width: 15vw; max-width: 17vw; padding-top: 35px">
                      <v-list>
                        <v-list-item>
                          <v-list-item-icon>
                            <v-icon left :color="getColoring('nodes',['gene','protein'][seedTypeId],'light')"
                                    size="43px">fas fa-genderless
                            </v-icon>
                          </v-list-item-icon>
                          <v-list-item-title style="margin-left: -25px">Seed {{ ['Gene', 'Protein'][seedTypeId] }}
                          </v-list-item-title>
                          <v-list-item-subtitle>{{ seeds.length }}</v-list-item-subtitle>
                        </v-list-item>
                        <v-list-item>
                          <v-list-item-icon>
                            <v-icon left :color="getColoring('nodes',['gene','protein'][seedTypeId],'light')">fas
                              fa-circle
                            </v-icon>
                          </v-list-item-icon>
                          <v-list-item-title style="margin-left: -25px">Module {{ ['Gene', 'Protein'][seedTypeId] }}
                          </v-list-item-title>
                          <v-list-item-subtitle>{{ results.targets.length - seeds.length }}</v-list-item-subtitle>
                        </v-list-item>
                      </v-list>
                    </v-card>
                  </template>

                </Graph>
              </v-col>
            </v-row>
            <v-divider style="margin-top:10px; margin-bottom: 10px"></v-divider>
            <v-row>
              <v-col>
                <v-chip outlined v-if="currentGid"
                        style="margin-top:15px"
                        @click="$emit('graphLoadEvent',{post: {id: currentGid}})">
                  <v-icon left>fas fa-angle-double-right</v-icon>
                  Load Result into Advanced View
                </v-chip>
              </v-col>
              <v-col>
                <v-switch label="Physics" v-model="graph.physics" @click="updateGraphPhysics()"
                          v-if="results.targets.length>0">
                </v-switch>

              </v-col>
              <v-col>
                <v-chip outlined v-show="results.targets.length>0" style="margin-top:15px" @click="loadDrugTargets">
                  <v-icon left>fas fa-angle-double-right</v-icon>
                  Continue to Drug-Ranking
                </v-chip>
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
</template>

<script>
import Graph from "../../graph/Graph";
import * as CONFIG from "../../../../Config"
import SuggestionElement from "@/components/app/suggestions/SuggestionElement";
import SuggestionAutocomplete from "@/components/app/suggestions/SuggestionAutocomplete";

export default {
  name: "ModuleIdentification",

  props: {
    blitz: Boolean,
    metagraph: Object,
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
      methods: [{
        id: "diamond",
        label: "DIAMOnD",
        scores: [{id: "rank", name: "Rank"}, {id: "p_hyper", name: "P-Value", decimal: true}]
      }, {id: "bicon", label: "BiCoN", scores: []}, {id: "must", label: "MuST", scores: []}],
      graph: {physics: false},
      methodModel: undefined,
      rankingMethodModel: undefined,
      experimentalSwitch: true,
      results: {targets: [], drugs: []},
      loadingResults: true,
      models: {
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
      drugTargetPopup: false,
      rankingSelect: 1
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
      if (this.$refs.graph)
        this.$refs.graph.reload()
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

    makeStep: function (s, button) {
      if (button === "continue") {
        this.step++
        if (this.step === 2 && this.blitz)
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
      this.$emit("loadDrugTargetEvent", this.blitz, drugSeeds, ["gene", "protein"][this.seedTypeId])
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
      params['type'] = ["gene", "protein"][this.seedTypeId]
      this.executeJob(method, params)
    }
    ,
    executeJob: function (algorithm, params) {
      let payload = {userId: this.uid, algorithm: algorithm, params: params}
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
        this.$socket.subscribeJob(data.jid, "quickFinishedEvent");
        this.readJob(data)
      }).catch(console.log)
    }
    ,
    readJob: function (data) {
      let jid = data.jid
      this.currentJid = jid
      this.currentGid = data.gid
      if (this.currentGid != null && data.state === "DONE") {
        this.$socket.unsubscribeJob(jid)
        this.loadTargetTable(this.currentGid).then(() => {
          this.loadGraph(this.currentGid)
        })

      }
    }
    ,
    getTargetCount: function () {
      let seedids = this.seeds.map(s => s.id)
      return this.results.targets.filter(t => seedids.indexOf(t.id) === -1).length
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
    methodScores: function () {
      if (this.methodModel !== undefined && this.methodModel > -1)
        return this.methods[this.methodModel].scores;
      return []
    }
    ,


    getOrigins: function (id) {
      if (this.seedOrigin[id] === undefined)
        return ["?"]
      else
        return this.seedOrigin[id].map(item => {
          let sp1 = item.split(":")
          let out = []
          out.push(sp1[0])
          if (out[0] === 'SUG') {
            let sp2 = sp1[1].split("[")
            out.push(sp2[0])
            out.push(sp2[1].substring(0, sp2[1].length - 1))
          } else {
            out.push(sp1[1])
          }
          return out;
        })
    }
    ,
    removeSeed: function (id) {
      let index = this.seeds.map(e => e.id).indexOf(id)
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
      }).catch(console.log)
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
    ,
    convertJobResult: function (res) {
      let data = JSON.parse(res)
      this.readJob(data)
    }
    ,
    loadTargetTable: function (gid) {
      let seedType = [["gene", "protein"][this.seedTypeId]]
      let scoreAttr = this.methods[this.methodModel].scores.filter(s => s.decimal)
      this.targetColorStyle = {'background-color': this.metagraph.colorMap[seedType].light}
      return this.$http.get("/getGraphList?id=" + gid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => this.$utils.roundScores(data, seedType, scoreAttr)).then(data => {
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
      }).catch(console.log)
    },

    waitForGraph: function (resolve) {
      if (this.$refs.graph === undefined)
        setTimeout(this.waitForGraph, 100)
      else
        resolve()
    },
    getGraph: function () {
      return new Promise(resolve => this.waitForGraph(resolve)).then(() => {
        return this.$refs.graph;
      })
    },
    getColoring: function (entity, name) {
      return this.$utils.getColoring(this.metagraph, entity, name);
    },
    focus: function () {
      this.$emit("focusEvent")
    },
    loadGraph: function (graphId) {
      this.getGraph().then(graph => {
        graph.setLoading(true)
        graph.show(graphId).then(() => {
          graph.showLoops(false)
          let seedIds = this.seeds.map(s => s.id)
          graph.modifyGroups(this.results.targets.filter(n => seedIds.indexOf(n.id) === -1).map(n => ["gen_", "pro_"][this.seedTypeId] + n.id), ["geneModule", "proteinModule"][this.seedTypeId])
          graph.setLoading(false)
        })
      })
    }
  }
  ,

  components: {
    SuggestionAutocomplete,
    Graph
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
