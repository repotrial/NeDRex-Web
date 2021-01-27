<template>
  <v-stepper
    alt-labels
    v-model="step"
  >
    <v-stepper-header>
      <v-stepper-step step="1" :complete="step>1">
        Select Nodes
        <small v-if="sourceTypeId!==undefined & targetTypeId!==undefined">{{
            nodeList[sourceTypeId].text
          }}->{{ nodeList[targetTypeId].text }}</small>
      </v-stepper-step>
      <v-divider></v-divider>
      <v-stepper-step step="2" :complete="step>2 ">
        Select Path
        <small>Something</small>
      </v-stepper-step>
      <v-divider></v-divider>
      <v-stepper-step step="3">
        Graph
      </v-stepper-step>
    </v-stepper-header>

    <v-stepper-items>
      <v-stepper-content step="1">
        <v-card
          v-if="step===1"
          class="mb-12"
          height="75vh"
        >

          <v-card-subtitle class="headline">I Node Configuration</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">Add seeds to your.</v-card-subtitle>

          <v-container style="height: 80%">
            <v-row style="height: 50vh">
              <v-col cols="5">
                <v-list-item-subtitle class="title">1a. Select the source node type:</v-list-item-subtitle>
                <v-list-item-action>
                  <v-select item-value="id" :items="nodeList" v-model="sourceTypeId"
                            placeholder="Select start Node"
                            :disabled="sources!==undefined && sources.length>0">

                  </v-select>
                </v-list-item-action>
                <v-container v-if="sourceTypeId!==undefined">
                  <v-card-subtitle class="title">1b. Prefilter the nodes:</v-card-subtitle>
                  <v-row>
                    <v-col cols="3">
                      <v-select :items="getSuggestionSelection(0)" v-model="suggestionType[0]"
                                placeholder="connected to"></v-select>
                    </v-col>
                    <v-col>
                      <v-autocomplete
                        clearable
                        :search-input.sync="nodeSourceSuggestions"
                        :disabled="suggestionType[0]===undefined || suggestionType[0]<0"
                        :loading="suggestions.loading"
                        :items="suggestions.data"
                        v-model="suggestionSourceModel"
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
                  <v-file-input :label="'by '+nodeIdTypeList[sourceTypeId]+' ids'"
                                v-on:change="onSourceFileSelected"
                                show-size
                                prepend-icon="far fa-list-alt"
                                v-model="fileInputModel"
                                dense
                  ></v-file-input>
                  <v-row>
                    <v-col cols="6">
                      <v-card-title>Source Nodes ({{ sources.length }})
                      </v-card-title>
                    </v-col>
                    <v-col>
                      <v-chip outlined v-show="sources.length>0" style="margin-top:15px" @click="removeAll(0)">
                        <v-icon left>fas fa-download</v-icon>
                        Clear All
                      </v-chip>
                      <v-chip outlined v-show="sources.length>0" style="margin-top:15px" @click="downloadList(0)">
                        <v-icon left>fas fa-download</v-icon>
                        Save
                      </v-chip>
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <v-list max-height="20vh" height="20vh" class="overflow-y-auto">
                        <v-list-item v-for="(node,index) in sources" :key="node.id">
                          <v-list-item-title>{{ node.displayName }}</v-list-item-title>
                          <v-list>
                            <template v-for="o in getOrigins(node.id,0)">
                              <v-list-item-subtitle :key="node.id+o">{{ o }}</v-list-item-subtitle>
                            </template>
                          </v-list>
                          <v-list-item-action>
                            <v-btn icon @click="removeNode(index,node.id,0)" color="red">
                              <v-icon>far fa-trash-alt</v-icon>
                            </v-btn>
                          </v-list-item-action>
                        </v-list-item>
                      </v-list>
                    </v-col>
                  </v-row>
                </v-container>
              </v-col>
              <v-col cols="2">
              <v-divider vertical></v-divider>
              </v-col>
              <v-col cols="5">
                <v-list-item-subtitle class="title">2a. Select the target node type:</v-list-item-subtitle>
                <v-list-item-action>
                  <v-select item-value="id" :items="nodeList" v-model="targetTypeId"
                            placeholder="Select start Node"
                            :disabled="targets!==undefined && targets.length>0">

                  </v-select>
                </v-list-item-action>
                <v-container v-if="targetTypeId!==undefined">
                  <v-card-subtitle class="title">2b. Optional: Prefilter the nodes:</v-card-subtitle>
                  <v-row>
                    <v-col cols="3">
                      <v-select :items="getSuggestionSelection(1)" v-model="suggestionType[1]"
                                placeholder="connected to"></v-select>
                    </v-col>
                    <v-col>
                      <v-autocomplete
                        clearable
                        :search-input.sync="nodeTargetSuggestions"
                        :disabled="suggestionType[1]===undefined || suggestionType[1]<0"
                        :loading="suggestions.loading"
                        :items="suggestions.data"
                        v-model="suggestionTargetModel"
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
                  <v-file-input :label="'by '+nodeIdTypeList[targetTypeId]+' ids'"
                                v-on:change="onTargetFileSelected"
                                show-size
                                prepend-icon="far fa-list-alt"
                                v-model="fileInputModel"
                                dense
                  ></v-file-input>
                  <v-row v-if="targets.length>0">
                    <v-col cols="6">
                      <v-card-title>Target Nodes ({{ targets.length }})
                      </v-card-title>
                    </v-col>
                    <v-col>
                      <v-chip outlined v-show="targets.length>0" style="margin-top:15px" @click="removeAll(1)">
                        <v-icon left>fas fa-download</v-icon>
                        Clear All
                      </v-chip>
                      <v-chip outlined v-show="targets.length>0" style="margin-top:15px" @click="downloadList(1)">
                        <v-icon left>fas fa-download</v-icon>
                        Save
                      </v-chip>
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <v-list max-height="20vh" height="20vh" class="overflow-y-auto">
                        <v-list-item v-for="(node,index) in targets" :key="node.id">
                          <v-list-item-title>{{ node.displayName }}</v-list-item-title>
                          <v-list>
                            <template v-for="o in getOrigins(node.id,1)">
                              <v-list-item-subtitle :key="node.id+o">{{ o }}</v-list-item-subtitle>
                            </template>
                          </v-list>
                          <v-list-item-action>
                            <v-btn icon @click="removeNode(index,node.id,1)" color="red">
                              <v-icon>far fa-trash-alt</v-icon>
                            </v-btn>
                          </v-list-item-action>
                        </v-list-item>
                      </v-list>
                    </v-col>
                  </v-row>
                </v-container>
              </v-col>
            </v-row>
          </v-container>
        </v-card>
        <v-btn
          color="primary"
          @click="makeStep(1,'continue')"
          :disabled="sourceTypeId===undefined || targetTypeId===undefined  ||  sources.length<1"
        >
          Continue
        </v-btn>

        <v-btn text @click="makeStep(1,'cancel')">
          Cancel
        </v-btn>
      </v-stepper-content>
      <v-stepper-content step="2">
        <v-card
          v-if="step===1"
          class="mb-12"
          height="75vh"
        >
          <v-card-subtitle class="headline">2. Path Selection</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">Subtitle blabla
          </v-card-subtitle>
        </v-card>
        <v-btn
          @click="makeStep(2,'back')"
          >
          Back
        </v-btn>
        <v-btn
          color="primary"
          @click="makeStep(2,'continue')"
        >
          Continue
        </v-btn>

        <v-btn text @click="makeStep(2,'cancel')">
          Cancel
        </v-btn>
      </v-stepper-content>
      <v-stepper-content step="3">
        <v-card
          v-if="step===1"
          class="mb-12"
          height="75vh"
        >
          <v-card-subtitle class="headline">3. Results</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">Subtitle blabala
          </v-card-subtitle>
        </v-card>
        <v-btn
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
import Utils from "../../scripts/Utils";

export default {
  name: "Guided",

  props: {
    metagraph: Object,
  },

  uid: undefined,

  data() {
    return {

      sugQuery: [undefined, undefined],
      graphWindowStyle: {
        height: '60vh',
        'min-height': '60vh',
      },

      sources: [],
      targets: [],
      nodeOrigins: [{}, {}],
      fileInputModel: undefined,

      sourceTypeId: undefined,
      targetTypeId: undefined,
      step: 1,
      nodeList: [],
      nodeIdTypeList: [],

      suggestionType: [undefined, undefined],
      suggestions: {loading: false, data: []},
      nodeSourceSuggestions: null,
      nodeTargetSuggestions: null,
      suggestionSourceModel: null,
      suggestionTargetModel: null,
    }
  },

  created() {
    this.uid = this.$cookies.get("uid")
    this.nodeList =[]
    this.nodeIdTypeList=[]
    this.metagraph.nodes.forEach((n, index) => {
      this.nodeList.push({id: index, value: n.group, text: n.label})
      this.nodeIdTypeList.push(this.metagraph.data[n.label])
    })
    this.init()
  },

  watch: {

    nodeSourceSuggestions: function (val) {
      this.getSuggestions(val, 0, false)
    },
    nodeTargetSuggestions: function (val) {
      this.getSuggestions(val, 1, false)
    },
    suggestionSourceModel: function (val) {
      this.applySuggestion(val, 0)
    },
    suggestionTargetModel: function (val) {
      this.applySuggestion(val, 1)
    },


  },

  methods: {


    init: function () {

      this.sugQuery = [undefined, undefined]

      this.sourceTypeId = undefined
      this.targetTypeId = undefined
      this.step = 1

      this.sources = []
      this.targets = []
      this.nodeOrigins = [{}, {}]

      this.suggestionType = [undefined, undefined]
      this.suggestions = {loading: false, data: []}
      this.nodeSourceSuggestions = null
      this.nodeTargetSuggestions = null
      this.suggestionSourceModel = null
      this.suggestionTargetModel = null

    },

    getSuggestionSelection: function (index) {
      console.log(this.nodeList[[this.sourceTypeId, this.targetTypeId][index]])
      let type = this.nodeList[[this.sourceTypeId, this.targetTypeId][index]].value
      let selfAdded = false;
      let nodeId = this.metagraph.nodes.filter(n => n.group === type)[0].id
      let typeList = this.metagraph.edges.filter(e => e.from !== e.to && e.from === nodeId || e.to === nodeId).map(e => e.to === nodeId ? e.from : e.to).map(nid => {
        let node = this.metagraph.nodes.filter(n => n.id === nid)[0]
        if(node.group === type)
          selfAdded=true
        return {value: node.group, text: node.label}
      })
      if(!selfAdded)
        typeList.push({value:type, text:this.nodeList[[this.sourceTypeId, this.targetTypeId][index]].text})
      return typeList
    },

    applySuggestion: function (val, index) {
      if (val !== undefined && val != null) {
        this.$http.post("getConnectedNodes", {
          sourceType: this.suggestionType[index],
          targetType: this.nodeList[this.sourceTypeId].value,
          sourceIds: val.ids,
          noloop: this.nodeList[this.sourceTypeId].value === this.suggestionType[index]
        }).then(response => {
          if (response.data !== undefined)
            return response.data
        }).then(data => {
          this.addToSelection(data, index, "SUG:" + val.text + "[" + this.suggestionType + "]")
        }).then(() => {
          this[["suggestionSourceModel","suggestionTargetModel"][index]] = undefined
          this.sugQuery[index]=""
        }).catch(console.log)
      }
    },


    getSuggestions: function (val, index, timeouted) {
      if (!timeouted) {
        this.sugQuery[index] = val
        if (val == null || val.length < 3) {
          this.suggestions.data = []
          return
        }
        setTimeout(this.getSuggestions, 500, val, index, true)
      } else {
        if (val !== this.sugQuery[index]) {
          return
        }
        let name = this.suggestionType[index]
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

    onSourceFileSelected: function (file) {
      this.onFileSelected(file, 0)
    },

    onTargetFileSelected: function (file) {
      this.onFileSelected(file, 1)
    },

    onFileSelected: function (file, index) {
      if (file == null)
        return
      Utils.readFile(file).then(content => {
        this.$http.post("mapFileListToItems", {
          type: this.nodeList[[this.sourceTypeId, this.targetTypeId][index]],
          file: content
        }).then(response => {
          if (response.data)
            return response.data
        }).then(data => {
          this.addToSelection(data, index, "FILE:" + file.name)
        }).then(() => {
          this.fileInputModel = undefined
        }).catch(console.log)
      }).catch(console.log)
    },

    addToSelection: function (list, index, nameFrom) {
      let table = this[["sources", "targets"][index]]
      let ids = table.map(n => n.id)
      let count = 0
      let origins = this.nodeOrigins[index]
      list.forEach(e => {
        if (ids.indexOf(e.id) === -1) {
          count++
          table.push(e)
        }


        if (origins[e.id] !== undefined) {
          if (origins[e.id].indexOf(nameFrom) === -1)
            origins[e.id].push(nameFrom)
        } else
          origins[e.id] = [nameFrom]

      })
      this.$emit("printNotificationEvent", "Added " + list.length + "from " + nameFrom + " (" + count + " new) seeds!", 1)
    }
    ,

    getOrigins: function (id, index) {
      if (this.nodeOrigins[index][id] === undefined)
        return ["?"]
      else
        return this.nodeOrigins[index][id]
    }
    ,
    removeNode: function (lindex, id, index) {
      this[["sources", "targets"][index]].splice(lindex, 1)
      this.nodeOrigins[index][id] = undefined
    },

    removeAll: function (index) {
      this[["sources", "targets"][index]] = []
      this.nodeOrigins[index] = {}
    }
    ,
    downloadList: function (index) {
      let nodeType = this.nodeList[[this.sourceTypeId, this.targetTypeId][index]].value
      this.$http.post("mapToDomainIds", {
        type: nodeType,
        ids: [this.sources, this.targets][index].map(n => n.id)
      }).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        let text = "";
        Object.values(data).forEach(id => text += id + "\n")
        this.download(nodeType + "_sources.tsv", text)
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

    makeStep: function (s, button) {
      if (button === "continue") {
        this.step++
      }
      if (button === "back") {
        if (this.step === 3) {
          this.results.targets = []
          this.$refs.graph.reload()
          // this.$socket.unsubscribeJob(this.currentJid)
        }
        this.step--
      }

      if (button === "cancel") {
        this.init()
        // this.$emit("resetEvent")
      }
      if (this.step === 3)
        console.log("load graph")
      // this.submitAlgorithm()
    },

  }

}
</script>

<style scoped>

</style>
