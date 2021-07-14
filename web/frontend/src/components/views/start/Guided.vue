<template>
  <v-card style="margin-bottom: 35px">
    <div style="display: flex; color: dimgray; padding-bottom: 8px; padding-top: 25px">
      <v-card-title style="font-size: 2.5em; justify-content: center; margin-left: auto; margin-right: auto">
        Guided Exploration
      </v-card-title>
    </div>
    <v-card-subtitle> Use the <b>Guided Exploration</b> to create a network based on specific <b><i>start</i></b> and
      <b><i>target</i></b> nodes types.
      Select a <b><i>path</i></b> connecting these metanodes and control the result through additional parameters.
    </v-card-subtitle>
    <v-stepper
      alt-labels
      v-model="step"
      flat
    >
      <v-stepper-header>
        <v-stepper-step step="1" :complete="step>1">
          Select Nodes
          <small v-if="sourceTypeId!==undefined & targetTypeId!==undefined">
            <span>{{ nodeList[sourceTypeId].text }}</span>
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
          Network
        </v-stepper-step>
      </v-stepper-header>

      <v-stepper-items>
        <v-stepper-content step="1">
          <v-card
            v-if="step===1"
            class="mb-4"
            max-height="95vh"
            height="95vh"
          >

            <v-card-subtitle class="headline">Node Configuration</v-card-subtitle>
            <v-card-subtitle style="margin-top: -25px">Select the starting node type (e.g. Disorder) and then a target
              of
              interest (e.g Drug or Gene/Protein).
              Select specific start nodes of the selected type by using the auto-complete system or list upload.
              Manually adjust the list. <i>Optional:</i> Also specify the target nodes in the same way in case only
              specific connections might be of interest.
            </v-card-subtitle>

            <v-container style="height: 80%">
              <div style="height: 75vh; display: flex">
                <div style="justify-self: flex-start; width: 48%;">
                  <div style="display: flex; justify-content: flex-start;">
                    <div class="title" style="padding-top: 16px;">1a. Select the source node type:</div>
                    <v-select item-value="id" :items="nodeList" v-model="sourceTypeId"
                              placeholder="Select start Node"
                              :disabled="$refs.sourceTable!=null && $refs.sourceTable.getSeeds().length>0"
                              style="min-width: 9.5em; max-width: 11.2em; margin-left: 25px">
                    </v-select>
                  </div>
                  <div v-if="sourceTypeId!==undefined" style="margin-top: 25px;">
                    <div style="display: flex">
                      <div class="title" style="padding-top: 16px;justify-self: flex-start">1b. Prefilter the sources:
                      </div>
                    </div>
                    <div style="display: flex">

                      <v-select :items="getSuggestionSelection(0)" v-model="suggestionType[0]"
                                placeholder="connected to" style="width: 35%; justify-self: flex-start"></v-select>
                      <SuggestionAutocomplete :suggestion-type="suggestionType[0]" :index="0"
                                              :target-node-type="this.nodeList[[[this.sourceTypeId, this.targetTypeId][0]]].value"
                                              @addToSelectionEvent="addToSelection"
                                              style="justify-self: flex-end;margin-left: auto"></SuggestionAutocomplete>
                    </div>
                    <v-card-subtitle style="margin-left: -10px">or</v-card-subtitle>
                    <div
                      style="justify-content: center; display: flex; width: 100%; margin-bottom: 25px; margin-left: -10px">
                      <v-file-input :label="'by '+nodeIdTypeList[sourceTypeId]+' ids'"
                                    v-on:change="onSourceFileSelected"
                                    show-size
                                    prepend-icon="far fa-list-alt"
                                    v-model="fileInputModel[0]"
                                    dense
                                    style="width: 97%; max-width: 97%"
                      ></v-file-input>
                    </div>
                    <SeedTable ref="sourceTable" v-if="sourceTypeId!==undefined" :download="true" :remove="true"
                               @printNotificationEvent="printNotification"
                               height="30vh"
                               :title="'Source nodes ('+($refs.sourceTable ? $refs.sourceTable.getSeeds().length : 0)+')'"
                               :nodeName="nodeList[sourceTypeId].value"></SeedTable>
                  </div>
                </div>
                <div style="justify-self: center; margin-left: auto">
                  <v-divider vertical></v-divider>
                </div>
                <div style="justify-self: flex-end; margin-left: auto; width: 48%;">
                  <div style="display: flex; justify-content: flex-start;">
                    <div class="title" style="padding-top: 16px;">2a. Select the target node type:</div>
                    <v-select item-value="id" :items="nodeList" v-model="targetTypeId"
                              placeholder="Select target Node"
                              :disabled="$refs.targetTable!=null && $refs.targetTable.getSeeds().length>0"
                              style="min-width: 9.5em; max-width: 11.2em; margin-left: 25px">
                    </v-select>
                  </div>
                  <div v-if="targetTypeId!==undefined" style="margin-top: 25px;">
                    <div style="display: flex">
                      <div class="title" style="padding-top: 16px;justify-self: flex-start">2b. Prefilter the targets
                        (optional):
                      </div>
                    </div>
                    <div style="display: flex">

                      <v-select :items="getSuggestionSelection(1)" v-model="suggestionType[1]"
                                placeholder="connected to" style="width: 35%; justify-self: flex-start"></v-select>
                      <SuggestionAutocomplete :suggestion-type="suggestionType[1]" :index="1"
                                              :target-node-type="this.nodeList[[this.targetTypeId]].value"
                                              @addToSelectionEvent="addToSelection"
                                              style="justify-self: flex-end;margin-left: auto"></SuggestionAutocomplete>
                    </div>
                    <v-card-subtitle style="margin-left: -10px">or</v-card-subtitle>
                    <div
                      style="justify-content: center; display: flex; width: 100%; margin-bottom: 25px; margin-left: -10px">
                      <v-file-input :label="'by '+nodeIdTypeList[targetTypeId]+' ids'"
                                    v-on:change="onTargetFileSelected"
                                    show-size
                                    prepend-icon="far fa-list-alt"
                                    v-model="fileInputModel[1]"
                                    dense
                                    style="width: 97%; max-width: 97%"
                      ></v-file-input>
                    </div>
                    <SeedTable ref="targetTable" v-if="targetTypeId!==undefined" :download="true" :remove="true"
                               @printNotificationEvent="printNotification"
                               height="30vh"
                               :title="'Target nodes ('+($refs.targetTable ? $refs.targetTable.getSeeds().length : 0)+')'"
                               :nodeName="nodeList[targetTypeId].value"></SeedTable>
                  </div>
                </div>
              </div>
            </v-container>
          </v-card>
          <v-btn
            color="primary"
            @click="makeStep(1,'continue')"
            :disabled="sourceTypeId===undefined || targetTypeId===undefined  ||  ($refs.sourceTable &&$refs.sourceTable.getSeeds().length<1)"
          >
            Continue
          </v-btn>

          <v-btn text @click="makeStep(1,'cancel')">
            Reset
          </v-btn>
        </v-stepper-content>
        <v-stepper-content step="2">
          <v-card
            v-if="step===2"
            class="mb-4"
            max-height="75vh"
          >
            <v-card-subtitle class="headline">Path Selection</v-card-subtitle>
            <v-card-subtitle style="margin-top: -25px">Select a path connecting
              {{ nodeList[sourceTypeId].text + ' and ' + nodeList[targetTypeId].text }}. Further decide to keep the
              intermediate node (in case an indirect path is selected) or to create a new edge type given a user defined
              name. Additional path specific configuration may be available.
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
                              <v-icon v-on="on" v-bind="attrs"
                                      :color="getColoring('nodes',nodeList[sourceTypeId].value,'light')"
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
                                    :rules="[value => !!value || 'Required!',value=>$global.metagraph.edges.map(e=>e.label).indexOf(value)===-1 || 'Existing names are not possible!']"></v-text-field>
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
            class="mb-4"
            max-height="80vh"
          >
            <v-card-subtitle class="headline">3. Network</v-card-subtitle>
            <v-card-subtitle style="margin-top: -25px">The network you created
            </v-card-subtitle>

            <v-row>
              <v-col cols="2" style="padding: 0">
                <v-card-title class="subtitle-1">Sources ({{ sources.length }})
                </v-card-title>
                <v-data-table max-height="50vh" height="50vh" class="overflow-y-auto" fixed-header dense item-key="id"
                              :items="sources"
                              :headers="getHeaders()"
                              disable-pagination
                              hide-default-footer @click:row="seedClicked">
                  <template v-slot:item.displayName="{item}">
                    <v-tooltip v-if="item.displayName.length>30" right>
                      <template v-slot:activator="{attr,on }">
                          <span v-bind="attr" v-on="on"
                                style="color: dimgray">{{ item.displayName.substr(0, 30) }}...</span>
                      </template>
                      <span>{{ item.displayName }}</span>
                    </v-tooltip>
                    <span v-else>{{ item.displayName }}</span>
                  </template>
                  <template v-slot:footer>
                    <div style="display: flex; justify-content: center; margin-left: auto">
                      <div style="padding-top: 16px">
                        <ResultDownload v-show="sources !=null && sources.length>0" seeds
                                        @downloadEvent="downloadSourceList"></ResultDownload>
                      </div>
                    </div>
                  </template>
                </v-data-table>
              </v-col>
              <v-col>
                <Network ref="graph" :configuration="graphConfig" :window-style="graphWindowStyle"
                         :legend="$refs.graph!==undefined" :tools="$refs.graph!==undefined" secondaryViewer="true"
                         @loadIntoAdvancedEvent="$emit('graphLoadEvent',{post: {id: gid}})">
                  <template v-slot:legend>
                    <v-card style="width: 13vw; max-width: 13vw; padding-top: 35px" v-if="info!==undefined">
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
                  <template v-slot:tools v-if="$refs.graph!==undefined">
                    <v-card elevation="3" style="width: 13vw; max-width: 13vw; padding-top: 35px">
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
                                  <v-chip outlined v-if="gid!==undefined"
                                          style="margin-top:10px"
                                          @click="$emit('graphLoadNewTabEvent',{post: {id: gid}})">
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
              <v-col cols="2" style="padding: 0 10px 0 0">
                <v-card-title class="subtitle-1"> Targets{{
                    (targets.length > 0 ? (" (" + (targets.length) + ")") : ": Processing")
                  }}
                  <v-progress-circular indeterminate v-if="targets.length===0" style="margin-left:15px">
                  </v-progress-circular>
                </v-card-title>
                <template v-if="targets.length>=0">
                  <v-data-table max-height="50vh" height="50vh" class="overflow-y-auto" fixed-header dense item-key="id"
                                :items="targets"
                                :headers="getHeaders()"
                                disable-pagination
                                hide-default-footer @click:row="targetClicked">
                    <template v-slot:item.displayName="{item}">
                      <v-tooltip v-if="item.displayName.length>30" right>
                        <template v-slot:activator="{attr,on }">
                          <span v-bind="attr" v-on="on"
                                style="color: dimgray">{{ item.displayName.substr(0, 30) }}...</span>
                        </template>
                        <span>{{ item.displayName }}</span>
                      </v-tooltip>
                      <span v-else>{{ item.displayName }}</span>
                    </template>
                    <template v-slot:footer>
                      <div style="display: flex; justify-content: center; margin-left: auto">
                        <div style="padding-top: 16px">
                          <ResultDownload v-show="targets !=null && targets.length>0" seeds
                                          @downloadEvent="downloadTargetList"></ResultDownload>
                        </div>
                      </div>
                    </template>
                  </v-data-table>
                </template>
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
  </v-card>
</template>

<script>
import Network from "../graph/Network";
import SuggestionAutocomplete from "@/components/app/suggestions/SuggestionAutocomplete";
import SeedTable from "@/components/app/tables/SeedTable";
import ResultDownload from "@/components/app/tables/menus/ResultDownload";


export default {
  name: "Guided",

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
      fileInputModel: [undefined, undefined],

      sourceTypeId: undefined,
      targetTypeId: undefined,
      step: 1,
      nodeList: [],
      nodeIdTypeList: [],
      selectedPath: [],
      physicsOn: false,

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
    this.$global.metagraph.nodes.forEach((n, index) => {
      this.nodeList.push({id: index, value: n.group, text: n.label})
      this.nodeIdTypeList.push(this.$global.metagraph.data[n.label])
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
    reset: function () {
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
      let nodeId = this.$global.metagraph.nodes.filter(n => n.group === type)[0].id
      let typeList = this.$global.metagraph.edges.filter(e => e.from !== e.to && e.from === nodeId || e.to === nodeId).map(e => e.to === nodeId ? e.from : e.to).map(nid => {
        let node = this.$global.metagraph.nodes.filter(n => n.id === nid)[0]
        if (node.group === type)
          selfAdded = true
        return {value: node.group, text: node.label}
      })
      if (!selfAdded)
        typeList.push({value: type, text: this.nodeList[[this.sourceTypeId, this.targetTypeId][index]].text})
      return typeList
    },

    generatePaths: function () {
      let sourceId = this.$global.metagraph.nodes[this.sourceTypeId].id + ""
      let targetId = this.$global.metagraph.nodes[this.targetTypeId].id + ""
      this.$global.metagraph.edges.forEach(e1 => {
        if (e1.to === sourceId || e1.from === sourceId) {
          let i1 = (e1.to === sourceId) ? e1.from : e1.to
          if (i1 === targetId)
            this.paths[0].push([{label: e1.label, direction: e1.from === sourceId}])
          else
            this.$global.metagraph.edges.forEach(e2 => {
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
        this.$refs.graph.loadNetworkById(this.gid)
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
          type: this.nodeList[[this.sourceTypeId, this.targetTypeId][index]].value,
          file: content
        }).then(response => {
          if (response.data)
            return response.data
        }).then(data => {
          this.addToSelection(data, index, "FILE:" + file.name)
        }).then(() => {
          this.$set(this.fileInputModel, index, undefined)
        }).catch(console.log)
      }).catch(console.log)
    },

    addToSelection: function (list, index, nameFrom) {
      this.$refs[["sourceTable", "targetTable"][index]].addSeeds(list, nameFrom)
      // let table = this[["sources", "targets"][index]]
      // let ids = table.map(n => n.id)
      // let count = 0
      // let origins = this.nodeOrigins[index]
      // list.forEach(e => {
      //   if (ids.indexOf(e.id) === -1) {
      //     count++
      //     table.push(e)
      //   }
      //
      //
      //   if (origins[e.id] !== undefined) {
      //     if (origins[e.id].indexOf(nameFrom) === -1)
      //       origins[e.id].push(nameFrom)
      //   } else
      //     origins[e.id] = [nameFrom]
      //
      // })
      // this.$emit("printNotificationEvent", "Added " + list.length + "from " + nameFrom + " (" + count + " new) nodes!", 1)
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
        return this.nodeOrigins[index][id].map(item => {
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
    },
    printNotification: function (message, type) {
      this.$emit("printNotificationEvent", message, type)
    }
    ,
    removeNode: function (id, index) {
      let idx = this[["sources", "targets"][index]].map(e => e.id).indexOf(id)
      this[["sources", "targets"][index]].splice(idx, 1)
      delete this.nodeOrigins[index][id]
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

    downloadSourceList: function (names, sep) {
      this.downloadList(0, names, sep)
    },
    downloadTargetList: function (names, sep) {
      this.downloadList(1, names, sep)
    },
    downloadList: function (index, names, sep) {
      let nodeType = this.nodeList[[this.sourceTypeId, this.targetTypeId][index]].value
      this.$http.post("mapToDomainIds", {
        type: nodeType,
        ids: [this.sources, this.targets][index].map(n => n.id)
      }).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        let text = "#ID" + (names ? sep + "Name" : "") + "\n";
        let dlName = ["gene", "protein"][this.seedTypeId] + "_seeds." + (!names ? "list" : (sep === '\t' ? "tsv" : "csv"))
        if (!names) {
          Object.values(data).forEach(id => text += id + "\n")
        } else {
          [this.sources, this.targets][index].forEach(s => text += data[s.id] + sep + s.displayName + "\n")
        }
        this.download(dlName, text)
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

    makeStep: async function (s, button) {
      if (button === "continue") {
        if (this.step === 1) {
          this.$emit("clearURLEvent")
          await this.$nextTick(() => {
            this.sources = this.$refs.sourceTable.getSeeds()
            this.targets = this.$refs.targetTable.getSeeds()
          })
        }
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
        this.$emit('printNotificationEvent', "Reset of the Guided Exploration pipeline successfull!", 1)
      }
      if (this.step === 3)
        this.submitGraphGeneration()
    },
    getHeaders: function () {
      return [{text: "Name", align: "start", sortable: true, value: "displayName"}]
    },
    seedClicked: function (item) {
      this.focusNode(this.nodeList[this.sourceTypeId].value.substring(0, 3) + '_' + item.id)
    },
    targetClicked: function (item) {
      this.focusNode(this.nodeList[this.targetTypeId].value.substring(0, 3) + '_' + item.id)
    },

    getColoring: function (entity, name, style) {
      return this.$utils.getColoring(this.$global.metagraph, entity, name, style);
    },

    getNodeLabel: function (name, idx) {
      let id = this.$utils.getNodes(this.$global.metagraph, name)[idx]
      return id.substring(0, 1).toUpperCase() + id.substring(1)
    }

  }
  ,
  components: {
    SuggestionAutocomplete,
    Network,
    SeedTable,
    ResultDownload,
  }

}
</script>

<style scoped>

</style>
