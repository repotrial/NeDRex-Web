<template>
  <v-card style="margin-bottom: 25px">
    <v-dialog
      v-model="verify"
      persistent
      style="z-index: 1001"
      max-width="500"
    >
      <v-card>
        <v-card-title>Confirm Navigation</v-card-title>
        <v-card-text>Do you really ant to reset the current page?
        </v-card-text>
        <v-divider></v-divider>

        <v-card-actions style="display: flex; justify-content: flex-end">
          <v-btn style="margin-left: 5px; margin-right: 5px;" color="primary"
                 @click="verify = false">
            <v-icon left>fas fa-angle-left</v-icon>
            <v-divider vertical style="border-color: white; margin-right: 5px;"></v-divider>
            Stay
          </v-btn>
          <v-btn style="margin-left: 5px; margin-right: 5px;" color="error"
                 @click="verify = false; makeStep('cancel')">
            <v-icon left>fas fa-times</v-icon>
            <v-divider vertical style="border-color: white; margin-right: 5px;"></v-divider>
            Cancel
          </v-btn>
        </v-card-actions>
      </v-card>

    </v-dialog>
    <div style="display: flex; justify-content: flex-end; margin-left: auto; ">
      <v-tooltip left>
        <template v-slot:activator="{on, attrs}">
          <v-btn icon style="padding:1em" color="red darker" @click="verify=true" v-on="on" v-bind="attrs">
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
          <small
            v-if="(step>1 ||blitz) && $refs.moduleAlgorithms!=null && $refs.moduleAlgorithms.getAlgorithm() !=null">{{
              $refs.moduleAlgorithms.getAlgorithm().label
            }}</small>
        </v-stepper-step>
        <v-divider></v-divider>
        <v-stepper-step step="3" :complete="step>3 || blitz">
          Ranking Method
          <small
            v-if="rankingAlgorithmSelected &&(step>2 || blitz) && $refs.rankingAlgorithms!=null && $refs.rankingAlgorithms.getAlgorithm() !=null">{{
              this.$refs.rankingAlgorithms.getAlgorithm().label
            }}</small>
        </v-stepper-step>
        <v-divider></v-divider>
        <v-stepper-step step="4" :complete="step>4">
          Results
          <small>{{
              ((results.targets != null && results.targets.length > 0) ? ("Module (" + results.targets.length + ")") : "")
            }}<br>
            {{
              ((results.drugs != null && results.drugs.length > 0) ? ("Candidates (" + results.drugs.length + ")") : "")
            }}</small>
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
            min-height="85vh"
          >

            <v-card-subtitle class="headline" style="color: black; text-align: left; margin-left: 5vw">1. Seed
              Configuration
            </v-card-subtitle>
            <v-card-subtitle style="margin-top: -25px">
              <ul>
                <li v-if="blitz" style="margin-left: 0;">
                  <span> In Quick Module Identification a
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
                  <b v-if="$refs.moduleAlgorithms!=null && $refs.moduleAlgorithms.getAlgorithm() !=null">{{
                      $refs.moduleAlgorithms.getAlgorithm().label
                    }}</b>
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
                </li>
                <li style="margin-left: 0;">Add seeds genes or proteins to the list. You can either:</li>
                <li style="margin-top: 8px">
                  <v-icon style="font-size: 8px" left>fas fa-circle</v-icon>
                  Select genes or proteins directly
                </li>
                <li>
                  <v-icon style="font-size: 8px" left>fas fa-circle</v-icon>
                  Select associated genes based on diseases or drugs
                </li>
                <li v-if="!blitz">
                  <v-icon style="font-size: 8px" left>fas fa-circle</v-icon>
                  Extract them from expression data (here:
                  {{ blitz ? "." : " or use an expression data based algorithm (" }}<a
                  @click="seedTypeId=0; moduleMethodModel=1; makeStep('continue'); setBicon()">BiCoN
                  <v-icon right size="1em" style="margin-left: 0">fas fa-caret-right</v-icon>
                </a>)
                </li>
              </ul>
            </v-card-subtitle>
            <v-divider style="margin: 15px;"></v-divider>
            <v-row>
              <v-col>
                <v-card-title style="text-align: left"><i v-show="seedTypeId==null" style="color: red">*</i><b>Select
                  the seed type:
                </b>
                  <v-radio-group row v-model="seedTypeId" style="display: inline-block; margin-left: 32px"
                                 :disabled="(seedTypeId != null && $refs.seedTable != null && $refs.seedTable.getSeeds() != null && $refs.seedTable.getSeeds().length > 0)">
                    <v-radio label="Gene" :value="0"></v-radio>
                    <v-radio label="Protein" :value="1"></v-radio>
                  </v-radio-group>
                </v-card-title>
              </v-col>
            </v-row>
            <div v-if="seedTypeId!=null">
              <v-card-title><i v-show="$refs.seedTable ==null || $refs.seedTable.getSeeds().length===0"
                               style="color: red">*</i><b>Select seeds:</b>
              </v-card-title>

            </div>

            <v-container style="height: 560px;margin: 15px;max-width: 100%">
              <v-row style="height: 100%">
                <v-col cols="5">
                  <div style="height: 40vh; max-height: 40vh;">
                    <template v-if="seedTypeId!==undefined">
                      <v-card-title style="margin-left: 20px; color: rgb(128,128,128)">Option 1: From example
                        <QuickExamples v-if="$refs.validation" :seedType="['gene','protein'][seedTypeId]"
                                       style="display: inline-block; margin-left: 16px"
                                       @drugsEvent="$refs.validation.addDrugs" @exampleEvent="applyExample"
                                       @disorderEvent="saveDisorders" @suggestionEvent="addToSuggestions"
                                       @addNodesEvent="addToSelection"></QuickExamples>
                      </v-card-title>
                      <div style="display: flex">
                        <div style="justify-content: flex-start">
                          <v-card-title style="text-align: left; margin-left: 20px;  color: rgb(128,128,128)"
                                        class="title"> Option 2: Add
                            {{ ['genes', 'proteins'][this.seedTypeId] }} by association to <i style="margin-left: 8px">{{ suggestionType }}</i>
                          </v-card-title>
                        </div>
                        <div style="justify-content: flex-end; margin-left: auto">
                          <v-radio-group row v-model="advancedOptions" style="display: inline-block; margin-left: 32px"
                                         :disabled="(seedTypeId != null && $refs.seedTable != null && $refs.seedTable.getSeeds() != null && $refs.seedTable.getSeeds().length > 0)">
                            <v-tooltip left>
                              <template v-slot:activator="{on,attrs}">
                                <v-radio label="Limited" @click="suggestionType = 'disorder'" :value="false"
                                         v-bind="attrs"
                                         v-on="on"></v-radio>
                              </template>
                              <div style="width: 300px"><b>Limited Mode:</b><br>The options are limited to the most
                                interesting and generally used ones to not overcomplicate the user interface
                              </div>
                            </v-tooltip>
                            <v-tooltip left>
                              <template v-slot:activator="{on,attrs}">
                                <v-radio label="Full" :value="true" v-bind="attrs"
                                         v-on="on"></v-radio>
                              </template>
                              <div style="width: 300px"><b>Full Mode:</b><br> The full mode provides a wider list of
                                options to select from
                                for
                                more
                                specific queries.
                              </div>
                            </v-tooltip>
                          </v-radio-group>
                        </div>
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
                        <SuggestionAutocomplete ref="suggestions" :suggestion-type="suggestionType" :emit-drugs="true"
                                                :emit-disorders="true" :disorder-select="true"
                                                @drugsEvent="$refs.validation.addDrugs"
                                                @disorderEvent="saveDisorders" @subtypeSelection="subtypePopup"
                                                :target-node-type="['gene', 'protein'][seedTypeId]"
                                                @addToSelectionEvent="addToSelection" :add-all="true"
                                                @suggestionEvent="addToSuggestions"
                                                style="justify-self: flex-end;margin-left: auto"></SuggestionAutocomplete>
                      </div>
                      <div style="display: flex; justify-content: flex-start; margin-top: 32px; margin-left: 20px">
                        <NodeInput :text="'Option 3: Provide '+['gene','protein'][seedTypeId]+'s IDs by'"
                                   @addToSelectionEvent="addToSelection"
                                   :idName="['entrez','uniprot'][seedTypeId]"
                                   :nodeType="['gene', 'protein'][seedTypeId]"
                                   @printNotificationEvent="printNotification"></NodeInput>
                      </div>
                    </template>
                  </div>
                </v-col>

                <v-divider vertical v-show="seedTypeId!==undefined"></v-divider>
                <v-col cols="6" style="padding-top:0">
                  <v-tooltip left>
                    <template v-slot:activator="{attrs,on}">
                      <v-chip style="position: absolute; left:auto; right:0" v-on="on" v-bind="attrs"
                              v-show="seedTypeId!=null"
                              :disabled="$refs.seedTable==null || $refs.seedTable.getSeeds().length===0"
                              color="primary" @click="showInteractionNetwork()">
                        <v-icon>fas fa-project-diagram</v-icon>
                      </v-chip>
                    </template>
                    <span>Display an interaction network with all your current seeds</span>
                  </v-tooltip>
                  <v-tooltip left>
                    <template v-slot:activator="{attrs,on}">
                      <v-chip @click="$refs.drugsDialog.show()" style="position: absolute; left:auto; right:55px"
                              v-on="on" v-bind="attrs"
                              v-show="seedTypeId!=null"
                              color="primary">
                        <v-icon left>fas fa-capsules</v-icon>
                        {{ validationDrugCount }}
                      </v-chip>
                    </template>
                    <span>There are {{ validationDrugCount }} drugs that were associated with your query.<br> These are saved for validation purposes later.<br><i>Click here to view the current list!</i></span>
                  </v-tooltip>
                  <SeedTable ref="seedTable" v-show="seedTypeId!=null" :download="true"
                             :remove="true"
                             :filter="true" @clearEvent="clearData"
                             @printNotificationEvent="printNotification"
                             height="405px"
                             :title="'Selected Seeds ('+($refs.seedTable ? $refs.seedTable.getSeeds().length : 0)+')'"
                             :nodeName="['gene','protein'][seedTypeId]"
                  ></SeedTable>
                </v-col>
              </v-row>
            </v-container>


          </v-card>
          <v-card-actions style="display: flex; justify-content: flex-end">
            <ButtonCancel @click="makeStep"></ButtonCancel>
            <ButtonNext @click="makeStep"
                        :disabled="seedTypeId<0 || $refs.seedTable == null || $refs.seedTable.getSeeds().length===0"></ButtonNext>
          </v-card-actions>
        </v-stepper-content>

        <v-stepper-content step="2">
          <MIAlgorithmSelect ref="moduleAlgorithms" :blitz="blitz" :seeds="seeds" :seed-type-id="seedTypeId"
                             socket-event="quickRepurposeModuleFinishedEvent" goal="module_identification"
                             @algorithmSelectedEvent="acceptModuleAlgorithmSelectEvent"
                             @jobEvent="readModuleJob" @clearSeedsEvent="seeds = []"></MIAlgorithmSelect>
          <v-card-actions style="display: flex; justify-content: flex-end">
            <ButtonCancel @click="makeStep"></ButtonCancel>
            <ButtonBack @click="makeStep"></ButtonBack>
            <ButtonNext @click="makeStep"
                        :disabled=" !moduleAlgorithmSelected  || ($refs.moduleAlgorithms.getAlgorithmMethod()==='bicon' && $refs.moduleAlgorithms.getAlgorithmModels().exprFile ===undefined)"></ButtonNext>
          </v-card-actions>
        </v-stepper-content>

        <v-stepper-content step="3">
          <DPAlgorithmSelect ref="rankingAlgorithms" :blitz="blitz" :step="3" :seeds="seeds" :seed-type-id="seedTypeId"
                             socket-event="quickRepurposeRankingFinishedEvent" goal="drug_repurposing"
                             @algorithmSelectedEvent="acceptRankingAlgorithmSelectEvent"
                             @jobEvent="readRankingJob"></DPAlgorithmSelect>
          <ButtonCancel @click="makeStep"></ButtonCancel>
          <ButtonBack @click="makeStep"></ButtonBack>
          <ButtonNext @click="makeStep" label="RESULTS" :disabled="!rankingAlgorithmSelected"></ButtonNext>

        </v-stepper-content>

        <v-stepper-content step="4">
          <v-card
            v-if="step===4"
            class="mb-4"
            min-height="90vh"
            max-height="775px"
          >
            <v-card-subtitle class="headline" style="color: black; text-align: left; margin-left: 5vw">4. Drug
              Repurposing Results
            </v-card-subtitle>
            <v-card-subtitle style="margin-top: -25px">
              <ul>
                <li style="margin-left: 0;">Explore the results of Module Identification and Drug Ranking:</li>
                <li style="margin-top: 8px">
                  <v-icon style="font-size: 8px" left>fas fa-circle</v-icon>
                  Explore the network
                </li>
                <li>
                  <v-icon style="font-size: 8px" left>fas fa-circle</v-icon>
                  Click on nodes in a list to highlight it in the network
                </li>
                <li>
                  <v-icon style="font-size: 8px" left>fas fa-circle</v-icon>
                  Double click a nodes in a list to get details
                </li>
              </ul>
            </v-card-subtitle>
            <v-divider style="margin: 15px;"></v-divider>
            <v-container style="max-width: 100%">
              <v-row>
                <v-col cols="4" style="padding: 0 50px 0 0; margin-right: -50px; min-width: 375px">
                  <v-card-title class="subtitle-1" style="display: flex">
                    <span style="justify-content: flex-start">Seeds ({{ seeds.length }}) {{
                        (results.targets.length !== undefined && results.targets.length > 0 ? ("& Module (" + getTargetCount() + ") " + ["Genes", "Proteins"][seedTypeId]) : (": " + (state != null ? ("[" + state + "]") : "Processing")))
                      }}
                    </span>
                    <v-progress-circular indeterminate size="25" v-if="this.results.targets.length===0"
                                         style="margin-left:15px; z-index:50">
                    </v-progress-circular>
                    <ResultDownload v-else v-show="seeds && seeds.length>0" raw results seeds
                                    style="margin: auto; justify-self: flex-end; display: inline-block"
                                    @downloadEvent="downloadList" @downloadResultsEvent="downloadResultList"
                                    @downloadRawEvent="downloadFullResultList"></ResultDownload>
                  </v-card-title>
                  <v-data-table
                    v-if="$refs.moduleAlgorithms && $refs.moduleAlgorithms && $refs.moduleAlgorithms.getAlgorithm()"
                    max-height="50vh"
                    height="55vh" class="overflow-y-auto" fixed-header dense item-key="id"
                    :items="(!results.targets ||results.targets.length ===0) ?seeds : results.targets"
                    :headers="getHeaders(0)"
                    disable-pagination @dblclick:row="seedDoubleClicked"
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
                      <v-icon color="primary" @click="seedDoubleClicked(null,{item:item})">
                        fas fa-info-circle
                      </v-icon>
                    </template>
                    <template v-slot:item.seed="{item}" v-if="seeds || (reload && reloaded)">
                      <v-icon v-if="item.isSeed == null || item.isSeed " color="success">fas fa-check</v-icon>
                      <v-icon v-else color="error">fas fa-times</v-icon>
                    </template>
                  </v-data-table>
                </v-col>
                <v-col>

                  <div style="display: flex; justify-content: center">
                    <v-tooltip top>
                      <template v-slot:activator="{attrs, on}">
                        <v-btn
                          :disabled="!rankingGid"
                          outlined
                          small
                          v-on="on"
                          v-bind="attrs"
                          style="margin:8px"
                          @click="requestGraphDownload"
                        >
                          <v-icon
                            left
                            small
                            color="primary"
                          >
                            far fa-arrow-alt-circle-down
                          </v-icon>
                          <v-divider vertical style="border-color: black; margin-right: 5px;"></v-divider>
                          Download
                        </v-btn>
                      </template>
                      <div style="width: 250px">Download a .graphml file containing the current network with all
                        available
                        attributes.
                      </div>
                    </v-tooltip>
                    <v-tooltip top>
                      <template v-slot:activator="{attrs, on}">
                        <v-btn
                          :disabled="!rankingJid"
                          v-on="on"
                          v-bind="attrs"
                          outlined
                          small
                          style="margin:8px"
                          @click="copyLink(); printNotification('Copied graph link to clipboard!',1)"
                        >
                          <v-icon
                            left
                            small
                            color="primary"
                          >
                            far fa-copy
                          </v-icon>
                          <v-divider vertical style="border-color: black; margin-right: 5px;"></v-divider>
                          Copy URL
                        </v-btn>
                      </template>
                      <div style="width: 250px">
                        Copies the unique link of this network to your clipboard to save it to some document or to share
                        it with
                        others.
                      </div>
                    </v-tooltip>
                  </div>
                  <i v-if="!this.rankingGid">The execution could take a moment. Save the current URL and return at any
                    time!</i>
                  <Network ref="graph" :configuration="graphConfig" :window-style="graphWindowStyle"
                           :progress="resultProgress" :progress-interminate="reloaded && !rankingGid"
                           :legend="resultProgress>=50"
                           :tools="resultProgress===100"
                           :secondaryViewer="true" :show-vis-option="showVisOption"  @toggleNodeSelectEvent="nodeDoubleclicked"
                           @loadIntoAdvancedEvent="$emit('graphLoadEvent',{post: {id: rankingGid}})">
                    <template v-slot:legend v-if="results.targets.length>0">
                      <v-card style="width: 13vw; max-width: 18vw; padding-top: 35px">
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
                              {{ getTargetCount() }}
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
                    <template v-slot:tools>
                      <Tools :physics="true" ref="tools" :cc="false" :loops="false" v-if="results.drugs.length>0"
                             @toggleOptionEvent="toggleToolOption" @clickOptionEvent="clickToolOption">
                        <template v-slot:append>
                          <ToolDropdown
                            :items="[{value:'default', text:'Default'}, {value:'portrait', text:'Portrait'}, {value:'topographic_x', text:'Topographic (X,Z)'}, {value:'topographic_y', text:'Topographic (Y,Z)'}, {value: 'geodesic', text: 'Geodesic (X,Y)'},{value: 'geodesic_x', text: 'Geodesic (X,Z)'},{value: 'geodesic_y', text: 'Geodesic (Y,Z)'}]"
                            label="Layout" icon="fas fa-project-diagram" @change="$refs.graph.loadLayout">
                            <template v-slot:tooltip>
                              <div style="display: inline-block"><i><b>Change the layout of the network!</b></i> <br>Options:<br>
                                <b>Default:</b> force-directed layout<br>
                                <b>Portrait:</b> cartoGRAPHs.Portrait layout<br>
                                <b>Topographic (X,Z):</b> cartoGRAPHs Topographic 3D layout reduced to X and Z coordinates <br>
                                <b>Topographic (Y,Z):</b> cartoGRAPHs Topographic 3D layout reduced to Y and Z coordinates <br>
                                <b>Geodesic (X,Y):</b> cartoGRAPHs Geodesic 3D layout reduced to X and Y coordinates <br>
                                <b>Geodesic (X,Z):</b> cartoGRAPHs Geodesic 3D layout reduced to X and Z coordinates <br>
                                <b>Geodesic (Y,Z):</b> cartoGRAPHs Geodesic 3D layout reduced to Y and Z coordinates <br>
                              </div>
                            </template>
                          </ToolDropdown>
                        </template>
                      </Tools>
                    </template>
                  </Network>

                </v-col>
                <v-col style="padding:0; max-width: 30%; width: 31%; min-width: 400px">
                  <v-card-title class="subtitle-1" style="display: flex">
                    <span style="justify-content: flex-start">Drugs{{
                        (results.drugs.length !== undefined && (results.drugs.length > 0 || rankingGid != null) ? (" (" + (results.drugs.length) + ")") : (": " + (rankingState != null ? ("[" + rankingState + "]") : "Processing")))
                      }}</span>
                    <span v-show="loadingTrialData">: Loading Trial
                      Data</span>
                    <v-progress-circular indeterminate
                                         size="25"
                                         v-if="(results.drugs.length === 0 && rankingGid ==null) || loadingTrialData"
                                         style="margin-left:15px; z-index:50">
                    </v-progress-circular>
                    <ResultDownload v-if="results.drugs.length>0" raw results
                                    result-label="Drug Ranking"
                                    style="margin: auto; justify-self: flex-end; display: inline-block"
                                    @downloadResultsEvent="downloadRankingResultList"
                                    @downloadRawEvent="downloadFullResultList(rankingJid)"></ResultDownload>
                  </v-card-title>
                  <template v-if="results.drugs.length>=0">
                    <v-data-table v-if="$refs.rankingAlgorithms" max-height="55vh" height="55vh" class="overflow-y-auto"
                                  fixed-header dense
                                  item-key="id" @dblclick:row="drugDoubleClicked"
                                  :items="results.drugs"
                                  :headers="getHeaders(1)"
                                  disable-pagination
                                  hide-default-footer @click:row="drugClicked">
                      <template v-slot:item.displayName="{item}">
                        <v-tooltip v-if="item.displayName.length>24" right>
                          <template v-slot:activator="{attr,on }">
                          <span v-bind="attr" v-on="on"
                                style="color: dimgray">{{ item.displayName.substring(0, 21) }}...</span>
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
                      <template v-slot:item.data-table-expand="{expand, item,isExpanded}">
                        <v-icon color="primary" @click="drugDoubleClicked(null,{item:item})">
                          fas fa-info-circle
                        </v-icon>
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
                            <span v-bind="attr" v-on="on"><v-icon size="12pt">fas fa-clinic-medical</v-icon></span>
                          </template>
                          <span>There is at least one entry for trials regarding one of the<br> initially selected disorders and this drug on e.g. ClinicalTrial.gov ({{
                              item.trialCount
                            }}). Expand the entry to find some linked studies!</span>
                        </v-tooltip>

                      </template>
                    </v-data-table>
                  </template>
                </v-col>
              </v-row>
            </v-container>
          </v-card>
          <v-card-actions style="justify-content: flex-end; display: flex">
            <ButtonCancel @click="makeStep"></ButtonCancel>
            <ButtonBack @click="makeStep" v-if="!reloaded"></ButtonBack>
            <ButtonNext @click="makeStep" label="VALIDATE" :disabled="rankingGid ==null"></ButtonNext>
            <ButtonAdvanced @click="$emit('graphLoadNewTabEvent',{post: {id: rankingGid}})"
                            :disabled="rankingGid==null">
              Advanced
            </ButtonAdvanced>
          </v-card-actions>
        </v-stepper-content>
        <v-stepper-content step="5">
          <Validation ref="validation" :step="5" :seed-type-id="seedTypeId" :module="results.targets"
                      :ranking="results.drugs" @drugCountUpdate="updateDrugCount"
                      @printNotificationEvent="printNotification"></Validation>
          <ButtonCancel @click="makeStep"></ButtonCancel>
          <ButtonBack @click="makeStep" label="RESULTS" save></ButtonBack>
        </v-stepper-content>
      </v-stepper-items>


    </v-stepper>
    <DetailDialog ref="details" max-width="25vw"
                  :attributes="detailAttributes"
                  :detail-request="detailRequest"
                  :additions="detailAdditions"
    ></DetailDialog>
    <DrugsDialog v-if="$refs.validation" ref="drugsDialog" :drugs="$refs.validation.getDrugs()"></DrugsDialog>
    <v-dialog v-model="namePopup"
              persistent
              max-width="500px"
              style="z-index: 1001"
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
          <v-btn outlined @click="graphName=option" style="font-size: 8pt" color="primary">
            <v-icon left>fas fa-angle-double-right</v-icon>
            Load
          </v-btn>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions style="justify-content: flex-end; display: flex; margin-right: 16px">
          <v-btn
            @click="resolveNamingDialog()"
            color="error"
          >
            <v-icon left>fas fa-times</v-icon>
            <v-divider vertical style="border-color: white; margin-right: 5px;"></v-divider>
            Cancel
          </v-btn>
          <v-btn
            color="success"
            @click="resolveNamingDialog(graphName)"
            :disabled="graphName==null || graphName.length ===0"
          >
            <v-icon left>fas fa-check</v-icon>
            <v-divider vertical style="border-color: white; margin-right: 5px;"></v-divider>
            Accept
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <v-dialog
      v-model="error"
      max-width="300"
      style="z-index: 1001"
    >
      <v-card>
        <v-card-title>Error</v-card-title>
        <v-card-text>
          <div>
            Unfortunately there was an error during the execution of your job. That can sometimes be the case when
            choosing compatible parameters. <br>
            So you might either reach out to us or retry with slightly adjusted parameters.
          </div>
        </v-card-text>
        <v-card-actions>
          <v-btn
            text
            @click="error=false"
          >
            Close
          </v-btn>
        </v-card-actions>
      </v-card>


    </v-dialog>
    <DisorderHierarchyDialog v-if="$refs.suggestions!=null" ref="disorderHierarchy"
                             @addDisorders="$refs.suggestions.loadDisorders"></DisorderHierarchyDialog>
    <InteractionNetworkDialog ref="interactionDialog"></InteractionNetworkDialog>
  </v-card>
</template>

<script>
import Network from "../../graph/Network";
import * as CONFIG from "../../../../Config"
import SuggestionAutocomplete from "@/components/app/suggestions/SuggestionAutocomplete";
import SeedTable from "@/components/app/tables/SeedTable";
import ResultDownload from "@/components/app/tables/menus/ResultDownload";
import NodeInput from "@/components/app/input/NodeInput";
import ValidationBox from "@/components/start/quick/ValidationBox";
import ValidationDrugTable from "@/components/app/tables/ValidationDrugTable";
import EntryDetails from "@/components/app/EntryDetails";
import LabeledSwitch from "@/components/app/input/LabeledSwitch";
import MIAlgorithmSelect from "@/components/start/quick/MIAlgorithmSelect";
import DPAlgorithmSelect from "@/components/start/quick/DPAlgorithmSelect";
import Validation from "@/components/start/quick/Validation";
import DisorderHierarchyDialog from "@/components/start/quick/DisorderHierarchyDialog";
import Tools from "@/components/views/graph/Tools";
import ButtonCancel from "@/components/start/quick/ButtonCancel";
import ButtonNext from "@/components/start/quick/ButtonNext";
import ButtonBack from "@/components/start/quick/ButtonBack";
import ButtonAdvanced from "@/components/start/quick/ButtonAdvanced";
import QuickExamples from "@/components/start/quick/QuickExamples";
import InteractionNetworkDialog from "@/components/start/quick/InteractionNetworkDialog";
import DetailDialog from "@/components/start/quick/DetailDialog";
import DrugsDialog from "@/components/start/quick/DrugsDialog";

export default {
  name: "CombinedRepurposing",
  props: {
    blitz: Boolean,
    reload: {
      default: undefined,
      type: Object,
    }
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
      verify: false,
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
      rankingState: undefined,
      moduleState: undefined,
      rankingGid: undefined,
      rankingJid: undefined,
      validationDrugCount: 0,
      drugDetailAttributes: ["Name", "SourceIDs", "Formula", "Indication", "Description", "Synonyms"],
      geneDetailAttributes: ["Name", "SourceIDs", "Symbols", "Chromosome", "Genomic Location", "Synonyms", "Description"],
      proteinDetailAttributes: ["Name", "SourceIDs", "Gene", "Synonyms", "Comments"],
      disorderIds: [],

      selectedSuggestions: [],
      namePopup: false,
      nameOptions: [],
      reloaded: false,
      graphName: "",
      showVisOption: false,
      loadingTrialData: false,
      error: false,
      detailRequest: undefined,
      detailAttributes: undefined,
      detailAdditions: undefined,
    }
  },

  created() {
    this.$socket.$on("quickRepurposeModuleFinishedEvent", this.convertModuleJob)
    this.$socket.$on("quickRepurposeRankingFinishedEvent", this.convertRankingJob)
    this.uid = this.$cookies.get("uid")
    this.init()
    if (this.reload)
      this.reloadJob(this.reload);
  },
  destroyed() {
    //TODO maybe add destroyed function to be save that all data is removed
  },

  methods: {

    init: function (keepSeedId) {
      this.method = undefined;
      this.sourceType = undefined
      this.step = 1
      if (!keepSeedId)
        this.seedTypeId = undefined
      if (this.$refs.seedTable)
        this.$refs.seedTable.clear()
      else this.clearData()
      this.seeds = []
      this.results.targets = []
      this.results.drugs = []
      this.seedOrigin = {}
      this.reloaded = false
      if (this.$refs.graph)
        this.$refs.graph.reload()
      this.moduleJid = undefined
      this.moduleGid = undefined
      this.rankingJid = undefined
      this.rankingGid = undefined
      this.graphName = undefined
      this.nameOptions = []
      this.showVisOption = false
      this.resultProgress = 0
      this.disorderIds = []
      this.validationDrugCount = 0
    },
    reset: function (keepSeedId) {
      this.init(keepSeedId)
    },

    clearData: function () {
      this.selectedSuggestions = []
      this.disorderIds = []
      if (this.$refs.validation)
        this.$refs.validation.clear()
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
    subtypePopup: function (item) {
      this.$refs.disorderHierarchy.loadDisorder(item.sid)
    },
    setURL: function (jid) {
      let route = location.pathname + "?job=" + jid
      if (location.origin + route !== location.href) {
        this.$router.push(route)
      }
    },
    makeStep: function (button) {
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
          if (this.rankingGid == null || this.rankingGid === this.graphName)
            this.graphNamePopup()
          if (this.moduleGid != null && this.resultProgress === 50)
            this.loadGraph(this.moduleGid, false, true)
          this.submitRankingAlgorithm()
        }
        this.focus()

      }
      if (button === "back") {
        this.step--
        if (this.step === 4) {
          this.resultProgress = 100
          this.loadGraph(this.rankingGid, false, false, true)
        }

        if (this.step === 3) {
          this.results.drugs = []
          this.rankingGid = undefined
          this.graphName = undefined
          this.$refs.graph.reload()
          this.$refs.validation.resetValidation();
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
      if (this.moduleJid == null)
        setTimeout(this.waitForModuleJob, 100, resolve)
      else
        resolve()
    },
    showInteractionNetwork: function () {
      this.$refs.interactionDialog.show(["gene", "protein"][this.seedTypeId], this.$refs.seedTable.getSeeds().map(n => n.id))
    },
    getColor: function (item) {
      if (item.isSeed)
        return "#e4ca02"
      return this.getColoring('nodes', ['gene', 'protein'][this.seedTypeId])
    },
    toggleToolOption: function (option, value) {
      if (option === "physics")
        this.$refs.graph.setPhysics(value);
      if (option === "loops")
        this.$refs.graph.showLoops(value)
      if (option === "unconnected")
        this.$refs.graph.showUnconnected(value)
      if (option === "isolation")
        this.$refs.graph.graphViewEvent(value)
      if (option === 'shadow')
        this.$refs.graph.setShadow(value)
    },

    clickToolOption: function (option) {
      if (option === "fit")
        this.$refs.graph.setSelection()
    },
    submitRankingAlgorithm: function () {
      let ctx = this
      new Promise(resolve => this.waitForModuleJob(resolve)).then(() => {
        if (ctx.moduleGid != null) {
          ctx.$refs.rankingAlgorithms.run(ctx.moduleGid)
        } else
          ctx.$refs.rankingAlgorithms.runLater(ctx.moduleJid)
      })
    },

    readModuleJob: function (data, notSubbed) {
      this.moduleState = data.state
      if (data.state === "ERROR") {
        this.error = true;
        return
      }
      this.resultProgress += 5
      let jid = data.jid
      this.moduleJid = jid
      this.moduleGid = data.gid
      if (this.moduleGid != null && data.state === "DONE") {
        if (!notSubbed)
          this.$socket.unsubscribeJob(jid)
        this.loadModuleTargetTable(this.moduleGid).then(() => {
          this.$emit("newGraphEvent")
          this.resultProgress = 25
          this.loadGraph(this.moduleGid, false, true)
        })
      }

    },
    saveDisorders: function (list) {
      this.disorderIds = this.disorderIds.concat(list.filter(id => this.disorderIds.indexOf(id) === -1))
    },

    setBicon: function () {
      this.$refs.moduleAlgorithms.setExpMethod("bicon")
    },
    updateDrugCount: function () {
      this.validationDrugCount = this.$refs.validation.getDrugs().length;
    },
    reloadJobs: async function (module, ranking) {
      try {
        this.moduleState = module.state
        this.rankingState = ranking.state
        await this.$refs.moduleAlgorithms.setMethod(module.method)
        if (this.$refs.moduleAlgorithms.getGroup() !== "exp" && (!module.seeds || module.seeds.length === 0))
          this.$emit("jobReloadError")
        if (module.seeds && module.seeds.length > 0)
          this.$http.getNodes(module.target, module.seeds, ["id", "displayName"]).then(response => {
            if (response && response.length > 0)
              this.seeds = response
          })
        this.moduleGid = module.derivedGraph
        this.moduleJid = module.jobId;
        this.rankingJid = ranking.jobId;
        if (module.derivedGraph && module.state === "DONE") {
          this.loadModuleTargetTable(this.moduleGid).then(() => {
            this.loadGraph(this.moduleGid, false)
          })
        } else {
          this.$socket.subscribeJob(this.moduleJid, "quickRepurposeModuleFinishedEvent");
        }
        await this.$refs.rankingAlgorithms.setMethod(ranking.method)
        if (ranking.derivedGraph && ranking.state === "DONE") {
          this.loadRankingTargetTable(ranking.derivedGraph).then(() => {
            this.loadGraph(this.rankingGid, false)
          })
        } else {
          this.$socket.subscribeJob(this.rankingJid, "quickRepurposeRankingFinishedEvent");
        }
      } catch (e) {
        console.error(e)
        this.$emit("jobReloadError")
      }
    },

    reloadJob: async function (job) {
      try {
        this.reloaded = true;
        this.resultProgress = 50;
        this.step = 4;
        await setTimeout(() => {
        }, 200)
        this.seedTypeId = ["gene", "protein"].indexOf(job.target)
        await setTimeout(() => {
        }, 1000);
        if (job.basisGraph) {
          this.$http.getJobByGraph(job.basisGraph).then(moduleJob => {
            this.reloadJobs(moduleJob, job)
          })
        } else {
          this.$http.getJob(job.parentJid).then(async moduleJob => {
            this.reloadJobs(moduleJob, job)
          })
        }
      } catch (e) {
        console.error(e)
        this.$emit("jobReloadError")
      }
    },
    readRankingJob: function (result, unsubscribed, dirty) {
      this.resultProgress += 5
      let data = !dirty ? result : JSON.parse(result)
      this.rankingState = data.state
      this.rankingJid = data.jid
      this.setURL(this.rankingJid)
      this.rankingGid = data.gid
      if (this.rankingGid != null && data.state === "DONE") {
        this.resultProgress = 75
        if (!unsubscribed)
          this.$socket.unsubscribeJob(this.rankingJid)
        this.loadRankingTargetTable(this.rankingGid).then(() => {
          this.loadGraph(this.rankingGid, false)
        })

      }
      if (this.rankingGid != null && data.state === "ERROR") {
        this.error = true;
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
          let escape = s.displayName.indexOf(sep) > -1 ? "\"" : "";
          this.seeds.forEach(s => text += data[s.id] + sep + escape + s.displayName + escape + "\n")
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
            let escape = t.displayName.indexOf(sep) > -1 ? "\"" : "";
            text += data[t.id] + (names ? sep + escape + t.displayName + escape : "")
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
            let escape = t.displayName.indexOf(sep) > -1 ? "\"" : "";
            text += data[t.id] + sep + escape + t.displayName + escape
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
      let seeds = list.filter(n => seedIds.indexOf(n.id) > -1)
      seeds.forEach(s => s.isSeed = true)
      list.filter(n => seedIds.indexOf(n.id) === -1).forEach(s => s.isSeed = false)
      method.scores.forEach(score => seeds.filter(n => n[score.id] == null).forEach(n => n[score.id] = score.seed))
    },

    loadModuleTargetTable: function () {
      let seedType = ['gene', 'protein'][this.seedTypeId]
      return this.$http.get("/getGraphList?id=" + this.moduleGid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        if (data.nodes[seedType] != null) {
          data.nodes[seedType].forEach(n => n.displayName = this.$utils.adjustLabels(n.displayName))

          let method = this.$refs.moduleAlgorithms.getAlgorithm()
          let primaryAttribute = method.scores.filter(s => s.primary)[0]
          this.seedValueReplacement(data.nodes[seedType], method)
          this.results.targets = this.sort(data.nodes[seedType], primaryAttribute)
          this.rank(this.results.targets, primaryAttribute)
          this.normalize(this.results.targets, method)
          this.round(this.results.targets, method)
        }
      }).catch(console.error)
    },

    applyExample: function (example) {
      this.reset(true)
      this.example = example
      this.$nextTick(() => {
        this.$refs.moduleAlgorithms.setNWMethod(example.mi.algorithm, example.mi.params)
        this.$refs.rankingAlgorithms.setMethod(example.dp.algorithm, example.dp.params)
      })

    },

    loadRankingTargetTable: function (gid) {
      this.rankingGid = gid
      return this.$http.get("/getGraphList?id=" + this.rankingGid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        if (this.$refs.rankingAlgorithms) {
          let method = this.$refs.rankingAlgorithms.getAlgorithm()
          let primaryAttribute = method.scores.filter(s => s.primary)[0]
          if (data.nodes.drug != null) {
            this.results.drugs = this.sort(data.nodes.drug, primaryAttribute)
            this.rank(this.results.drugs, primaryAttribute)
            this.normalize(this.results.drugs, method)
            this.round(this.results.drugs, method)
            this.addTrialsNumber(this.results.drugs, method)
          }
        }
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
      this.loadingTrialData = true
      let drugNames = await this.$http.getNodes("drug", list.map(drug => drug.id), ["id", "displayName"]).then(data => {
        return data.map(d => d.displayName)
      })
      let disorderNames = await this.$http.getNodes("disorder", this.disorderIds, ["id", "displayName", "synonyms"]).then(data => {
        return data.map(d => d.displayName)
      })
      await this.$http.getAllTrials(disorderNames, drugNames).then(data => {
        if (data.studies)
          data.studies.forEach(study => {
            if (study && study.protocolSection && study.protocolSection.armsInterventionsModule && study.protocolSection.armsInterventionsModule.interventions)
              study.protocolSection.armsInterventionsModule.interventions.forEach(target => {
                list.forEach(drug => {
                  if (target.name.toLowerCase().includes(drug.displayName.toLowerCase())) {
                    if (drug.trials == null) {
                      drug.trials = [study.protocolSection.identificationModule.nctId]
                    } else {
                      let id = study.protocolSection.identificationModule.nctId
                      if (drug.trials.indexOf(id) === -1)
                        drug.trials.push(id)
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
        method.scores.push({id: "trialCount", name: "Use", width: "80px"})
      this.loadingTrialData = false
    },
    requestGraphDownload: function () {
      window.open(CONFIG.HOST_URL + CONFIG.CONTEXT_PATH + '/api/downloadGraph?gid=' + this.rankingGid)
    },
    copyLink: function () {
      const el = document.createElement('textarea');
      el.value = location.host + "/explore/quick/start?job=" + this.rankingJid;
      el.setAttribute('readonly', '');
      el.style.position = 'absolute';
      el.style.left = '-9999px';
      document.body.appendChild(el);
      const selected = document.getSelection().rangeCount > 0 ? document.getSelection().getRangeAt(0) : false;
      el.select();
      document.execCommand('copy');
      document.body.removeChild(el);
      if (selected) {
        document.getSelection().removeAllRanges();
        document.getSelection().addRange(selected);
      }
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
    }

    ,

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
      this.selectedSuggestions.push(item.text)
    }
    ,
    graphNamePopup: async function () {
      let sources = ""
      if (this.selectedSuggestions.length > 0) {
        this.selectedSuggestions.forEach(s => sources += s + "; ")
        sources = sources.substr(0, sources.length - 2)
      }
      this.nameOptions = []
      this.nameOptions.push(sources)
      this.nameOptions.push((sources + " (" + this.$refs.moduleAlgorithms.getAlgorithmMethod() + "/" + this.$refs.rankingAlgorithms.getAlgorithmMethod() + ")"))
      this.nameOptions.push((sources + " (" + this.$refs.moduleAlgorithms.getAlgorithmMethod() + ") [" + (await this.$refs.moduleAlgorithms.getParamString()) + "]" + " / " + "(" + this.$refs.rankingAlgorithms.getAlgorithmMethod() + ") [" + (await this.$refs.rankingAlgorithms.getParamString()) + "]"))
      this.namePopup = true

    }
    ,
    resolveNamingDialog: function (value) {
      this.namePopup = false
      if (value == null || value.length === 0)
        return
      this.setName(value)
    }
    ,
    setName: function (name) {
      if (this.rankingGid == null)
        setTimeout(() => {
          this.setName(name)
        }, 500)
      else {
        this.$http.post("setGraphName", {gid: this.moduleGid, name: name}).then(() => {
          this.$http.post("setGraphName", {gid: this.rankingGid, name: name}).then(() => {
            this.$emit("newGraphEvent")
          })
        }).catch(console.error)
      }
    }
    ,


    waitForGraph: function (resolve) {
      if (this.$refs.graph === undefined)
        setTimeout(() => this.waitForGraph(resolve), 100)
      else
        resolve()
    }
    ,
    getGraph: function () {
      return new Promise(resolve => this.waitForGraph(resolve)).then(() => {
        return this.$refs.graph;
      })
    }
    ,
    loadGraph: async function (graphId, layoutRequested, disableSkipToAdvanced, noProg) {
      if (this.rankingGid && graphId !== this.rankingGid)
        return
      let ready = await this.$http.get("layoutReady?id=" + graphId).then(response => {
        return response.data
      })
      if (!ready) {
        if (!layoutRequested)
          this.$http.getLayout(graphId, 'default')
        setTimeout(() => {
          this.loadGraph(graphId, true, disableSkipToAdvanced, noProg)
        }, 1000)
        return
      }
      if (this.namePopup) {
        setTimeout(() => {
          this.loadGraph(graphId, true, disableSkipToAdvanced, noProg)
        }, 500)
      } else if (this.rankingGid == null || graphId === this.rankingGid) {
        this.getGraph().then(graph => {
          if (!noProg)
            this.resultProgress += 5
          if (this.rankingGid == null || this.results.drugs.length > 0) {
            graph.loadNetworkById(graphId, disableSkipToAdvanced).then(() => {
              if (!noProg)
                this.resultProgress += 15
              graph.showLoops(false)
              let seedIds = this.seeds.map(s => s.id)
              if (!noProg)
                this.resultProgress += 3
              graph.modifyGroups(this.results.targets.filter(n => seedIds.indexOf(n.id) > -1).map(n => ["gen_", "pro_"][this.seedTypeId] + n.id), ["seedGene", "seedProtein"][this.seedTypeId])
              if (!noProg)
                this.resultProgress += 2
              this.showVisOption = !this.graphConfig.visualized
              this.resultProgress = this.rankingGid == null ? 50 : 100
            })
          } else {
            this.resultProgress = 100
          }
        })
      }
    }
    ,
    printNotification: function (message, type) {
      this.$emit("printNotificationEvent", message, type)
    }
    ,
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
      if (table === 0)
        headers.push({text: "Seed", value: "seed", sortable: false, align: "center", width: "1rem"})
      headers.push({text: "", value: "data-table-expand", width: "1rem"})
      return headers
    }
    ,
    seedClicked: function (item) {
      this.focusNode(['gen_', 'pro_'][this.seedTypeId] + item.id)
    }
    ,
    nodeDoubleclicked: function (obj) {
      if (obj[0]) {
        let item = obj[0]
        this.rowDoubleClicked(item, item.group)
      }
    },
    seedDoubleClicked: function (event, obj) {
      console.log(obj)
      this.detailAdditions = undefined
      this.rowDoubleClicked(obj.item, ['gene', 'protein'][this.seedTypeId])
    },
    drugDoubleClicked: function (event, obj) {
      let item = obj.item
      this.detailAdditions = item.trials != null ? [{pos: 3, key: 'ClinicalTrials', value: item.trials}] : null
      this.rowDoubleClicked(item, 'drug')
    },

    rowDoubleClicked: function (item, type) {
      switch (type) {
        case 'gene':
          this.detailAttributes = this.geneDetailAttributes;
          break;
        case 'protein' :
          this.detailAttributes = this.proteinDetailAttributes;
          break;
        case 'drug':
          this.detailAttributes = this.drugDetailAttributes;
          break;
      }
      this.detailRequest = undefined
      this.detailRequest = {edge: false, type: type, id: item.id}
      this.$refs.details.showDialog(this.detailRequest)
    },
    drugClicked: function (item) {
      this.focusNode(['dru_'] + item.id)
    }
    ,
    focus: function () {
      this.$emit("focusEvent")
    }
    ,
    getColoring: function (entity, name, style) {
      return this.$utils.getColoring(this.$global.metagraph, entity, name, style);
    }
    ,

  },

  components: {
    InteractionNetworkDialog,
    DrugsDialog,
    DetailDialog,
    ButtonAdvanced,
    ButtonBack,
    ButtonNext,
    ButtonCancel,
    Tools,
    LabeledSwitch,
    DisorderHierarchyDialog,
    Network,
    SuggestionAutocomplete,
    ValidationDrugTable,
    NodeInput,
    SeedTable,
    ResultDownload,
    QuickExamples,
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
