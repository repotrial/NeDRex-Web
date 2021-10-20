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
          <small v-if="step>2 && $refs.moduleAlgorithms.getAlgorithm()!=null">{{
              $refs.moduleAlgorithms.getAlgorithm().label
            }}</small>
        </v-stepper-step>
        <v-divider></v-divider>
        <v-stepper-step step="3" :complete="step>3 || blitz">
          Ranking Method
          <small v-if="rankingAlgorithmSelected">{{ this.$refs.rankingAlgorithms.getAlgorithm().label }}</small>
        </v-stepper-step>
        <v-divider></v-divider>
        <v-stepper-step step="4" :complete="step>4">
          Results
        </v-stepper-step>
        <v-divider></v-divider>
        <v-stepper-step step="5">
          Validation
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
                  <v-container style="max-width: 100%">
                    <v-row>
                      <v-col>
                        <i>
                         Default MI Algorithm:
                        </i>
                      </v-col>
                      <v-col>
                        <b v-if="moduleAlgorithmSelected">{{ $refs.moduleAlgorithms.getAlgorithm().label }}</b>
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
                        <b v-if="rankingAlgorithmSelected">{{ this.$refs.rankingAlgorithms.getAlgorithm().label }}</b>
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
                <v-list-item-subtitle class="title">Select the seed type
                </v-list-item-subtitle>
                <v-list-item-action>
                  <v-radio-group row v-model="seedTypeId"
                                 :disabled="(this.seedTypeId !=null && $refs.seedTable !=null && $refs.seedTable.getSeeds()!=null && $refs.seedTable.getSeeds().length>0)">
                    <v-radio label="Gene">
                    </v-radio>
                    <v-radio label="Protein">
                    </v-radio>
                  </v-radio-group>
                </v-list-item-action>
              </v-col>
            </v-row>
            <ExampleSeeds :seedTypeId="seedTypeId" @addSeedsEvent="addToSelection"
            ></ExampleSeeds>
            <v-container style="height: 560px;margin: 15px;max-width: 100%">
              <v-row style="height: 100%">
                <v-col cols="6">
                  <div style="height: 40vh; max-height: 40vh;">
                    <template v-if="seedTypeId!==undefined">
                      <div style="display: flex">
                        <div style="justify-content: flex-start">
                          <v-card-title style="margin-left: -25px;" class="subtitle-1">Add
                            {{ ['genes', 'proteins'][this.seedTypeId] }} associated to
                          </v-card-title>
                        </div>
                        <v-tooltip top>
                          <template v-slot:activator="{on,attrs}">
                            <div v-on="on" v-bind="attrs" style="justify-content: flex-end; margin-left: auto">
                              <LabeledSwitch v-model="advancedOptions"
                                             @click="suggestionType = advancedOptions ? suggestionType : 'disorder'"
                                             label-off="Limited" label-on="Full" v-on="on"
                                             v-bind="attrs"></LabeledSwitch>
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
                        <SuggestionAutocomplete :suggestion-type="suggestionType" :emit-drugs="true"
                                                :emit-disorders="true"
                                                @drugsEvent="$refs.validation.addDrugs"
                                                @disorderEvent="saveDisorders"
                                                :target-node-type="['gene', 'protein'][seedTypeId]"
                                                @addToSelectionEvent="addToSelection"
                                                @suggestionEvent="addToSuggestions"
                                                style="justify-self: flex-end;margin-left: auto"></SuggestionAutocomplete>
                      </div>
                      <NodeInput text="or provide Seed IDs by" @addToSelectionEvent="addToSelection"
                                 :idName="['entrez','uniprot'][seedTypeId]"
                                 :nodeType="['gene', 'protein'][this.seedTypeId]"
                                 @printNotificationEvent="printNotification"></NodeInput>
                    </template>
                  </div>
                </v-col>

                <v-divider vertical v-show="seedTypeId!==undefined"></v-divider>
                <v-col cols="6" style="padding-top:0">
                  <v-tooltip left>
                    <template v-slot:activator="{attrs,on}">
                      <v-chip style="position: absolute; left:auto; right:0" v-on="on" v-bind="attrs"
                              v-show="seedTypeId!=null"
                              color='primary'>
                        <v-icon left>fas fa-capsules</v-icon>
                        {{ validationDrugCount }}
                      </v-chip>
                    </template>
                    <span>There are {{ validationDrugCount }} drugs that were associated with your query.<br> These are saved for validation purposes later.<br><br><i>To see or even adjust the list, toggle this button!</i></span>
                  </v-tooltip>
                  <SeedTable ref="seedTable" v-show="seedTypeId!=null" :download="true"
                             :remove="true"
                             :filter="true"
                             @printNotificationEvent="printNotification"
                             height="405px"
                             :title="'Selected Seeds ('+($refs.seedTable ? $refs.seedTable.getSeeds().length : 0)+')'"
                             :nodeName="['gene','protein'][seedTypeId]"
                  ></SeedTable>
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
          <MIAlgorithmSelect ref="moduleAlgorithms" :blitz="blitz" :seeds="seeds" :seed-type-id="seedTypeId"
                             socket-event="quickRepurposeModuleFinishedEvent"
                             @algorithmSelectedEvent="acceptModuleAlgorithmSelectEvent"
                             @jobEvent="readModuleJob"></MIAlgorithmSelect>
          <v-btn text @click="makeStep(2,'back')">
            Back
          </v-btn>
          <v-btn
            @click="makeStep(2,'continue')"
            color="primary"
            :disabled=" !moduleAlgorithmSelected  || ($refs.moduleAlgorithms.getAlgorithmMethod()==='bicon' && $refs.moduleAlgorithms.getAlgorithmModels().exprFile ===undefined)"
          >
            Run
          </v-btn>

          <v-btn text @click="makeStep(2,'cancel')">
            Cancel
          </v-btn>
        </v-stepper-content>

        <v-stepper-content step="3">
          <DPAlgorithmSelect ref="rankingAlgorithms" :blitz="blitz" :step="3" :seeds="seeds" :seed-type-id="seedTypeId"
                             socket-event="quickRepurposeRankingFinishedEvent"
                             @algorithmSelectedEvent="acceptRankingAlgorithmSelectEvent"
                             @jobEvent="readRankingJob"></DPAlgorithmSelect>

          <v-btn text @click="makeStep(3,'back')">
            Back
          </v-btn>

          <v-btn
            @click="makeStep(3,'continue')"
            color="primary"
            :disabled="!rankingAlgorithmSelected"
          >
            Run
          </v-btn>

          <v-btn text @click="makeStep(3,'cancel')">
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
            <v-container style="max-width: 100%">
              <v-row>
                <v-col cols="3" style="padding: 0 50px 0 0; margin-right: -50px">
                  <v-card-title class="subtitle-1">Seeds ({{ seeds.length }}) {{
                      (results.targets.length !== undefined && results.targets.length > 0 ? ("& Module (" + getTargetCount() + ") " + ["Genes", "Proteins"][seedTypeId]) : ": Processing")
                    }}
                    <v-progress-circular indeterminate size="25" v-if="this.results.targets.length===0"
                                         style="margin-left:15px; z-index:50">
                    </v-progress-circular>
                  </v-card-title>
                  <v-data-table max-height="50vh" height="50vh" class="overflow-y-auto" fixed-header dense item-key="id"
                                :items="(!results.targets ||results.targets.length ===0) ?seeds : results.targets"
                                :headers="getHeaders(0)"
                                disable-pagination show-expand :single-expand="true"
                                hide-default-footer @click:row="seedClicked">
                    <template v-slot:item.displayName="{item}">
                      <v-tooltip v-if="item.displayName.length>12" right>
                        <template v-slot:activator="{attr,on }">
                          <span v-bind="attr" v-on="on"
                                style="color: dimgray">{{ item.displayName.substr(0, 12) }}...</span>
                        </template>
                        <span>{{ item.displayName }}</span>
                      </v-tooltip>
                      <span v-else>{{ item.displayName }}</span>
                    </template>
                    <template v-slot:item.data-table-expand="{expand, item,isExpanded}">
                      <v-icon v-show="!isExpanded" @click="expand(true)">fas fa-angle-down</v-icon>
                      <v-icon v-show="isExpanded" @click="expand(false)">fas fa-angle-up</v-icon>
                    </template>
                    <template v-slot:expanded-item="{ headers, item }">
                      <td :colspan="headers.length">
                        <EntryDetails max-width="17vw"
                                      :attributes="[geneDetailAttributes,proteinDetailAttributes][seedTypeId]"
                                      :detail-request="{edge:false, type:['gene','protein'][seedTypeId], id:item.id}"></EntryDetails>
                      </td>
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
                              <v-icon left color="#fbe223" size="42">fas fa-genderless
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
                              <v-icon left :color="getColoring('nodes',['gene','protein'][seedTypeId],'light')"
                                      size="42">fas fa-genderless
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
                              <v-icon left :color="getColoring('nodes','drug','light')" size="42">fas fa-genderless
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
                <v-col style="padding:0; max-width: 31%; width: 31%">
                  <v-card-title class="subtitle-1"> Drugs{{
                      (results.drugs.length !== undefined && results.drugs.length > 0 ? (" (" + (results.drugs.length) + ")") : ": Processing")
                    }}
                    <v-progress-circular indeterminate size="25" v-if="results.drugs.length===0"
                                         style="margin-left:15px; z-index:50">
                    </v-progress-circular>
                  </v-card-title>
                  <template v-if="results.drugs.length>0">
                    <v-data-table max-height="50vh" height="50vh" class="overflow-y-auto" fixed-header dense
                                  item-key="id" show-expand :single-expand="true"
                                  :items="results.drugs"
                                  :headers="getHeaders(1)"
                                  disable-pagination
                                  hide-default-footer @click:row="drugClicked">
                      <template v-slot:item.displayName="{item}">
                        <v-tooltip v-if="item.displayName.length>12" right>
                          <template v-slot:activator="{attr,on }">
                          <span v-bind="attr" v-on="on"
                                style="color: dimgray">{{ item.displayName.substr(0, 12) }}...</span>
                          </template>
                          <span>{{ item.displayName }}</span>
                        </v-tooltip>
                        <span v-else>{{ item.displayName }}</span>
                      </template>
                      <template v-slot:header.trialCount="{header}">
                        <v-tooltip bottom>
                          <template v-slot:activator="{on, attrs}">
                            <span v-on="on" v-bind="attrs">{{ header.text }}</span>
                          </template>
                          <span>Entries in this column can contain following values<br><v-icon left color="white"
                                                                                               size="12pt">fas fa-check</v-icon> This drug is already known to be effective against at least one of the selected disorders<br>
                            <v-icon left color="white"
                                    size="12pt">fas fa-clinic-medical</v-icon> This drug has some clinical trial entries for treatments of the selected disorders.</span>
                        </v-tooltip>
                      </template>
                      <template v-slot:item.trialCount="{item}">
                        <v-tooltip v-if="item.known" left>
                          <template v-slot:activator="{attr,on }">
                            <span v-bind="attr" v-on="on"><v-icon size="1rem" left>fas fa-check</v-icon></span>
                          </template>
                          <span>This drug is already known to have an effect on at least one of the initially selected disorders.</span>
                        </v-tooltip>
                        <span v-if="item.known && item.trials" style="margin-left: 5px"></span>
                        <v-tooltip v-if="item.trials!=null" left>
                          <template v-slot:activator="{attr,on }">
                            <span v-bind="attr" v-on="on"><v-icon>fas fa-clinic-medical</v-icon></span>
                          </template>
                          <span>There is at least one entry for trials regarding one of the<br> initially selected disorders and this drug on e.g. ClinicalTrial.gov ({{item.trialCount }}). Expand the entry to find some linked studies!</span>
                        </v-tooltip>

                      </template>
                      <template v-slot:item.data-table-expand="{expand, item,isExpanded}">
                        <v-icon v-show="!isExpanded" @click="expand(true)">fas fa-angle-down</v-icon>
                        <v-icon v-show="isExpanded" @click="expand(false)">fas fa-angle-up</v-icon>
                      </template>
                      <template v-slot:expanded-item="{ headers, item }">
                        <td :colspan="headers.length">
                          <EntryDetails max-width="25vw" :attributes="drugDetailAttributes"
                                        :additions="(item.trials != null ?  [{pos:3,key:'ClinicalTrials',value:item.trials}]:null)"
                                        :detail-request="{edge:false, type:'drug', id:item.id}"></EntryDetails>
                        </td>
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

          <v-btn text @click="makeStep(4,'continue')" :disabled="rankingGid ==null">
            Validate
          </v-btn>
        </v-stepper-content>
        <v-stepper-content step="5">
          <Validation ref="validation" :step="5" :seed-type-id="seedTypeId" :module="results.targets"
                      :ranking="results.drugs" @drugCountUpdate="updateDrugCount"
                      @printNotificationEvent="printNotification"></Validation>
          <v-btn text @click="makeStep(5,'back')">
            Back
          </v-btn>

          <v-btn text @click="makeStep(5,'cancel')">
            Restart
          </v-btn>
        </v-stepper-content>
      </v-stepper-items>


    </v-stepper>
    <v-dialog v-model="namePopup"
              persistent
              max-width="500px"
    >
      <v-card>
        <v-card-title>Set graph name</v-card-title>
        <v-card-text>Please enter a useful graph name, to find your graph easier in the history again. Or select one
          of
          the autogenerated options that are based on your input.
        </v-card-text>
        <v-card-actions>
          <v-text-field label="Name" v-model="graphName"></v-text-field>
        </v-card-actions>
        <v-card-text v-for="option in nameOptions" :key="option">
          <div>{{ option }}</div>
          <v-chip @click="graphName=option" style="font-size: 8pt">Load
            <v-icon right>fas fa-angle-double-right</v-icon>
          </v-chip>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-btn
            text
            @click="resolveNamingDialog()"
          >
            Cancel
          </v-btn>
          <v-btn
            color="green darken-1"
            text
            @click="resolveNamingDialog(graphName)"
            :disabled="graphName==null || graphName.length ===0"
          >
            Accept
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
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
import EntryDetails from "@/components/app/EntryDetails";
import LabeledSwitch from "@/components/app/input/LabeledSwitch";
import MIAlgorithmSelect from "@/components/start/quick/MIAlgorithmSelect";
import DPAlgorithmSelect from "@/components/start/quick/DPAlgorithmSelect";
import Validation from "@/components/start/quick/Validation";

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
      moduleAlgorithmSelected: false,
      rankingAlgorithmSelected: false,
      physicsOn: false,
      graph: {physics: false},
      results: {seeds: [], targets: []},
      drugTargetPopup: false,
      rankingSelect: 1,
      moduleGid: undefined,
      moduleJid: undefined,
      rankingGid: undefined,
      rankingJid: undefined,
      validationDrugCount: 0,
      drugDetailAttributes: ["Name", "SourceID", "SourceIDs", "Formula", "Indication", "Description", "Synonyms"],
      geneDetailAttributes: ["Name", "SourceID", "SourceIDs", "Symbols", "Chromosome", "Genomic Location", "Synonyms", "Description"],
      proteinDetailAttributes: ["Name", "SourceID", "SourceIDs", "Gene", "Synonyms", "Comments"],
      disorderIds: [],

      selectedSuggestions: [],
      namePopup: false,
      nameOptions: [],
      graphName: "",
    }
  },

  created() {
    this.$socket.$on("quickRepurposeModuleFinishedEvent", this.convertModuleJob)
    this.$socket.$on("quickRepurposeRankingFinishedEvent", this.convertRankingJob)
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
      this.disorderIds = []
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
    acceptModuleAlgorithmSelectEvent: function (value) {
      this.moduleAlgorithmSelected = value;
    },
    acceptRankingAlgorithmSelectEvent: function (value) {
      this.rankingAlgorithmSelected = value;
    },
    makeStep: function (s, button) {
      if (button === "continue") {
        this.step++
        if (this.step === 3)
          this.$refs.moduleAlgorithms.run()

        if (this.step === 2) {
          this.seeds = this.$refs.seedTable.getSeeds()
          if (this.blitz) {
            this.$refs.moduleAlgorithms.run()
            this.step += 2
          }
        }

        if (this.step === 4) {
          this.graphNamePopup()
          this.submitRankingAlgorithm()
        }
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
    updateGraphPhysics: function () {
      this.$refs.graph.setPhysics(this.graph.physics)
    }
    ,
    getTargetCount: function () {
      let seedids = this.seeds.map(s => s.id)
      return this.results.targets.filter(t => seedids.indexOf(t.id) === -1).length
    },
    convertModuleJob: function (res) {
      let data = JSON.parse(res)
      this.readModuleJob(data)
    },
    convertRankingJob: function (res) {
      let data = JSON.parse(res)
      this.readRankingJob(data)
    },
    waitForModuleJob: function (resolve) {
      if (this.moduleGid == null)
        setTimeout(this.waitForModuleJob, 100, resolve)
      else
        resolve()
    },
    submitRankingAlgorithm: function () {
      let ctx = this
      new Promise(resolve => this.waitForModuleJob(resolve)).then(() => {
        ctx.$refs.rankingAlgorithms.run(ctx.moduleGid)
      })
    },

    readModuleJob: function (data, notSubbed, dirty) {
      this.resultProgress += 5
      let jid = data.jid
      this.moduleJid = jid
      this.moduleGid = data.gid
      if (this.moduleGid != null && data.state === "DONE") {
        if (!notSubbed)
          this.$socket.unsubscribeJob(jid)
        this.loadModuleTargetTable(this.moduleGid).then(() => {
          this.resultProgress = 25
          // this.$refs.validation.validate()

          this.loadGraph(this.moduleGid, true)
        })
      }
    },
    saveDisorders: function (list) {
      this.disorderIds = this.disorderIds.concat(list.filter(id => this.disorderIds.indexOf(id) === -1))
    },
    updateDrugCount: function () {
      this.validationDrugCount = this.$refs.validation.getDrugs().length;
    },
    readRankingJob: function (result, unsubscribed, dirty) {
      this.resultProgress += 5
      let data = !dirty ? result : JSON.parse(result)
      this.rankingJid = data.jid
      this.rankingGid = data.gid
      if (this.rankingGid != null && data.state === "DONE") {
        this.resultProgress = 75
        if (!unsubscribed)
          this.$socket.unsubscribeJob(this.rankingJid)
        this.loadRankingTargetTable(this.rankingGid).then(() => {
          // this.$refs.validation.validate()
          this.loadGraph(this.rankingGid)
        })

      }
    },

    addToSelection: function (data) {
      this.$refs.seedTable.addSeeds(data)
    },
    moduleMethodScores: function () {
      return this.$refs.moduleAlgorithms.getHeaders()
    }
    ,
    rankingMethodScores: function () {
      return this.$refs.rankingAlgorithms.getHeaders()
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
        let scores = this.$refs.moduleAlgorithms.getAlgorithm().scores
        scores.forEach(s => text += sep + s.name)
        text += "\n"
        this.results.targets.forEach(t => {
            text += data[t.id] + (names ? sep + t.displayName : "")
            scores.forEach(s => text += sep + t[s.id])
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
        let scores = this.$refs.rankingAlgorithms.getAlgorithm().scores
        scores.forEach(s => text += sep + s.name)
        text += "\n"
        this.results.drugs.forEach(t => {
            text += data[t.id] + sep + t.displayName
            scores.forEach(s => text += sep + t[s.id])
            text += "\n"
          }
        )
        this.download("drug_ranking-Top_" + this.$refs.rankingAlgorithms.getAlgorithmModels().topX + (sep === "\t" ? ".tsv" : ".csv"), text)
      }).catch(console.error)
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
    seedValueReplacement: function (list, method) {
      let seedIds = this.seeds.map(n => n.id)
      let seeds = list.filter(n => seedIds.indexOf(n.id > -1))
      method.scores.forEach(score => seeds.filter(n => n[score.id] == null).forEach(n => n[score.id] = score.seed))
    },

    loadModuleTargetTable: function () {
      let seedType = ['gene', 'protein'][this.seedTypeId]
      this.targetColorStyle = {'background-color': this.$global.metagraph.colorMap[seedType].light}
      return this.$http.get("/getGraphList?id=" + this.moduleGid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        data.nodes[seedType].forEach(n => n.displayName = this.$utils.adjustLabels(n.displayName))

        let method = this.$refs.moduleAlgorithms.getAlgorithm()

        let primaryAttribute = method.scores.filter(s => s.primary)[0]
        this.seedValueReplacement(data.nodes[seedType], method)
        this.results.targets = this.sort(data.nodes[seedType], primaryAttribute)
        this.rank(this.results.targets, primaryAttribute)
        this.normalize(this.results.targets, method)
        this.round(this.results.targets, method)
      }).catch(console.error)
    },
    loadRankingTargetTable: function () {
      this.drugColorStyle = {'background-color': this.$global.metagraph.colorMap['drug'].light}
      return this.$http.get("/getGraphList?id=" + this.rankingGid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        let method = this.$refs.rankingAlgorithms.getAlgorithm()
        let primaryAttribute = method.scores.filter(s => s.primary)[0]
        this.results.drugs = this.sort(data.nodes.drug, primaryAttribute)
        this.rank(this.results.drugs, primaryAttribute)
        this.normalize(this.results.drugs, method)
        this.round(this.results.drugs, method)
        this.addTrialsNumber(this.results.drugs, method)
      }).catch(console.error)
    },


    sort: function (list, attribute) {
      if (attribute == null)
        return list
      return attribute.order === "descending" ? list.sort((e1, e2) => e2[attribute.id] - e1[attribute.id]) : list.sort((e1, e2) => e1[attribute.id] - e2[attribute.id])
    },

    round: function (list, method) {
      method.scores.filter(s => s.decimal).forEach(attribute => {
        list.forEach(e => {
          this.$utils.roundScore(e, attribute.id)
        })
      })

    },

    addTrialsNumber: async function (list, method) {
      if (this.disorderIds == null || this.disorderIds.length === 0) {
        return
      }
      let drugNames = await this.$http.getNodes("drug", list.map(drug => drug.id), ["id", "displayName"]).then(data => {
        return data.map(d => d.displayName)
      })
      let disorderNames = await this.$http.getNodes("disorder", this.disorderIds, ["id", "displayName", "synonyms"]).then(data => {
        return data.map(d => d.displayName)
      })
      await this.$http.getAllTrials(disorderNames, drugNames).then(data => {
        data.StudyFields.forEach(studie => {
          studie.InterventionName.forEach(target => {
            list.forEach(drug => {
              if (target.toLowerCase().indexOf(drug.displayName.toLowerCase()) > -1) {
                if (drug.trials == null)
                  drug.trials = studie.NCTId
                else {
                  studie.NCTId.forEach(id => {
                    if (drug.trials.indexOf(id) === -1)
                      drug.trials.push(id)
                  })
                }
              }
            })
          })
        })
      })
      list.forEach(drug => {
        if (drug.trials != null) {
          drug.trialCount = drug.trials.length;
        }
      })
      let validDrugs = this.$refs.validation.getDrugs();
      if (validDrugs != null) {
        let ids = validDrugs.map(d => d.id)
        this.results.drugs.filter(d => ids.indexOf(d.id) > -1).forEach(d => d.known = true)
      }
      if (method.scores.filter(s => s.id === "trialCount").length === 0)
        method.scores.push({id: "trialCount", name: "Use"})
    },

    rank: function (list, attribute) {
      if (attribute == null || (list.length > 0 && list[0].rank != null))
        return list;
      let lastRank = 0;
      let lastScore = 0;

      list.forEach(drug => {
        if (lastRank === 0 || lastScore !== drug[attribute.id]) {
          lastRank++
          lastScore = drug[attribute];
        }
        drug.rank = lastRank
      })
    },

    normalize: function (list, method) {
      method.scores.filter(s => s["normalize"]).forEach(attribute => {
        if (attribute.order === "descending") {
          let base = list.map(e => e[attribute.id]).reduce((e1, e2) => {
            return Math.max(e1, e2)
          })
          list.forEach(e => e[attribute.id] = (e[attribute.id] / base))
        } else if (attribute.order === "ascending") {
          let base = list.map(e => e[attribute.id]).reduce((e1, e2) => {
            return Math.min(e1, e2)
          })
          list.forEach(e => e[attribute.id] = base / e.attribute.id)
        }
        if (!attribute.name.endsWith(" (Norm)"))
          attribute.name = attribute.name + " (Norm)"
      })
    }
    ,
    addToSuggestions: function (item) {
      this.selectedSuggestions.push(item.value)
    },
    graphNamePopup: async function () {
      let sources = ""
      if (this.selectedSuggestions.length > 0) {
        this.selectedSuggestions.forEach(s => sources += s + "; ")
        sources = sources.substr(0, sources.length - 2)
      }
      this.nameOptions=[]
      this.nameOptions.push(sources)
      this.nameOptions.push((sources + " (" + this.$refs.moduleAlgorithms.getAlgorithmMethod() + "/" + this.$refs.rankingAlgorithms.getAlgorithmMethod() + ")"))
      this.nameOptions.push((sources + " (" + this.$refs.moduleAlgorithms.getAlgorithmMethod() + ") [" + (await this.$refs.moduleAlgorithms.getParamString()) + "]" + " / " + "(" + this.$refs.rankingAlgorithms.getAlgorithmMethod() + ") [" + (await this.$refs.rankingAlgorithms.getParamString()) + "]"))
      this.namePopup = true

    },
    resolveNamingDialog: function (value) {
      this.namePopup = false
      if (value == null || value.length === 0)
        return
      this.setName(value)
    },
    setName: function (name) {
      if (this.rankingGid == null)
        setTimeout(() => {
          this.setName(name)
        }, 500)
      else {
        this.$http.post("setGraphName", {gid: this.moduleGid, name: name}).catch(console.error)
        this.$http.post("setGraphName", {gid: this.rankingGid, name: name}).catch(console.error)
      }
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
      if (this.namePopup) {
        setTimeout(() => {
          this.loadGraph(graphId,disableSkipToAdvanced)
        }, 500)
      } else if(this.rankingGid==null || graphId === this.rankingGid) {
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
      }
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
      scores.forEach(e => {
        let entry = {
          text: e.name,
          align: e.decimal ? "start" : "end",
          sortable: true,
          value: e.id,
        }
        if (e.id === "rank") {
          headers = [entry].concat(headers)
        } else
          headers.push(entry)
      })
      headers.push({text: "", value: "data-table-expand", width: "1rem"})
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
    LabeledSwitch,
    Network,
    SuggestionAutocomplete,
    ValidationDrugTable,
    NodeInput,
    SeedTable,
    ResultDownload,
    ExampleSeeds,
    ValidationBox,
    EntryDetails,
    MIAlgorithmSelect,
    DPAlgorithmSelect,
    Validation
  }
}
</script>

<style lang="sass">

th
  z-index: 5 !important

.td-name
  max-width: 4vw

.td-score
  max-width: 4vw

.td-rank
  max-width: 3vw !important

.td-result
  font-size: smaller !important

</style>
