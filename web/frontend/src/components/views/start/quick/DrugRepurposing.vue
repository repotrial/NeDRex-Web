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
          height="75vh"
        >

          <v-card-subtitle class="headline">1. Seed Configuration</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">Add seeds to your
            list. Use the autocomplete system or the id list upload to add seed entries. The list can be manually
            adjusted or the intersection of multiple sources may be created.
          </v-card-subtitle>
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
          <v-container style="height: 80%">
            <v-row style="height: 50vh">
              <v-col cols="6">
                <v-container v-if="seedTypeId!==undefined">
                  <v-card-title style="margin-left: -25px">Add seeds associated to</v-card-title>
                  <v-row>
                    <v-col cols="3">
                      <v-select :items="getSuggestionSelection()" v-model="suggestionType"
                                placeholder="connected to" style="width: 100%"></v-select>
                    </v-col>
                    <v-col cols="6">
                      <SuggestionAutocomplete :suggestion-type="suggestionType" :target-node-type="['gene', 'protein'][this.seedTypeId]" @addToSelectionEvent="addToSelection"></SuggestionAutocomplete>
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
                <v-list max-height="40vh" height="40vh" class="overflow-y-auto" ref="seedList">
                  <v-list-item v-for="(seed,index) in seeds" :key="seed.id">
                    <v-list-item-title>{{ $utils.adjustLabels(seed.displayName) }}</v-list-item-title>
                    <v-list>
                      <template v-for="o in getOrigins(seed.id)">
                        <v-list-item-subtitle :key="seed.id+o">{{ o }}</v-list-item-subtitle>
                      </template>
                    </v-list>
                    <v-list-item-action>
                      <v-btn icon @click="seeds.splice(index,1)" color="red">
                        <v-icon>far fa-trash-alt</v-icon>
                      </v-btn>
                    </v-list-item-action>
                  </v-list-item>
                </v-list>
                <v-chip outlined v-show="seeds.length>0" style="margin-top:15px" @click="downloadList">
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
          <v-card-subtitle class="headline">2. Algorithm Selection</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">Select and adjust the algorithm you want to apply on the
            constructed module to
            identify and rank drug candidates.
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
                  <v-card-title style="margin-left:-25px">Configure Parameters</v-card-title>
                  <v-row>
                    <v-col>
                      <v-slider
                        hide-details
                        class="align-center"
                        v-model="models.topX"
                        step="1"
                        min="1"
                        max="2000"
                      >
                        <template v-slot:prepend>
                          <v-text-field
                            v-model="models.topX"
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
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <v-switch
                        label="Only direct Drugs"
                        v-model="models.onlyDirect"
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
                    </v-col>
                    <v-col>
                      <v-switch
                        label="Only approved Drugs"
                        v-model="models.onlyApproved"
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
                        v-model="models.filterElements"
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
                        v-show="methodModel===0"
                        hide-details
                        class="align-center"
                        v-model="models.damping"
                        step="0.01"
                        min="0"
                        max="1"
                      >
                        <template v-slot:prepend>
                          <v-text-field
                            v-model="models.damping"
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
          :disabled="methodModel===undefined"
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
          height="750px"
        >
          <v-container>
            <v-card-subtitle class="headline">3. Drug Prioritization Results</v-card-subtitle>
            <v-row>
              <v-col cols="2" style="padding: 0 50px 0 0; margin-right: -50px">
                <v-card-title class="subtitle-1">Seeds ({{ seeds.length }})
                </v-card-title>
                  <v-data-table max-height="45vh" height="45vh" class="overflow-y-auto" fixed-header dense item-key="id"
                                :items="seeds" :headers="getHeaders(true)" disable-pagination
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
                <v-chip outlined style="margin-top:15px" @click="downloadList">
                  <v-icon left>fas fa-download</v-icon>
                  Save Seeds
                </v-chip>
              </v-col>
              <v-col>
                <Graph ref="graph" :configuration="graphConfig" :window-style="graphWindowStyle"
                       :legend="results.targets.length>0" :meta="metagraph">
                  <template v-slot:legend v-if="results.targets.length>0">
                    <v-card style="width: 8vw; max-width: 10vw;padding-top: 35px">
                      <v-list>
                        <v-list-item>
                          <v-list-item-icon>
                            <v-icon left :color="getColoring('nodes',['gene','protein'][seedTypeId],'light')">fas
                              fa-genderless
                            </v-icon>
                          </v-list-item-icon>
                          <v-list-item-title style="margin-left: -25px">{{ ['Gene', 'Protein'][seedTypeId] }}
                          </v-list-item-title>
                          <v-list-item-subtitle>{{ seeds.length }}</v-list-item-subtitle>
                        </v-list-item>
                        <v-list-item>
                          <v-list-item-icon>
                            <v-icon left :color="getColoring('nodes','drug','light')">fas fa-genderless</v-icon>
                          </v-list-item-icon>
                          <v-list-item-title style="margin-left: -25px">Drug</v-list-item-title>
                          <v-list-item-subtitle>{{ results.targets.length }}</v-list-item-subtitle>
                        </v-list-item>
                      </v-list>
                    </v-card>
                  </template>
                </Graph>
              </v-col>
              <v-col cols="2" style="padding: 0">
                <v-card-title class="subtitle-1"> Targets{{
                    (results.targets.length !== undefined && results.targets.length > 0 ? (" (" + (results.targets.length) + ")") : ": Processing")
                  }}
                  <v-progress-circular indeterminate v-if="this.results.targets.length===0" style="margin-left:15px">
                  </v-progress-circular>
                </v-card-title>
                <template v-if="results.targets.length>=0">
                  <v-data-table max-height="45vh" height="45vh" class="overflow-y-auto" fixed-header dense item-key="id"
                                :items="results.targets" :headers="getHeaders()" disable-pagination
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
                          @click="downloadResultList" v-if="results.targets.length>0">
                    <v-icon left>fas fa-download</v-icon>
                    Save Drug Ranking (Top {{ models.topX }})
                  </v-chip>
                  <v-chip outlined style="margin-top:15px"
                          @click="downloadFullResultList" v-if="results.targets.length>0">
                    <v-icon left>fas fa-download</v-icon>
                    Save Complete Drug Ranking
                  </v-chip>
                </template>
              </v-col>
            </v-row>
            <v-divider style="margin-top:10px"></v-divider>
            <v-row>
              <v-col>
                <v-chip outlined v-if="jobs[currentJid] && jobs[currentJid].result"
                        style="margin-top:15px"
                        @click="$emit('graphLoadEvent',{post: {id: jobs[currentJid].result}})">
                  <v-icon left>fas fa-angle-double-right</v-icon>
                  Load Result into Advanced View
                </v-chip>
              </v-col>
              <v-col>
                <v-switch label="Physics" v-model="graph.physics" @click="updateGraphPhysics()"
                          v-if="results.targets.length>0">
                </v-switch>

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


  </v-stepper>
</template>

<script>
import Graph from "../../graph/Graph";
import * as CONFIG from "../../../../Config"
import SuggestionAutocomplete from "@/components/app/suggestions/SuggestionAutocomplete";

export default {
  name: "DrugRepurposing",

  props: {
    blitz: Boolean,
    metagraph: Object,
  },
  sugQuery: "",

  data() {
    return {
      graphWindowStyle: {
        height: '60vh',
        'min-height': '60vh',
      },
      graphConfig: {visualized: false},
      targetColorStyle: {},
      uid: undefined,
      seedTypeId: undefined,
      seeds: [],
      seedOrigin: {},
      method: undefined,
      sourceType: undefined,
      step: 1,
      suggestionType: undefined,
      fileInputModel: undefined,
      methods: [
        {id: "trustrank", label: "TrustRank", scores: [{id: "score", name: "Score", decimal: true}]},
        {id: "centrality", label: "Closeness Centrality", scores: [{id: "score", name: "Score", decimal: true}]}],
      graph: {physics: false},
      methodModel: undefined,
      experimentalSwitch: true,
      results: {seeds: [], targets: []},
      jobs: {},
      currentJid: undefined,
      models: {
        onlyApproved: true,
        onlyDirect: true,
        damping: 0.85,
        topX: 100,
        filterElements: true,
      }
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
      this.methodModel = undefined
      if (this.blitz) {
        this.methodModel = 1
      }
      this.results.target = []
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
    getHeaders: function (seed) {
      let headers = [{text: "Name", align: "start", sortable: true, value: "displayName"}]
      if(!seed)
      this.methodScores().forEach(e => headers.push({
        text: e.name,
        align: e.decimal ?"start":"end",
        sortable: true,
        value: e.id,
      }))
      return headers
    },
    seedClicked:function(item){
      this.focusNode(['gen_','pro_'][this.seedTypeId]+item.id)
    },
    drugClicked:function(item) {
      this.focusNode(['dru_']+item.id)
    },
    updateGraphPhysics: function () {
      this.$refs.graph.setPhysics(this.graph.physics)
    },
    submitAlgorithm: function () {
      let params = {}
      let method = this.methods[this.methodModel].id
      params.experimentalOnly = this.experimentalSwitch

      params["addInteractions"] = true
      params["nodesOnly"] = true

      params['direct'] = this.models.onlyDirect;
      params['approved'] = this.models.onlyApproved;
      if (method === "trustrank")
        params['damping'] = this.models.damping;

      params['type'] = ["gene", "protein"][this.seedTypeId]
      params['topX'] = this.models.topX
      params['elements'] = !this.models.filterElements
      this.executeJob(method, params)
    },
    executeJob: function (algorithm, params) {
      let payload = {userId: this.uid, algorithm: algorithm, params: params}
      payload.selection = true
      payload.experimentalOnly = params.experimentalOnly
      payload["nodes"] = this.seeds.map(n => n.id)
      if (this.seeds.length === 0) {
        this.printNotification("Cannot execute " + algorithm + " without seed nodes!", 1)
        return;
      }
      this.$http.post("/submitJob", payload).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.$socket.subscribeJob(data.jid, "quickFinishedEvent");
        this.readJob(data)
      }).catch(console.log)
    },
    readJob: function (data) {
      let jid = data.jid
      this.currentJid = jid
      let base = data.basis
      let result = data.gid
      if (this.jobs[jid] === undefined)
        this.jobs[jid] = {}
      if (base != null) {
        if (this.jobs[jid].base === undefined) {
          this.jobs[jid].base = base
        }
      }
      if (result != null && data.state === "DONE") {
        this.$socket.unsubscribeJob(jid)
        this.jobs[jid].result = result
        this.loadTargetTable(result).then(() => {
          this.loadGraph(result)
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
    },
    methodScores: function () {
      if (this.methodModel !== undefined && this.methodModel > -1)
        return this.methods[this.methodModel].scores;
      return []
    },

    setSeeds: function (seedIds, type) {
      this.seedTypeId = ["gene", "protein"].indexOf(type)
      this.$http.post("getConnectedNodes", {
        sourceType: type,
        targetType: type,
        sourceIds: seedIds,
        noloop: true
      }).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.addToSelection(data, "METH: Module Ident")
      }).then(() => {
        this.suggestionModel = undefined
      }).catch(console.log)
    },

    getOrigins: function (id) {
      if (this.seedOrigin[id] === undefined)
        return ["?"]
      else
        return this.seedOrigin[id]
    },

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
        Object.values(data).forEach(id => text += id + "\n")
        this.download(["gene", "protein"][this.seedTypeId] + "_seeds.tsv", text)
      }).catch(console.log)
    },
    downloadFullResultList: function () {
      window.open(CONFIG.HOST_URL + CONFIG.CONTEXT_PATH + '/api/downloadJobResult?jid=' + this.currentJid)
    },
    downloadResultList: function () {
      this.$http.post("mapToDomainIds", {
        type: 'drug',
        ids: this.results.targets.map(l => l.id)
      }).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        let text = "id";
        this.methods[this.methodModel].scores.forEach(s => text += "\t" + s.name)
        text += "\n"
        this.results.targets.forEach(t => {
            text += data[t.id]
            this.methods[this.methodModel].scores.forEach(s => text += "\t" + t[s.id])
            text += "\n"
          }
        )
        this.download("drug_ranking-Top_" + this.models.topX + ".tsv", text)
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
    },
    convertJobResult: function (res) {
      let data = JSON.parse(res)
      this.readJob(data)
    },
    loadTargetTable: function (gid) {
      let scoreAttrs = this.methods[this.methodModel].scores.filter(s => s.decimal)
      this.targetColorStyle = {'background-color': this.metagraph.colorMap['drug'].light}
      return this.$http.get("/getGraphList?id=" + gid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => this.$utils.roundScores(data, 'drug', scoreAttrs)
      ).then(data => {
        this.results.targets = data.nodes.drug.sort((e1, e2) => e2.score - e1.score)
      }).catch(console.log)
    },
    roundScores: function (data) {
      return data
    }
    ,
    clearList: function () {
      this.seeds = []
      this.seedOrigin = {}
    }
    ,
    removeNonIntersecting: function () {
      let remove = []
      Object.keys(this.seedOrigin).forEach(seed => {
        if (this.seedOrigin[seed].length < 2) {
          delete this.seedOrigin[seed]
          remove.push(parseInt(seed))
        }
      })
      this.seeds = this.seeds.filter(s => remove.indexOf(s.id) === -1)
    }
    ,
    focusNode: function (id) {
      if (this.$refs.graph === undefined)
        return
      this.$refs.graph.setSelection([id])
      this.$refs.graph.zoomToNode(id)
    }
    ,
    waitForGraph: function (resolve) {
      if (this.$refs.graph === undefined)
        setTimeout(this.waitForGraph, 100)
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
    loadGraph: function (graphId) {
      this.getGraph().then(graph => {
        graph.setLoading(true)
        graph.show(graphId).then(() => {
          graph.showLoops(false)
          graph.setLoading(false)
        })
      })
    }
    ,
    focus: function () {
      this.$emit("focusEvent")
    }
    ,
    getColoring: function (entity, name) {
      return this.$utils.getColoring(this.metagraph, entity, name);
    }
    ,
  },

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
