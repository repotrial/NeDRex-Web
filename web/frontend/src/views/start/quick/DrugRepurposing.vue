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
          height="75vh"
        >

          <v-card-subtitle class="headline">1. Seed Configuration</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">Add seeds to your.</v-card-subtitle>
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
                <v-list max-height="40vh" height="40vh" class="overflow-y-auto">
                  <v-list-item v-for="(seed,index) in seeds" :key="seed.id">
                    <v-list-item-title>{{ seed.displayName }}</v-list-item-title>
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
          class="mb-12"
          height="700px"
        >
          <v-card-subtitle class="headline">2. Algorithm Selection</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">Select and adjust the algorithm you want to use on your seeds to
            generate a ranked list of applicable drugs for.
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
                      <v-switch
                        label="Only use experimentally validated interaction networks"
                        v-model="experimentalSwitch"
                      >
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
                  </v-row>
                  <v-row>
                    <v-col>
                      <v-slider
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
          class="mb-12"
          height="700px"
        >
          <v-container>
            <v-row>
              <v-col cols="2">
                <v-card-title class="subtitle-1">Seeds ({{ seeds.length }})
                </v-card-title>
                <template v-if="results.targets.length>=0">
                  <v-simple-table max-height="45vh" height="45vh" class="overflow-y-auto" fixed-header>
                    <template v-slot:default>
                      <thead>
                      <tr>
                        <th class="text-left">
                          Name
                        </th>
                      </tr>
                      </thead>
                      <tbody>
                      <tr v-for="seed in seeds" :key="seed.id">
                        <td>{{ seed.displayName }}</td>
                      </tr>
                      </tbody>
                    </template>
                  </v-simple-table>
                  <v-chip outlined style="margin-top:15px" @click="downloadList">
                    <v-icon left>fas fa-download</v-icon>
                    Save Seeds
                  </v-chip>
                </template>
              </v-col>
              <v-col cols="7">
                <Graph ref="graph" :configuration="graphConfig" :window-style="graphWindowStyle"></Graph>
              </v-col>
              <v-col cols="3">
                <v-card-title class="subtitle-1"> Targets{{ (results.targets.length !== undefined && results.targets.length > 0 ? (" (" + (results.targets.length - seeds.length) + ")") : ": Processing") }}
                  <v-progress-circular indeterminate v-if="this.results.targets.length===0" style="margin-left:15px">
                  </v-progress-circular>
                </v-card-title>
                <template v-if="results.targets.length>=0">
                  <v-simple-table max-height="45vh" height="45vh" class="overflow-y-auto" fixed-header>
                    <template v-slot:default>
                      <thead>
                      <tr>
                        <th class="text-left">
                          Name
                        </th>
                        <th v-for="val in methodScores()" class="text-left">
                          {{ val.name }}
                        </th>
                      </tr>
                      </thead>
                      <tbody>
                      <tr v-for="seed in results.targets" :key="seed.id" :style="targetColorStyle">
                        <td>{{ seed.displayName }}</td>
                        <td v-for="val in methodScores()">{{ seed[val.id] }}</td>
                      </tr>
                      </tbody>
                    </template>
                  </v-simple-table>
                  <v-chip outlined style="margin-top:15px"
                          @click="downloadResultList" v-if="this.results.targets.length>0">
                    <v-icon left>fas fa-download</v-icon>
                    Save Drug Ranking
                  </v-chip>
                </template>
              </v-col>
            </v-row>
            <v-divider></v-divider>
            <v-row>
              <v-col>
                <v-chip outlined v-if="jobs[currentJid]!==undefined && jobs[currentJid].result!==undefined"
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
              <v-col>
                <v-chip outlined v-show="results.targets.length>0" style="margin-top:15px">
                  <v-icon left>fas fa-angle-double-right</v-icon>
                  Continue to Drug-Target Identification
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


  </v-stepper>
</template>

<script>
import Utils from "../../../scripts/Utils";
import Graph from "../../Graph";

export default {
  name: "DrugRepurposing",

  props: {
    blitz: Boolean,
    metagraph: Object,
  },
  sugQuery:"",


  data() {
    return {
      graphWindowStyle: {
        height: '60vh',
        'min-height': '60vh',
      },
      targetColorStyle: {},
      graphConfig: {visualized: false},
      uid: undefined,
      seedTypeId: undefined,
      seeds: [],
      seedOrigin: {},
      method: undefined,
      sourceType: undefined,
      step: 1,
      highlighted: [],
      suggestionType: undefined,
      suggestions: {loading: false, data: []},
      nodeSuggestions: null,
      suggestionModel: null,
      fileInputModel: undefined,
      methods: [
        {id: "trustrank", label: "TrustRank", scores: [{id: "score", name: "Score"}]},
        {id: "centrality", label: "Closeness Centrality", scores: [{id: "score", name: "Score"}]}],
      graph: {physics: false},
      methodModel: undefined,
      experimentalSwitch: true,
      results: {seeds: [], targets: []},
      jobs: {},
      currentJid: undefined,
      models: {
          onlyApproved: true,
          onlyDirect: true,
          damping: 0.85
      }
    }
  },
  watch: {

    nodeSuggestions: function (val) {
      this.getSuggestions(val,false)
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
          this.addToSelection(data, "SUG:" + val.text + "[" + this.suggestionType + "]")
        }).then(() => {
          this.suggestionModel = undefined
        }).catch(console.log)
      }
    },

  },

  created() {
    this.$socket.$on("quickFinishedEvent", this.convertJobResult)
    this.uid = this.$cookies.get("uid")
    this.init()

    //TODO dev
    // this.seedTypeId = 0
    // this.seeds = [{
    //   "primaryDomainId": "entrez.3757",
    //   "displayName": "KCNH2",
    //   "id": 19888
    // }, {"primaryDomainId": "entrez.5005", "displayName": "ORM2", "id": 54656}, {
    //   "primaryDomainId": "entrez.4988",
    //   "displayName": "OPRM1",
    //   "id": 13457
    // }, {"primaryDomainId": "entrez.4985", "displayName": "OPRD1", "id": 13458}, {
    //   "primaryDomainId": "entrez.4986",
    //   "displayName": "OPRK1",
    //   "id": 13459
    // }, {"primaryDomainId": "entrez.23643", "displayName": "LY96", "id": 1413}, {
    //   "primaryDomainId": "entrez.3359",
    //   "displayName": "HTR3A",
    //   "id": 29783
    // }, {"primaryDomainId": "entrez.57053", "displayName": "CHRNA10", "id": 1177}, {
    //   "primaryDomainId": "entrez.116443",
    //   "displayName": "GRIN3A",
    //   "id": 50124
    // }]


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
      params['damping'] = this.models.damping;

      params['type'] = ["gene", "protein"][this.seedTypeId]
      this.executeJob(method, params)
    },
    getSuggestions: function (val, timeouted) {
      if (!timeouted) {
        this.sugQuery = val
        if (val == null || val.length < 3) {
          this.suggestions.data = []
          return
        }
        setTimeout(function() {this.getSuggestions(val, true)}.bind(this), 500)
      } else {
        if (val !== this.sugQuery) {
          return
        }
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
      }
    },

    executeJob: function (algorithm, params) {
      let payload = {userId: this.uid, algorithm: algorithm, params: params}
      console.log(params)
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
        if (this.seedOrigin[e.id] !== undefined)
          this.seedOrigin[e.id].push(nameFrom)
        else
          this.seedOrigin[e.id] = [nameFrom]
      })
      this.$emit("printNotificationEvent", "Added " + list.length + "from " + nameFrom + " (" + count + " new) seeds!", 1)
    },
    methodScores: function () {
      if (this.methodModel !== undefined && this.methodModel > -1)
        return this.methods[this.methodModel].scores;
      return []
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
      Utils.readFile(file).then(content => {
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
        data.forEach(id => text += id + "\n")
        this.download(["gene", "protein"][this.seedTypeId] + "_seeds.tsv", text)
      }).catch(console.log)
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
        this.methods[this.methodModel].scores.forEach(s=> text+="\t"+s.name)
        text+="\n"
        this.results.targets.forEach(t => {
            text += data[t.id]
          this.methods[this.methodModel].scores.forEach(s => text += "\t" + t[s.id])
            text += "\n"
          }
        )
        this.download( "drug_ranking.tsv", text)
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
      this.targetColorStyle = {'background-color': this.metagraph.colorMap['drug'].light}
      return this.$http.get("/getGraphList?id=" + gid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.results.targets = data.nodes.drug.sort((e1,e2)=>e2.score-e1.score)
      }).catch(console.log)
    },
    loadGraph: function (graphId) {
      this.$refs.graph.show(graphId).then(()=>{
        this.$refs.graph.showLoops(false)
      })
    }
  },

  components: {
    Graph
  }
}
</script>

<style scoped>

</style>
