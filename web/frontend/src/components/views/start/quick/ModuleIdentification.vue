<template>
  <v-card style="margin-bottom: 25px">
    <div style="display: flex; justify-content: flex-end; margin-left: auto; ">
      <v-tooltip left>
        <template v-slot:activator="{on, attrs}">
          <v-btn icon style="padding:1em" color="red darker" @click="makeStep('cancel')" v-on="on" v-bind="attrs">
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
          <small v-if="$refs.algorithms!=null && $refs.algorithms.getAlgorithm()!=null">{{
              $refs.algorithms.getAlgorithm().label
            }}</small>
        </v-stepper-step>
        <v-divider></v-divider>
        <v-stepper-step step="3" :complete="step>3">
          Results
          <small v-if="results.targets!=null && results.targets.length>0">Module ({{
              results.targets.length
            }})</small>
        </v-stepper-step>
        <v-divider></v-divider>
        <v-stepper-step step="4">
          Validation
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
                @click="seedTypeId=0; methodModel=1; makeStep('continue'); setBicon()">BiCoN
                <v-icon right size="1em" style="margin-left: 0">fas fa-caret-right</v-icon>
              </a>{{ ")." }}
              </span>
              <span v-else-if="algorithmSelected"> In Quick Module Identification a
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
                  <b>{{ $refs.algorithms.getAlgorithm().label }}</b>
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
                <v-list-item-subtitle class="title">Select the seed type
                </v-list-item-subtitle>
                <v-list-item-action>
                  <v-radio-group row v-model="seedTypeId"
                                 :disabled="(seedTypeId !=null && $refs.seedTable !=null && $refs.seedTable.getSeeds()!=null && $refs.seedTable.getSeeds().length>0)">
                    <v-radio label="Gene">
                    </v-radio>
                    <v-radio label="Protein">
                    </v-radio>
                  </v-radio-group>
                </v-list-item-action>
              </v-col>
            </v-row>
            <QuickExamples v-if="$refs.validation" :seedType="['gene','protein'][seedTypeId]"
                           @drugsEvent="$refs.validation.addDrugs" @exampleEvent="applyExample"
                           @disorderEvent="saveDisorders" @suggestionEvent="addToSuggestions"
                           @addNodesEvent="addToSelection"></QuickExamples>
            <v-container style="height: 560px; margin-top: 15px; max-width: 100%">
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
                        <div style="justify-content: flex-end; margin-left: auto">
                          <LabeledSwitch v-model="advancedOptions"
                                         @click="suggestionType = advancedOptions ? suggestionType : 'disorder'"
                                         label-off="Limited" label-on="Full">
                            <template v-slot:tooltip>
                              <div style="width: 300px"><b>Limited Mode:</b><br>The options are limited to the most
                                interesting and generally used ones to not overcomplicate the user interface <br>
                                <b>Full Mode:</b><br> The full mode provides a wider list of options to select from
                                for
                                more
                                specific queries.
                              </div>
                            </template>
                          </LabeledSwitch>
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
                                                @drugsEvent="$refs.validation.addDrugs" :disorder-select="true"
                                                :emit-disorders="true"
                                                :target-node-type="['gene', 'protein'][seedTypeId]"
                                                @disorderEvent="saveDisorders"
                                                @addToSelectionEvent="addToSelection" @subtypeSelection="subtypePopup"
                                                @suggestionEvent="addToSuggestions" :add-all="true"
                                                style="justify-self: flex-end;margin-left: auto"></SuggestionAutocomplete>
                      </div>
                      <NodeInput text="or provide Seed IDs by" @addToSelectionEvent="addToSelection"
                                 :idName="['entrez','uniprot'][seedTypeId]"
                                 :nodeType="['gene', 'protein'][seedTypeId]"
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
                              :disabled="$refs.seedTable==null || $refs.seedTable.getSeeds().length===0"
                              color="primary" @click="showInteractionNetwork()">
                        <v-icon>fas fa-project-diagram</v-icon>
                      </v-chip>
                    </template>
                    <span>Display an interaction network with all your current seeds</span>
                  </v-tooltip>
                  <v-tooltip left>
                    <template v-slot:activator="{attrs,on}">
                      <v-chip style="position: absolute; left:auto; right:55px" v-on="on" v-bind="attrs"
                              v-show="seedTypeId!=null"
                              color="primary">
                        <v-icon left>fas fa-capsules</v-icon>
                        {{ validationDrugCount }}
                      </v-chip>
                    </template>
                    <span>There are {{ validationDrugCount }} drugs that were associated with your query.<br> These are saved for validation purposes later.</span>
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
          <ButtonCancel @click="makeStep"></ButtonCancel>
          <ButtonNext @click="makeStep"
                      :disabled="seedTypeId<0 || $refs.seedTable == null || $refs.seedTable.getSeeds().length===0"></ButtonNext>
        </v-stepper-content>

        <v-stepper-content step="2">
          <template>
            <MIAlgorithmSelect ref="algorithms" :blitz="blitz" type="mi" :seeds="seeds"
                               socket-event="quickModuleFinishedEvent" :seed-type-id="seedTypeId"
                               @algorithmSelectedEvent="acceptAlgorithmSelectEvent"
                               @jobEvent="readJob" @clearSeedsEvent="seeds = []"></MIAlgorithmSelect>
            <ButtonCancel @click="makeStep"></ButtonCancel>
            <ButtonBack @click="makeStep"></ButtonBack>
            <ButtonNext @click="makeStep" label="RUN"
                        :disabled=" !algorithmSelected  || ($refs.algorithms.getAlgorithmMethod()==='bicon' && $refs.algorithms.getAlgorithmModels().exprFile ===undefined)"></ButtonNext>
          </template>
        </v-stepper-content>

        <v-stepper-content step="3">
          <v-card
            v-if="step===3"
            class="mb-4"
            min-height="80vh"
          >
            <v-card-subtitle class="headline">3. Module Identification Results</v-card-subtitle>
            <v-divider style="margin: 15px;"></v-divider>
            <v-container style="max-width: 100%">
              <v-row>
                <v-col cols="3" style="padding: 0 50px 0 0; margin-right: -50px">
                  <v-card-title class="subtitle-1">Seeds ({{ seeds.length }}) {{
                      (results.targets.length !== undefined && results.targets.length > 0 ? ("& Module (" + getTargetCount() + ") " + ["Genes", "Proteins"][seedTypeId]) : (": " + (state != null ? ("[" + state + "]") : "Processing")))
                    }}
                    <v-progress-circular indeterminate size="25" v-if="this.results.targets.length===0"
                                         style="margin-left:15px; z-index:50">
                    </v-progress-circular>
                  </v-card-title>

                  <template v-if="!loadingResults">
                    <v-data-table max-height="50vh" height="50vh" fixed-header dense item-key="id"
                                  :items="results.targets" :headers="getHeaders()" disable-pagination
                                  hide-default-footer @click:row="rowClicked" show-expand :single-expand="true">
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
                        <v-icon v-show="!isExpanded" @click="expand(true)" :color="getColor(item)">fas fa-angle-down
                        </v-icon>
                        <v-icon v-show="isExpanded" @click="expand(false)" :color="getColor(item)">fas fa-angle-up
                        </v-icon>
                      </template>
                      <template v-slot:expanded-item="{ headers, item }">
                        <td :colspan="headers.length">
                          <EntryDetails max-width="15vw" :gid="currentGid"
                                        :attributes="[geneDetailAttributes,proteinDetailAttributes][seedTypeId]"
                                        :detail-request="{edge:false, type:['gene', 'protein'][seedTypeId], id:item.id}"></EntryDetails>
                        </td>
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
                                :items="seeds" :headers="getHeaders(true)" disable-pagination show-expand
                                :single-expand="true"
                                hide-default-footer @click:row="rowClicked">
                    <template v-slot:item.data-table-expand="{expand, item,isExpanded}">
                      <v-icon v-show="!isExpanded" @click="expand(true)" color="#e4ca02">fas fa-angle-down</v-icon>
                      <v-icon v-show="isExpanded" @click="expand(false)" color="#e4ca02">fas fa-angle-up</v-icon>
                    </template>
                    <template v-slot:expanded-item="{ headers, item }">
                      <td :colspan="headers.length">
                        <EntryDetails max-width="15vw" :gid="currentGid"
                                      :detail-request="{edge:false, type:['gene', 'protein'][seedTypeId], id:item.id}"></EntryDetails>
                      </td>
                    </template>


                  </v-data-table>
                </v-col>
                <v-col>
                  <i v-if="!this.currentGid">The execution could take a moment. Save the current URL and return at any
                    time!</i>
                  <Network ref="graph" :configuration="graphConfig" :window-style="graphWindowStyle"
                           :legend="results.targets.length>0" :tools="results.targets.length>0" :secondaryViewer="true"
                           @loadIntoAdvancedEvent="$emit('graphLoadEvent',{post: {id: currentGid}})"
                           :show-vis-option="showVisOption">
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
                      <Tools :physics="true" :cc="false" :loops="false"
                             @toggleOptionEvent="toggleToolOption" @clickOptionEvent="clickToolOption"></Tools>
                    </template>
                  </Network>
                </v-col>
              </v-row>
            </v-container>
          </v-card>
          <ButtonCancel @click="makeStep"></ButtonCancel>
          <ButtonBack @click="makeStep" v-if="!reloaded"></ButtonBack>
          <ButtonNext @click="makeStep" label="VALIDATE" :disabled="currentGid==null"></ButtonNext>
          <v-tooltip top>
            <template v-slot:activator="{attrs, on}">
              <v-btn v-bind="attrs" v-on="on" @click="loadDrugTargets" color="primary"
                     :disabled="results.targets.length===0">
                <v-icon left>fas fa-angle-double-right</v-icon>
                Drug Ranking
              </v-btn>
            </template>
            <span>Use the results of this execution to now identify potential drug candidates.</span>
          </v-tooltip>
          <ButtonAdvanced @click="$emit('graphLoadNewTabEvent',{post: {id: currentGid}})"
                          :disabled="currentGid==null"></ButtonAdvanced>
        </v-stepper-content>
        <v-stepper-content step="4">
          <Validation ref="validation" :step="4" :seed-type-id="seedTypeId" :module="results.targets"
                      @drugCountUpdate="updateDrugCount" @printNotificationEvent="printNotification"></Validation>
          <ButtonCancel @click="makeStep"></ButtonCancel>
          <ButtonBack @click="makeStep" label="RESULTS" save></ButtonBack>
        </v-stepper-content>
      </v-stepper-items>
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
      <v-dialog
        v-model="drugTargetPopup"
        persistent
        max-width="500"
        style="z-index: 1001"
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
    <DisorderHierarchyDialog v-if="$refs.suggestions!=null" ref="disorderHierarchy"
                             @addDisorders="$refs.suggestions.loadDisorders"></DisorderHierarchyDialog>
    <InteractionNetworkDialog ref="interactionDialog"></InteractionNetworkDialog>
  </v-card>
</template>

<script>
import Network from "../../graph/Network";
import * as CONFIG from "../../../../Config"
import SuggestionAutocomplete from "@/components/app/suggestions/SuggestionAutocomplete";
import SeedDownload from "@/components/app/tables/menus/SeedDownload";
import SeedTable from "@/components/app/tables/SeedTable";
import ResultDownload from "@/components/app/tables/menus/ResultDownload";
import HeaderBar from "@/components/app/Header";
import NodeInput from "@/components/app/input/NodeInput";
import EntryDetails from "@/components/app/EntryDetails";
import LabeledSwitch from "@/components/app/input/LabeledSwitch";
import MIAlgorithmSelect from "@/components/start/quick/MIAlgorithmSelect";
import Validation from "@/components/start/quick/Validation";
import DisorderHierarchyDialog from "@/components/start/quick/DisorderHierarchyDialog";
import Tools from "@/components/views/graph/Tools";
import ButtonNext from "@/components/start/quick/ButtonNext";
import ButtonCancel from "@/components/start/quick/ButtonCancel";
import ButtonBack from "@/components/start/quick/ButtonBack";
import ButtonAdvanced from "@/components/start/quick/ButtonAdvanced";
import QuickExamples from "@/components/start/quick/QuickExamples";
import InteractionNetworkDialog from "@/components/start/quick/InteractionNetworkDialog";

export default {
  name: "ModuleIdentification",

  props: {
    blitz: Boolean,
    reload: {
      default: undefined,
      type: Object,
    }
  },
  sugQuery: undefined,
  disorderIds: [],

  data() {
    return {
      graphWindowStyle: {
        height: '60vh',
        'min-height': '60vh',
      },
      example: undefined,
      targetColorStyle: {},
      currentJid: undefined,
      currentGid: undefined,
      graphConfig: {visualized: false},
      showVisOption: false,
      uid: undefined,
      seedTypeId: undefined,
      seeds: [],
      reloaded: false,
      // seedOrigin: {},
      sourceType: undefined,
      step: 1,
      suggestionType: undefined,
      fileInputModel: undefined,
      seedInput: false,
      algorithmSelected: false,

      graph: {physics: false},
      results: {targets: [], drugs: []},
      loadingResults: true,
      advancedOptions: false,
      physicsOn: false,

      state: undefined,

      geneDetailAttributes: ["Name", "SourceIDs", "Symbols", "Chromosome", "Genomic Location", "Synonyms", "Description"],
      proteinDetailAttributes: ["Name", "SourceIDs", "Gene", "Synonyms", "Comments"],

      drugTargetPopup: false,
      rankingSelect: 1,
      validationDrugCount: 0,

      selectedSuggestions: [],
      namePopup: false,
      nameOptions: [],
      graphName: "",

      showSubtypeSelection: false,
      error: false,
    }

  },

  created() {
    this.$socket.$on("quickModuleFinishedEvent", this.convertJobResult)
    this.uid = this.$cookies.get("uid")
    this.init()
    if (this.reload)
      this.reloadJob(this.reload);
  },

  methods: {
    init: function (keepSeedType) {
      if (!keepSeedType)
        this.seedTypeId = undefined
      if (this.$refs.seedTable)
        this.$refs.seedTable.clear()
      else this.clearData()
      this.sourceType = undefined
      this.step = 1
      this.seeds = []
      this.currentJid = undefined
      this.currentGid = undefined
      this.graphName = undefined
      this.showVisOption = false
      this.example = undefined
      this.results.targets = []
      this.results.drugs = []
      this.validationDrugCount = 0
      this.reloaded = false;
    },

    reset: function (keepSeedType) {
      this.init(keepSeedType)
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
    focusNode: function (id) {
      if (this.$refs.graph == null)
        return
      this.$refs.graph.setSelection([id])
      this.$refs.graph.zoomToNode(id)
    },

    makeStep: function (button) {
      if (button === "continue") {
        this.step++
        if (this.step === 2) {
          this.seeds = this.$refs.seedTable.getSeeds()

          if (this.blitz)
            this.step++
        }
        if (this.step === 3) {
          this.$refs.algorithms.run()
        }
      }
      if (button === "back") {
        this.step--
        if (this.step === 3) {
          this.loadGraph(this.currentGid)
        }
        if (this.step === 2) {
          this.results.targets = []
          this.graphName = undefined
          this.currentGid = undefined
          this.$refs.graph.reload()
          this.$refs.validation.resetValidation();
          this.$socket.unsubscribeJob(this.currentJid)
        }

        if (this.step === 2 && this.blitz)
          this.step--
      }
      if (button === "cancel") {
        this.$socket.unsubscribeJob(this.currentJid)
        this.init()
        this.$emit("resetEvent")
      }
      if (this.step === 3) {
        if (this.currentGid == null || this.currentGid === this.graphName)
          this.graphNamePopup()
      }

    },
    loadDrugTargets: function () {
      this.rankingSelect = 1
      this.drugTargetPopup = true
    },
    rowClicked: function (item) {
      this.focusNode(['gen_', 'pro_'][this.seedTypeId] + item.id)

    },

    subtypePopup: function (item) {
      this.$refs.disorderHierarchy.loadDisorder(item.sid)
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
      this.$emit("loadDrugTargetEvent", {
        blitz: this.blitz,
        seeds: drugSeeds,
        type: ["gene", "protein"][this.seedTypeId],
        origin: "Module Identification: " + this.$refs.algorithms.getAlgorithm().label,
        disorders: this.disorderIds,
        drugs: this.$refs.validation.getDrugs(),
        suggestions: this.selectedSuggestions
      })
      this.init()
    },
    saveDisorders: function (list) {
      this.disorderIds = this.disorderIds.concat(list.filter(id => this.disorderIds.indexOf(id) === -1))
    }
    ,
    getHeaders: function (seeds) {
      let headers = [{text: "Name", align: "start", sortable: true, value: "displayName"}]
      if (!seeds)
        this.methodScores().forEach(e => {
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
    updateGraphPhysics: function () {
      this.$refs.graph.setPhysics(this.graph.physics)
    }
    ,

    setBicon: function () {
      this.$refs.algorithms.setExpMethod("bicon")
    },

    applyExample: function (example) {
      this.reset(true)
      this.example = example
      this.$nextTick(() => {
        this.$refs.algorithms.setNWMethod(example.mi.algorithm, example.mi.params)
      })
    },


    readJob: function (data, notSubbed) {
      this.state = data.state
      if (data.state === "ERROR") {
        this.error = true;
        return;
      }
      let jid = data.jid
      this.setURL(jid)
      this.currentJid = jid
      this.currentGid = data.gid
      if (this.currentGid != null && data.state === "DONE") {
        if (!notSubbed)
          this.$socket.unsubscribeJob(jid)
        this.loadTargetTable(this.currentGid).then(() => {
          this.loadGraph(this.currentGid)
        })
      }

    }
    ,
    setURL: function (jid) {
      let route = location.pathname + "?job=" + jid
      if (location.origin + route !== location.href) {
        this.$router.push(route)
      }
    },
    updateDrugCount: function () {
      this.validationDrugCount = this.$refs.validation ? this.$refs.validation.getDrugs().length : 0;
    },
    getTargetCount: function () {
      let seedids = this.seeds.map(s => s.id)
      return this.results.targets.filter(t => seedids.indexOf(t.id) === -1).length
    },

    addToSelection: function (data) {
      this.$refs.seedTable.addSeeds(data)
    }
    ,
    methodScores: function () {
      return this.$refs.algorithms.getHeaders()
    }
    ,
    reloadJob: async function (job) {
      try {
        this.reloaded = true;
        this.step = 3;
        await setTimeout(() => {
        }, 200)
        this.seedTypeId = ["gene", "protein"].indexOf(job.target)
        await setTimeout(() => {
        }, 1000)
        this.currentJid = job.jobId
        this.state = job.state
        await this.$refs.algorithms.setMethod(job.method)
        if (this.$refs.algorithms.getGroup() !== "exp" && (!job.seeds || job.seeds.length === 0))
          this.$emit("jobReloadError")
        if (job.seeds && job.seeds.length > 0)
          this.$http.getNodes(job.target, job.seeds, ["id", "displayName"]).then(response => {
            if (!response || response.length === 0) {
              this.$emit("jobReloadError")
            }
            this.seeds = response
          })
        if (job.derivedGraph && job.state === "DONE") {
          this.currentGid = job.derivedGraph;
          this.loadTargetTable(this.currentGid).then(() => {
            this.loadGraph(this.currentGid)
          })
        } else {
          this.$socket.subscribeJob(this.currentJid, "quickModuleFinishedEvent");
        }
      } catch (e) {
        this.$emit("jobReloadError")
      }
    },
    showInteractionNetwork: function () {
      this.$refs.interactionDialog.show(["gene", "protein"][this.seedTypeId], this.$refs.seedTable.getSeeds().map(n => n.id))
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
          this.seeds.forEach(s => {
            let escape = s.displayName.indexOf(sep) > -1 ? "\"" : "";
            text += data[s.id] + sep + escape + s.displayName + escape + "\n"
          })
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

        let scores = this.$refs.algorithms.getAlgorithm().scores
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
    acceptAlgorithmSelectEvent: function (value) {
      this.$set(this, "algorithmSelected", value)
    },


    loadTargetTable: function (gid) {
      let seedType = [["gene", "protein"][this.seedTypeId]]
      this.targetColorStyle = {'background-color': this.$global.metagraph.colorMap[seedType].light}
      return this.$http.get("/getGraphList?id=" + gid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        data.nodes[seedType].forEach(n => n.displayName = this.$utils.adjustLabels(n.displayName))

        let method = this.$refs.algorithms.getAlgorithm()
        let primaryAttribute = method.scores.filter(s => s.primary)[0]
        this.seedValueReplacement(data.nodes[seedType], method)
        this.results.targets = this.sort(data.nodes[seedType], primaryAttribute)
        this.rank(this.results.targets, primaryAttribute)
        this.normalize(this.results.targets, method)
        this.round(this.results.targets, method)


        this.loadingResults = false;

      }).catch(console.error)
    },

    seedValueReplacement: function (list, method) {
      let seedIds = this.seeds.map(n => n.id)
      let seeds = list.filter(n => seedIds.indexOf(n.id) > -1)
      seeds.forEach(s => s.isSeed = true)
      method.scores.forEach(score => seeds.filter(n => n[score.id] == null).forEach(n => n[score.id] = score.seed))
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

    rank: function (list, attribute) {
      if (attribute == null || (list.length > 0 && list[0].rank != null))
        return list
      let lastRank = 0;
      let lastScore = 0;

      list.forEach(drug => {
        if (lastRank === 0 || lastScore !== drug[attribute.id]) {
          lastRank++
          lastScore = drug[attribute.id];
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
      this.selectedSuggestions.push(item.text)
    },
    graphNamePopup: async function () {
      let sources = ""
      if (this.selectedSuggestions.length > 0) {
        this.selectedSuggestions.forEach(s => sources += s + "; ")
        sources = sources.substr(0, sources.length - 2)
      }
      this.nameOptions = []
      this.nameOptions.push(sources)
      this.nameOptions.push((sources + " (" + this.$refs.algorithms.getAlgorithmMethod() + ")"))
      this.nameOptions.push((sources + " (" + this.$refs.algorithms.getAlgorithmMethod() + ") [" + (await this.$refs.algorithms.getParamString()) + "]"))
      this.namePopup = true

    },
    resolveNamingDialog: function (value) {
      this.namePopup = false
      if (value == null || value.length === 0)
        return
      this.setName(value)
    },
    setName: function (name) {
      if (this.currentGid == null)
        setTimeout(() => {
          this.setName(name)
        }, 500)
      else
        this.$http.post("setGraphName", {gid: this.currentGid, name: name}).then(() => {
          this.$emit("newGraphEvent")
        }).catch(console.error)
    },
    waitForGraph: function (resolve) {
      if (this.$refs.graph === undefined)
        setTimeout(() => this.waitForGraph(resolve), 100)
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
    getColor: function (item) {
      if (item.isSeed)
        return "#e4ca02"
      return this.getColoring('nodes', ['gene', 'protein'][this.seedTypeId])
    },
    getColoring: function (entity, name, style) {
      return this.$utils.getColoring(this.$global.metagraph, entity, name, style);
    },
    focus: function () {
      this.$emit("focusEvent")
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
    loadGraph: function (graphId) {
      if (this.namePopup)
        setTimeout(() => {
          this.loadGraph(graphId)
        }, 500)
      else
        this.getGraph().then(graph => {
          this.showVisOption = false
          graph.loadNetworkById(graphId).then(() => {
            graph.showLoops(false)
            let seedIds = this.seeds.map(s => s.id)
            graph.modifyGroups(this.results.targets.filter(n => seedIds.indexOf(n.id) > -1).map(n => ["gen_", "pro_"][this.seedTypeId] + n.id), ["seedGene", "seedProtein"][this.seedTypeId])
            this.showVisOption = !this.graphConfig.visualized
          })
        })
    },
  }
  ,

  components: {
    InteractionNetworkDialog,
    QuickExamples,
    ButtonAdvanced,
    ButtonBack,
    ButtonCancel,
    ButtonNext,
    Tools,
    DisorderHierarchyDialog,
    EntryDetails,
    LabeledSwitch,
    HeaderBar,
    SuggestionAutocomplete,
    NodeInput,
    SeedDownload,
    SeedTable,
    ResultDownload,
    Network,
    MIAlgorithmSelect,
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
