<template>
  <v-stepper
    alt-labels
    v-model="step"
  >
    <v-stepper-header>
      <v-stepper-step step="1" :complete="step>1">
        Select Nodes
        <small v-if="sourceTypeId!==undefined & targetTypeId!==undefined">
          <span>{{nodeList[sourceTypeId].text }}</span>
          ->
          <span>{{ nodeList[targetTypeId].text }}</span>
        </small>
      </v-stepper-step>
      <v-divider></v-divider>
      <v-stepper-step step="2" :complete="step>2 ">
        Select Path
        <small v-if="selectedPath!==undefined && selectedPath.length>0"
               style="margin-left: -100px;margin-right: -100px">
          <span>{{ nodeList[sourceTypeId].text }}
          <span v-for="(edge,idx) in selectedPath" :key="'stepper_'+idx+'_'+edge.label">
            -> {{ getNodeLabel(edge.label, [edge.direction ? 1 : 0]) }}
          </span>
          </span>
        </small>
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
          max-height="90vh"
        >

          <v-card-subtitle class="headline">Node Configuration</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">Select the starting node type (e.g. Disorder) and then a target of interest (e.g Drug or Gene/Protein).
            Select specific start nodes of the selected type by using the auto-complete system or list upload.
            Manually adjust the list. <i>Optional:</i> Also specify the target nodes in the same way in case only
            specific connections might be of interest.</v-card-subtitle>

          <v-container style="height: 80%">
            <v-row style="height: 75vh">
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
                      <SuggestionAutocomplete :suggestion-type="suggestionType[0]" :index="0" :target-node-type="this.nodeList[[[this.sourceTypeId, this.targetTypeId][0]]].value" @addToSelectionEvent="addToSelection"></SuggestionAutocomplete>
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
                  </v-row>
                  <v-row>
                    <v-col>
                      <v-list max-height="20vh" height="20vh" class="overflow-y-auto">
                        <v-list-item v-for="(node,index) in sources" :key="node.id">
                          <v-list-item-title>{{ $utils.adjustLabels(node.displayName) }}</v-list-item-title>
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
                  <v-row>
                    <v-col>
                      <v-chip outlined v-show="sources.length>0" style="margin-top:15px"
                              @click="removeNonIntersecting(0)">
                        <v-icon left>fas fa-minus-square</v-icon>
                        Keep Intersection
                      </v-chip>
                      <v-chip outlined v-show="sources.length>0" style="margin-top:15px" @click="removeAll(0)">
                        <v-icon left>fas fa-trash-alt</v-icon>
                        Clear
                      </v-chip>
                      <v-chip outlined v-show="sources.length>0" style="margin-top:15px" @click="downloadList(0)">
                        <v-icon left>fas fa-download</v-icon>
                        Save
                      </v-chip>
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
                      <SuggestionAutocomplete :suggestion-type="suggestionType[1]" :index="1" :target-node-type="this.nodeList[[[this.sourceTypeId, this.targetTypeId][1]]].value" @addToSelectionEvent="addToSelection"></SuggestionAutocomplete>
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
                  <v-row>
                    <v-col>
                      <v-chip outlined v-show="targets.length>0" style="margin-top:15px"
                              @click="removeNonIntersecting(1)">
                        <v-icon left>fas fa-minus-square</v-icon>
                        Keep Intersection
                      </v-chip>
                      <v-chip outlined v-show="targets.length>0" style="margin-top:15px" @click="removeAll(1)">
                        <v-icon left>fas fa-trash-alt</v-icon>
                        Clear
                      </v-chip>
                      <v-chip outlined v-show="targets.length>0" style="margin-top:15px" @click="downloadList(1)">
                        <v-icon left>fas fa-download</v-icon>
                        Save
                      </v-chip>
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
          v-if="step===2"
          class="mb-12"
          max-height="75vh"
        >
          <v-card-subtitle class="headline">Path Selection</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">Select a path connecting {{nodeList[sourceTypeId].text +' and '+ nodeList[targetTypeId].text }}. Further decide to keep the intermediate node (in case an indirect path is selected) or to create a new edge type given a user defined name. Additional path specific configuration may be available.
          </v-card-subtitle>

          <v-container style="height: 80%">
            <v-row style="min-height: 35vh; margin-bottom: 15px">
              <v-col cols="3">
                <v-radio-group v-model="pathModel">
                  <v-list-item-subtitle class="title">Direct-Paths</v-list-item-subtitle>
                  <v-list>
                    <v-list-item v-for="(path,idx) in paths[0]" :key="idx" v-if="paths[0].length>0">
                      <v-list-item-title>
                        <v-tooltip top>
                          <template v-slot:activator="{on,attrs}">
                            <v-icon v-on="on" v-bind="attrs" :color="getColoring('nodes',nodeList[sourceTypeId].value,'light')"
                                    size="30px">
                              fas fa-genderless
                            </v-icon>
                          </template>
                          <span>{{ nodeList[sourceTypeId].text }}</span>
                        </v-tooltip>
                        <span v-for="(edge,idx2) in path" :key="'0_'+idx+'_'+idx2+'_'+edge.label">
                            <v-tooltip top>
                               <template v-slot:activator="{ on, attrs }">
                                  <v-icon v-bind="attrs"
                                          size="30px"
                                          v-on="on">{{
                                      edge.direction ? "fas fa-long-arrow-alt-right" : "fas fa-long-arrow-alt-left"
                                    }}</v-icon>
                               </template>
                              <span>{{ edge.label }}</span>
                            </v-tooltip>
                        <v-tooltip top>
                        <template v-slot:activator="{on,attrs}">
                          <v-icon v-on="on" v-bind="attrs"
                                  size="30px"
                                  :color="getColoring('edges',edge.label,'light')[edge.direction ? 1:0]">fas fa-genderless</v-icon>
                        </template>
                        <span>{{ getNodeLabel(edge.label, [edge.direction ? 1 : 0]) }}</span>
                      </v-tooltip>
                      </span>
                      </v-list-item-title>
                      <v-list-item-action>
                        <v-radio></v-radio>
                      </v-list-item-action>
                    </v-list-item>
                    <v-list-item v-if="paths[0].length===0">
                      <v-list-item-subtitle>no direct path available</v-list-item-subtitle>
                    </v-list-item>
                  </v-list>
                  <v-list-item-subtitle class="title">Indirect-Paths</v-list-item-subtitle>
                  <v-list>
                    <v-list-item v-for="(path,idx) in paths[1]" :key="idx">
                      <v-list-item-title>
                        <v-tooltip top>
                          <template v-slot:activator="{on,attrs}">
                            <v-icon v-on="on" v-bind="attrs" size="30px"
                                    :color="getColoring('nodes',nodeList[sourceTypeId].value,'light')">
                              fas fa-genderless
                            </v-icon>
                          </template>
                          <span>{{ nodeList[sourceTypeId].text }}</span>
                        </v-tooltip>
                        <span v-for="(edge,idx2) in path" :key="'1_'+idx+'_'+idx2+'_'+edge.label">
                            <v-tooltip top>
                               <template v-slot:activator="{ on, attrs }">
                                  <v-icon v-bind="attrs"
                                          size="30px"
                                          v-on="on">{{
                                      edge.direction ? "fas fa-long-arrow-alt-right" : "fas fa-long-arrow-alt-left"
                                    }}</v-icon>
                               </template>
                              <span>{{ edge.label }}</span>
                            </v-tooltip>
                        <v-tooltip top>
                        <template v-slot:activator="{on,attrs}">
                          <v-icon v-on="on" v-bind="attrs"
                                  size="30px"
                                  :color="getColoring('edges',edge.label,'light')[edge.direction ? 1:0]">fas fa-genderless</v-icon>
                        </template>
                        <span>{{ getNodeLabel(edge.label, [edge.direction ? 1 : 0]) }}</span>
                      </v-tooltip>
                      </span>
                      </v-list-item-title>
                      <v-list-item-action>
                        <v-radio>

                        </v-radio>
                      </v-list-item-action>
                    </v-list-item>
                  </v-list>
                </v-radio-group>
              </v-col>
              <v-divider vertical></v-divider>
              <v-col>
                <v-list-item-subtitle class="title">Additional Options</v-list-item-subtitle>
                <v-card-subtitle>General</v-card-subtitle>
                <v-row v-show="!direct">
                  <v-col cols="7">
                    <v-tooltip top>
                      <template v-slot:activator="{on,attrs}">
                        <v-list-item v-bind="attrs" v-on="on">
                          <span>Remove Intermediate Nodes</span>
                          <v-switch v-model="options.general.keep" style="margin-left: 5px"
                                    @click=" print(options.general.name) "></v-switch>
                          <span>Keep Intermediate Nodes</span>

                        </v-list-item>
                      </template>
                      <span>Decide if you want to keep all edges or replace the created paths by generating one connecting your source and target nodes directly.</span>
                    </v-tooltip>
                  </v-col>
                  <v-col cols="5">
                    <v-text-field v-model="options.general.name" v-show="!options.general.keep"
                                  label="Combined Edge Name"
                                  :rules="[value => !!value || 'Required!',value=>metagraph.edges.map(e=>e.label).indexOf(value)===-1 || 'Existing names are not possible!']"></v-text-field>
                  </v-col>
                </v-row>
                <v-divider></v-divider>
                <v-card-subtitle>Node specific</v-card-subtitle>
                <v-divider></v-divider>
                <v-card-subtitle>Edge specific</v-card-subtitle>
              </v-col>
            </v-row>
          </v-container>
        </v-card>
        <v-btn
          @click="makeStep(2,'back')"
        >
          Back
        </v-btn>
        <v-btn
          color="primary"
          @click="makeStep(2,'continue')"
          :disabled="(selectedPath === undefined || selectedPath.length === 0) || (!options.general.keep&&!direct && (options.general.name === undefined || options.general.name.length===0))"
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
          max-height="80vh"
        >
          <v-card-subtitle class="headline">3. Graph</v-card-subtitle>
          <v-card-subtitle style="margin-top: -25px">The network you created
          </v-card-subtitle>

          <v-row>
            <v-col cols="2">
              <v-card-title class="subtitle-1">Sources ({{ sources.length }})
              </v-card-title>
              <v-simple-table max-height="45vh" height="45vh" class="overflow-y-auto" fixed-header dense>
                <template v-slot:default>
                  <thead>
                  <tr>
                    <th class="text-left">
                      Name
                    </th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-for="node in sources" :key="node.id"
                      @click="focusNode(nodeList[sourceTypeId].value.substring(0,3)+'_'+node.id)"
                      :style="{'background-color': metagraph.colorMap[nodeList[sourceTypeId].value].light}">
                    <td>{{ $utils.adjustLabels(node.displayName) }}</td>
                  </tr>
                  </tbody>
                </template>
              </v-simple-table>
              <v-chip outlined style="margin-top:15px" @click="downloadList(0)">
                <v-icon left>fas fa-download</v-icon>
                Save Sources ({{ nodeList[sourceTypeId].text }})
              </v-chip>
            </v-col>
            <v-col cols="7">
              <Graph ref="graph" :configuration="graphConfig" :window-style="graphWindowStyle"
                     :legend="$refs.graph!==undefined && $refs.graph.isVisualized()" :meta="metagraph">
                <template v-slot:legend>
                  <v-card style="width: 11vw; max-width: 11vw; padding-top: 35px" v-if="info!==undefined">
                    <v-list>
                      <v-list-item v-for="node in Object.keys(info.nodes)" :key="node">
                        <v-list-item-icon>
                          <v-icon left :color="getColoring('nodes',node,'light')">fas fa-genderless</v-icon>
                        </v-list-item-icon>
                        <v-list-item-title style="margin-left: -25px">
                          {{ node.substr(0, 1).toUpperCase() + node.substr(1) }}
                        </v-list-item-title>
                        <v-list-item-subtitle>{{ info.nodes[node] }}</v-list-item-subtitle>
                      </v-list-item>
                    </v-list>
                  </v-card>
                </template>
              </Graph>
            </v-col>
            <v-col cols="3">
              <v-card-title class="subtitle-1"> Targets{{
                  (targets.length > 0 ? (" (" + (targets.length) + ")") : ": Processing")
                }}
                <v-progress-circular indeterminate v-if="targets.length===0" style="margin-left:15px">
                </v-progress-circular>
              </v-card-title>
              <template v-if="targets.length>=0">
                <v-simple-table max-height="45vh" height="45vh" class="overflow-y-auto" fixed-header dense>
                  <template v-slot:default>
                    <thead>
                    <tr>
                      <th class="text-left">
                        Name
                      </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="target in targets" :key="target.id"
                        @click="focusNode(nodeList[targetTypeId].value.substring(0,3)+'_'+target.id)"
                        :style="{'background-color': metagraph.colorMap[nodeList[targetTypeId].value].light}">
                      <td>{{ $utils.adjustLabels(target.displayName) }}</td>
                    </tr>
                    </tbody>
                  </template>
                </v-simple-table>
                <v-chip outlined style="margin-top:15px"
                        @click="downloadList(1)" v-if="targets.length>0">
                  <v-icon left>fas fa-download</v-icon>
                  Save Targets ({{ nodeList[targetTypeId].text }})
                </v-chip>
              </template>
            </v-col>
          </v-row>
          <v-divider style="margin-top:10px"></v-divider>
          <v-row>
            <v-col>
              <v-chip outlined v-if="gid!==undefined"
                      style="margin-top:10px"
                      @click="$emit('graphLoadEvent',{post: {id: gid}})">
                <v-icon left>fas fa-angle-double-right</v-icon>
                Load Result into Advanced View
              </v-chip>
            </v-col>
            <v-col>
              <v-switch label="Physics" v-model="graph.physics" @click="$refs.graph.setPhysics(graph.physics)"
                        v-if="$refs.graph!==undefined && $refs.graph.isVisualized()">
              </v-switch>
            </v-col>
          </v-row>
        </v-card>
        <v-btn style="margin-bottom: 10px"
          @click="makeStep(3,'back')"
        >
          Back
        </v-btn>
        <v-btn style="margin-bottom: 10px" text @click="makeStep(3,'cancel')">
          Restart
        </v-btn>
      </v-stepper-content>
    </v-stepper-items>
  </v-stepper>
</template>

<script>
import Graph from "../graph/Graph";
import SuggestionAutocomplete from "@/components/app/suggestions/SuggestionAutocomplete";

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
      graphConfig: {visualized: false},

      sources: [],
      targets: [],
      nodeOrigins: [{}, {}],
      fileInputModel: undefined,

      sourceTypeId: undefined,
      targetTypeId: undefined,
      step: 1,
      nodeList: [],
      nodeIdTypeList: [],
      selectedPath: [],

      suggestionType: [undefined, undefined],
      pathModel: -1,

      gid: undefined,

      graph: {
        physics: false,
      },

      info: undefined,
      direct: false,

      paths: {0: [], 1: []},

      options: {
        general: {
          keep: false,
          name: undefined,
        },
        nodes: {},
        edges: {}
      }
    }
  },

  created() {
    this.uid = this.$cookies.get("uid")
    this.metagraph.nodes.forEach((n, index) => {
      this.nodeList.push({id: index, value: n.group, text: n.label})
      this.nodeIdTypeList.push(this.metagraph.data[n.label])
    })
    this.init()
  },

  watch: {

    pathModel: function (val) {
      if (val < this.paths[0].length) {
        this.selectedPath = this.paths[0][val]
        this.direct = true
        this.options.general.keep = true
      } else {
        this.selectedPath = this.paths[1][val - this.paths[0].length]
        this.direct = false
        this.options.general.keep = false
      }
    }


  },

  methods: {


    init: function () {
      this.step = 1
      this.sugQuery = [undefined, undefined]


      this.sourceTypeId = undefined
      this.targetTypeId = undefined
      this.sources = []
      this.targets = []
      this.nodeOrigins = [{}, {}]
      this.suggestionType = [undefined, undefined]

      this.selectedPath = []
      this.pathModel = -1
      this.clearPaths()

      this.options.general.keep = false
      this.options.general.name = undefined

      this.info = undefined

    },
    reset: function (){
      this.init()
    },

    print: function (message) {
      console.log(message)
    },

    clearPaths: function () {

      this.paths[0] = []
      this.paths[1] = []
    },

    getSuggestionSelection: function (index) {
      let type = this.nodeList[[this.sourceTypeId, this.targetTypeId][index]].value
      let selfAdded = false;
      let nodeId = this.metagraph.nodes.filter(n => n.group === type)[0].id
      let typeList = this.metagraph.edges.filter(e => e.from !== e.to && e.from === nodeId || e.to === nodeId).map(e => e.to === nodeId ? e.from : e.to).map(nid => {
        let node = this.metagraph.nodes.filter(n => n.id === nid)[0]
        if (node.group === type)
          selfAdded = true
        return {value: node.group, text: node.label}
      })
      if (!selfAdded)
        typeList.push({value: type, text: this.nodeList[[this.sourceTypeId, this.targetTypeId][index]].text})
      return typeList
    },

    generatePaths: function () {
      let sourceId = this.metagraph.nodes[this.sourceTypeId].id + ""
      let targetId = this.metagraph.nodes[this.targetTypeId].id + ""
      this.metagraph.edges.forEach(e1 => {
        if (e1.to === sourceId || e1.from === sourceId) {
          let i1 = (e1.to === sourceId) ? e1.from : e1.to
          if (i1 === targetId)
            this.paths[0].push([{label: e1.label, direction: e1.from === sourceId}])
          else
            this.metagraph.edges.forEach(e2 => {
              if (e2.to === i1 || e2.from === i1) {
                let i2 = (e2.to === i1) ? e2.from : e2.to
                if (i2 === targetId)
                  this.paths[1].push([{label: e1.label, direction: e1.from === sourceId}, {
                    label: e2.label,
                    direction: e2.to === targetId
                  }])
              }
            })
        }
      })
    },

    focusNode: function (id) {
      if (this.$refs.graph === undefined)
        return
      this.$refs.graph.setSelection([id])
      this.$refs.graph.zoomToNode(id)
    },

    submitGraphGeneration: function () {
      this.$http.post("/getGuidedGraph", {
        uid: this.$cookies.get("uid"),
        sourceType: this.nodeList[this.sourceTypeId].value,
        targetType: this.nodeList[this.targetTypeId].value,
        sources: this.sources.map(n => n.id),
        targets: this.targets.map(n => n.id),
        path: this.selectedPath,
        params: this.options,
      }).then(response => {
        if (response.data !== undefined) {
          return response.data
        }
      }).then(data => {
        this.info = data;
        this.gid = data.id
        this.$refs.graph.show(this.gid)
        this.loadTargetTable(this.gid)
      }).catch(console.log)
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
      this.$utils.readFile(file).then(content => {
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
      this.$emit("printNotificationEvent", "Added " + list.length + "from " + nameFrom + " (" + count + " new) nodes!", 1)
    }
    ,
    loadTargetTable: function (gid) {
      let groupName = this.nodeList[this.targetTypeId].value
      return this.$http.get("/getGraphList?id=" + gid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.targets = data.nodes[groupName].map(n => {
          return {id: n.id, displayName: n.displayName}
        })
      }).catch(console.log)
    },

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
    },
    removeNonIntersecting: function (index) {
      let remove = []
      let seedOrigin = this.nodeOrigins[index]
      let seeds = this[["sources", "targets"][index]]
      Object.keys(seedOrigin).forEach(seed => {
        if (seedOrigin[seed] === undefined || seedOrigin[seed].length < 2) {
          seedOrigin[seed] = undefined
          remove.push(parseInt(seed))
        }
      })
      this[["sources", "targets"][index]] = seeds.filter(s => remove.indexOf(s.id) === -1)
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
    },

    makeStep: function (s, button) {
      if (button === "continue") {
        if(this.step===1)
          this.$emit("clearURLEvent")
        this.step++
        if (this.step === 2)
          this.generatePaths()
      }
      if (button === "back") {
        this.step--
        if (this.step === 2) {
          if (this.$refs.graph !== undefined)
            this.$refs.graph.reload()
          this.info = undefined
        }
        if (this.step === 1)
          this.clearPaths()
      }

      if (button === "cancel") {
        this.step = 1
        if (this.$refs.graph !== undefined)
          this.$refs.graph.reload()
        this.init()
      }
      if (this.step === 3)
        this.submitGraphGeneration()
    },

    getColoring: function (entity, name,style) {
      return this.$utils.getColoring(this.metagraph, entity, name,style);
    },

    getNodeLabel: function (name, idx) {
      let id = this.$utils.getNodes(this.metagraph, name)[idx]
      return id.substring(0, 1).toUpperCase() + id.substring(1)
    }

  },
  components: {
    SuggestionAutocomplete,
    Graph,
  }

}
</script>

<style scoped>

</style>
