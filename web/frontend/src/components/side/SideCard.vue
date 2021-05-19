<template>
  <v-container style="position: fixed; width: 20%">

    <v-card elevation="3" style="padding-top: 15px; overflow-y: auto; max-height: 80vh">
      <v-card-title>Toolbox</v-card-title>

      <v-card-title v-show="gid!==undefined">
        <v-chip
          outlined
          @click="requestGraphDownload"
        >
          <v-icon
            left
          >
            far fa-arrow-alt-circle-down
          </v-icon>
          Download
        </v-chip>
      </v-card-title>

      <v-card ref="options" elevation="3" style="margin:15px">
        <v-list-item @click="show.options=!show.options">
          <v-list-item-title>
            <v-icon left>{{ show.options ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
            Options
          </v-list-item-title>
        </v-list-item>
        <v-divider></v-divider>
        <v-list>
          <v-container v-if="show.options">
            <template v-if="selectedTab===0">
              <template v-if="options.start!==undefined">

                <v-list-item>
                  <v-col>
                    <v-switch v-model="options.start.onlyConnected" label="Hide unconnected"></v-switch>
                  </v-col>
                </v-list-item>
                <v-list-item>
                  <v-col>
                    <v-switch v-model="options.start.skipVis" label="Skip visualisation"></v-switch>
                  </v-col>
                </v-list-item>
                <v-list-item>
                  <v-container>
                    <v-row>
                      <v-col>
                        <v-chip outlined color="green" @click="$emit('applyEvent',true); $emit('applyEvent',false)">
                          Apply Subgraph
                          <v-icon right>far fa-check-circle</v-icon>
                        </v-chip>
                      </v-col>
                      <v-col>
                        <v-chip outlined color="red" @click="$emit('applyEvent',false)">
                          Reset
                          <v-icon right>far fa-trash-alt</v-icon>
                        </v-chip>
                      </v-col>
                    </v-row>
                  </v-container>
                </v-list-item>
              </template>
              <v-progress-circular v-else>
              </v-progress-circular>
            </template>
            <template v-if="selectedTab===1">
              <template v-if="options.graph.visualized">
                <v-list-item>
                  <v-chip outlined v-on:click="setAllSelected()">
                    <v-icon left>fas fa-globe</v-icon>
                    Overview
                  </v-chip>
                </v-list-item>
                <v-list-item>
                  <v-switch
                    v-model="options.graph.physics"
                    @click="$emit('updatePhysicsEvent')"
                    label="Enable physics"
                    :disabled="options.graph.noPhysics"
                  >
                  </v-switch>
                </v-list-item>
                <v-list-item>
                  <v-switch
                    v-model="options.graph.loops"
                    @click="$emit('showLoopsEvent',options.graph.loops)"
                    label="Show loops"
                  >
                  </v-switch>
                </v-list-item>
                <v-list-item>
                  <v-switch
                    v-model="options.graph.single"
                    :disabled="options.graph.physics"
                    @click="$emit('showUnconnectedEvent',options.graph.single)"
                    label="Show Unconnected"
                  >
                  </v-switch>
                </v-list-item>
                <v-list-item>
                  <v-switch
                    v-model="options.graph.component"
                    :disabled="selectedNode===undefined && !options.graph.component"
                    @click="$emit('graphViewEvent',{event:'isolate',selected:selectedNode.id, state:options.graph.component})"
                    label="Isolate Component"
                  >
                  </v-switch>
                </v-list-item>
              </template>
              <template v-else>
                <v-chip outlined @click="visualizeGraph">
                  <v-icon left>fas fa-check</v-icon>
                  Visualize Graph
                </v-chip>
              </template>
            </template>
            <template v-if="selectedTab===2">
              <v-list-item>
                <v-switch
                  v-model="options.list.showAll"
                  @click="$emit('reloadTablesEvent')"
                  :label="'Show all Items ('+options.list.selected+'/'+options.list.total+')'">
                  >
                </v-switch>
              </v-list-item>
              <v-list-item>
                <v-row>
                  <v-col>
                    <v-chip
                      outlined
                      v-on:click="$emit('selectionEvent','all','none')"
                    >
                      <v-icon left color="red">fas fa-trash</v-icon>
                      Unselect All
                    </v-chip>
                  </v-col>
                  <v-col>
                    <v-chip
                      outlined
                      v-on:click="$emit('graphModificationEvent','subselect');$forceUpdate"
                    >
                      <v-icon left color="green">fas fa-project-diagram</v-icon>
                      Load Selection
                    </v-chip>
                  </v-col>
                </v-row>
              </v-list-item>
              <v-tabs
                fixed-tabs
                v-model="menu.options.list.tab"
              >
                <v-tabs-slider></v-tabs-slider>
                <v-tab v-for="tab in menu.options.list.tabs" class="primary--text" :key=tab.id>
                  {{ tab.label }}
                </v-tab>
              </v-tabs>
              <template v-if="menu.options.list.tab===0" style="margin-top:10px">
                <v-list-item>
                  <v-chip
                    v-on:click="$emit('graphModificationEvent','extend')"
                    class="pa-3"
                    outlined
                  >
                    <v-icon left>fas fa-plus-circle</v-icon>
                    Extend Graph
                  </v-chip>
                </v-list-item>
                <v-list-item>
                  <v-chip
                    v-on:click="$emit('graphModificationEvent','collapse')"
                    class="pa-3"
                    outlined
                  >
                    <v-icon left>fas fa-compress-alt</v-icon>
                    Infer new edge
                  </v-chip>
                </v-list-item>
                <v-list-item>
                  <v-chip
                    v-on:click="$emit('colorSelectionEvent')"
                    class="pa-3"
                    outlined
                  >
                    <v-icon left>fas fa-palette</v-icon>
                    New group from selection
                  </v-chip>
                </v-list-item>
              </template>
              <template v-if="menu.options.list.tab===1">
                <v-list-item>
                  <v-chip
                    icon
                    outlined
                    v-on:click="$emit('selectionEvent','nodes','all')"
                  >
                    <v-icon left>fas fa-check-double</v-icon>
                    Select All Nodes
                  </v-chip>
                </v-list-item>
                <v-list-item>
                  <v-chip
                    icon
                    outlined
                    v-on:click="$emit('selectionEvent','nodes','induced')"
                  >
                    <v-icon left>fas fa-check</v-icon>
                    Select Induced
                  </v-chip>
                </v-list-item>
                <v-list-item>
                  <v-chip
                    icon
                    outlined
                    v-on:click="$emit('selectionEvent','nodes','none')"
                  >
                    <v-icon left>fas fa-ban</v-icon>
                    Unselect All Nodes
                  </v-chip>
                </v-list-item>
              </template>
              <template v-if="menu.options.list.tab===2">
                <v-list-item>
                  <v-chip
                    icon
                    outlined
                    v-on:click="$emit('selectionEvent','edges','all')"
                  >
                    <v-icon left>fas fa-check-double</v-icon>
                    Select All Edges
                  </v-chip>
                </v-list-item>
                <v-list-item>
                  <v-chip
                    icon
                    outlined
                    v-on:click="$emit('selectionEvent','edges','none')"
                  >
                    <v-icon left>fas fa-ban</v-icon>
                    Unselect All Edges
                  </v-chip>
                </v-list-item>
              </template>
            </template>
            <template v-if="selectedTab===3">
              <v-list-item>
                <v-switch v-model="options.history.favos" label="Favourites Only"
                          @click="$forceUpdate; $emit('historyReloadEvent')">
                </v-switch>

              </v-list-item>
              <v-list-item>
                <v-switch v-model="options.history.chronological" label="Show Chronological"
                          @click="$forceUpdate(); $emit('historyReloadEvent')"></v-switch>
              </v-list-item>
              <v-list v-show="options.history.chronological">
                <v-list-item>
                  <v-switch v-model="options.history.otherUsers" label="Show parent graphs of other users"
                            @click="$emit('historyReloadEvent')"></v-switch>
                </v-list-item>
                <v-list-item>
                  <v-chip outlined @click="$emit('reverseSortingEvent')">Reverse Sorting</v-chip>
                </v-list-item>
              </v-list>
            </template>
          </v-container>
        </v-list>
      </v-card>

<!--      <v-card ref="filters" elevation="3" style="margin:15px" v-if="selectedTab===0">-->

<!--        <v-list-item @click="show.filters=!show.filters">-->
<!--          <v-list-item-title>-->
<!--            <v-icon left>{{ show.filters ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>-->
<!--            Filters-->
<!--          </v-list-item-title>-->
<!--        </v-list-item>-->
<!--        <v-divider></v-divider>-->

<!--        <v-container v-show="show.filters">-->
<!--          <v-row>-->
<!--            <v-col>-->
<!--              <v-list-item>-->
<!--                <v-chip outlined-->
<!--                        @click="show.filterAdd=!show.filterAdd; show.filterSelectDisabled=false; filterEntity=''">-->
<!--                  <v-icon left>{{ show.filterAdd ? "fas fa-check" : "fas fa-filter" }}</v-icon>-->
<!--                  {{ show.filterAdd ? "Done" : "Edit Filters" }}-->
<!--                </v-chip>-->
<!--              </v-list-item>-->
<!--            </v-col>-->
<!--            <v-col>-->
<!--              <v-select v-if="show.filterAdd "-->
<!--                        v-model="filterEntity"-->
<!--                        :items="options.start.selectedElements.filter(e=>e.type==='node')"-->
<!--                        :autofocus="show.filterAdd"-->
<!--                        item-text="name"-->
<!--                        item-value="name"-->
<!--                        @focusout="show.filterSelectDisabled=true"-->
<!--                        :disabled="show.filterSelectDisabled"-->
<!--                        @change="setFiltering"-->
<!--              >Select Entity-->
<!--              </v-select>-->
<!--            </v-col>-->
<!--          </v-row>-->
<!--          <v-simple-table fixed-header ref="filterTable" v-if="filterEntity.length>0">-->
<!--            <template v-slot:default>-->
<!--              <thead>-->
<!--              <tr>-->
<!--                <th class="text-center">Type</th>-->
<!--                <th class="text-center">Filter</th>-->
<!--                <th class="text-center">Operation</th>-->
<!--              </tr>-->
<!--              </thead>-->
<!--              <tbody>-->
<!--              <tr v-for="(item,index) in filters[filterEntity]" :key="item.type+item.expression">-->
<!--                <td>{{ item.type }}</td>-->
<!--                <td>{{ item.expression }}</td>-->
<!--                <td>-->
<!--                  <v-chip outlined v-on:click="removeFilter(index)">-->
<!--                    <v-icon dense>fas fa-trash</v-icon>-->
<!--                  </v-chip>-->
<!--                </td>-->
<!--              </tr>-->
<!--              <tr v-if="show.filterAdding">-->
<!--                <td>-->
<!--                  <v-select-->
<!--                    v-model="filterTypeModel"-->
<!--                    :items="filterTypes"-->
<!--                    label="type"-->
<!--                  ></v-select>-->
<!--                </td>-->
<!--                <td>-->
<!--                  <v-text-field-->
<!--                    v-model="filterModel"-->
<!--                    :label="filterLabel"-->
<!--                    placeholder="Pattern"-->
<!--                  ></v-text-field>-->
<!--                </td>-->
<!--                <td>-->
<!--                  <v-chip outlined v-on:click="saveFilter"-->
<!--                          :disabled="filterModel ===undefined|| filterModel.length ===0 ||filterTypeModel ===undefined">-->
<!--                    <v-icon dense>fas fa-plus</v-icon>-->
<!--                  </v-chip>-->
<!--                </td>-->
<!--              </tr>-->
<!--              </tbody>-->
<!--            </template>-->
<!--          </v-simple-table>-->
<!--        </v-container>-->
<!--      </v-card>-->
      <template v-if="(selectedTab===1 && options.graph.visualized)" :options="options.graph.selection">
        <Selection ref="selection" :options="options.graph.selection"
                   @selectModeEvent="toggleSelectMode"
                   @nodeSelectionEvent="loadSelection"
                  @applyMultiSelect="applyMultiSelect"
        >
        </Selection>

        <!--        <Legend :metagraph="metagraph" :countMap="options.list.countMap" :entity-graph="options.list.entityGraph"-->
        <!--                :options="options.graph.legend"-->
        <!--                @graphViewEvent="graphViewEvent"-->
        <!--        ></Legend>-->
      </template>
      <v-card ref="info" elevation="3" style="margin:15px" v-if="(selectedTab===1 && options.graph.visualized)">

        <v-list-item @click="show.info=!show.info">
          <v-list-item-title>
            <v-icon left>{{ show.info ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
            Information
          </v-list-item-title>
        </v-list-item>
        <v-divider></v-divider>

        <v-container v-show="show.info">
          <v-card-text>Select a node to view its neighborhood. Double click the currently selected node to change to
            detail view.
          </v-card-text>
          <v-simple-table fixed-header height="300px" dense
                          v-if="selectedTab === 1 && (selectedNode !== undefined || (neighborNodes !== undefined && neighborNodes.length>0))">
            <template v-slot:default>
              <thead>
              <tr>
                <th class="text-center">ID</th>
                <th class="text-center">Name</th>
              </tr>
              </thead>
              <tbody>
              <tr v-if="selectedNode !== undefined" :key="selectedNode.id" v-on:dblclick="nodeDetails(selectedNode.id)">
                <td><b>{{ selectedNode.id }}</b></td>
                <td><b>{{ selectedNode.label }}
                </b></td>
              </tr>
              <tr v-for="item in neighborNodes" :key="item.id" v-on:click="setSelectedNode(item.id)">
                <td>{{ item.id }}</td>
                <td>{{ item.label }}
                </td>
              </tr>
              </tbody>
            </template>
          </v-simple-table>
          <i v-else>no selection available</i>
        </v-container>
      </v-card>

      <template v-if="selectedTab===2">
        <Algorithms ref="algorithms" @executeAlgorithmEvent="submitAlgorithm"></Algorithms>
        <Jobs ref="jobs" @graphLoadEvent="graphLoadEvent" @printNotificationEvent="printNotification"
              @reloadHistoryEvent="reloadHistory"
        ></Jobs>


        <v-card ref="detail" elevation="3" style="margin:15px" v-if="detailedObject !== undefined"
                :loading="metagraph===undefined">

          <v-list-item @click="show.detail=!show.detail">
            <v-list-item-title>
              <v-icon left>{{ show.detail ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
              Detail
            </v-list-item-title>
          </v-list-item>
          <v-divider></v-divider>


          <v-container v-if="show.detail">
            <template slot="progress">
              <v-progress-linear
                color="primary"
                height="5"
                indeterminate
              ></v-progress-linear>
            </template>

            <v-chip outlined v-if="details.redirected" @click="loadDetails({...details.redirected})">
              <v-icon>fas fa-arrow-left</v-icon>
            </v-chip>
            <v-card-text>
              <template class="text--primary" style="font-size: x-large" v-if="detailedObject.node">
                <div class="text-h5">
                  <v-tooltip left>
                    <template v-slot:activator="{ on, attrs }">
                      <v-icon
                        left
                        :color="getColoring('nodes',detailedObject['Type'])"
                        v-bind="attrs"
                        v-on="on"
                        :size="hover.node1?'45px':'35px'"
                        @mouseleave.native="hover.node1=false"
                        @mouseover.native="hover.node1=true"
                      >
                        > fas fa-genderless
                      </v-icon>
                    </template>
                    <span>{{ detailedObject['Type'] }}</span>
                  </v-tooltip>
                  {{ detailedObject['Name'] }}
                </div>

              </template>
              <template class="text--primary" style="font-size: x-large" v-if="detailedObject.edge">
                <div class="text-h5">
                  <v-tooltip left>
                    <template v-slot:activator="{ on, attrs }">
                      <v-icon
                        :color="getExtendedColoring('edges',detailedObject['Type'])[0]"
                        v-bind="attrs"
                        v-on="on"
                        :size="hover.node1?'45px':'35px'"
                        @mouseleave.native="hover.node1=false"
                        @mouseover.native="hover.node1=true"
                        @click="loadDetails( {type:'node',name:getExtendedNodeNames(detailedObject['Type'])[0],id:detailedObject['ID'].split('-')[0]},{type: 'edge', name:detailedObject['Type'],id1:detailedObject['ID'].split('-')[0],id2:detailedObject['ID'].split('-')[1]})"
                      >
                        > fas fa-genderless
                      </v-icon>
                    </template>
                    <span>{{ getExtendedNodeNames(detailedObject['Type'])[0] }}</span>
                  </v-tooltip>
                  {{ detailedObject['Node1'] }}
                </div>
                <div>
                  <v-tooltip left>
                    <template v-slot:activator="{ on, attrs }">
                      <v-icon
                        v-bind="attrs"
                        v-on="on"
                        :size="hover.arrow?'45px':'35px'"
                        @mouseleave.native="hover.arrow=false"
                        @mouseover.native="hover.arrow=true">
                        {{ detailedObject.directed ? 'fas fa-long-arrow-alt-down' : 'fas fa-arrows-alt-v' }}
                      </v-icon>
                    </template>
                    <span>{{ detailedObject['Type'] }}</span>
                  </v-tooltip>
                </div>
                <div class="text-h5">
                  <v-tooltip left>
                    <template v-slot:activator="{ on, attrs }">
                      <v-icon
                        :color="getExtendedColoring('edges',detailedObject['Type'])[1]"
                        v-bind="attrs"
                        v-on="on"
                        :size="hover.node2?'45px':'35px'"
                        @mouseleave.native="hover.node2=false"
                        @mouseover.native="hover.node2=true"
                        @click="loadDetails({type:'node',name:getExtendedNodeNames(detailedObject['Type'])[1],id:detailedObject['ID'].split('-')[1]},{type: 'edge', name:detailedObject['Type'],id1:detailedObject['ID'].split('-')[0],id2:detailedObject['ID'].split('-')[1]})"
                      >
                        > fas fa-genderless
                      </v-icon>
                    </template>
                    <span>{{ getExtendedNodeNames(detailedObject['Type'])[1] }}</span>
                  </v-tooltip>
                  {{ detailedObject['Node2'] }}
                </div>
              </template>
            </v-card-text>

            <v-divider></v-divider>
            <v-timeline align-top dense>
              <v-timeline-item small :color="getDetailDotColor(item)" v-for="item in detailedObject.order"
                               :key="item">

                <div><strong>{{ item }}</strong></div>
                <div>
                  <v-list v-if="typeof detailedObject[item] === 'object'">
                    <div v-for="(i, index) in detailedObject[item]" :key="index">
                      <v-chip outlined v-if="getUrl(item,i).length>0" @click="openExternal(item,i)"
                              :title="getExternalSource(item,i)">
                        {{ format(item, i) }}
                        <v-icon right size="14px" :color="getExternalColor(item,i)">fas fa-external-link-alt
                        </v-icon>
                      </v-chip>
                      <span v-else>{{ format(item, i) }}</span>
                    </div>
                  </v-list>
                  <v-chip outlined v-else-if="getUrl(item,detailedObject[item]).length>0"
                          @click="openExternal(item,detailedObject[item])"
                          :title="getExternalSource(item,detailedObject[item])">
                    {{ format(item, detailedObject[item]) }}
                    <v-icon right size="14px" :color="getExternalColor(item,detailedObject[item])">fas
                      fa-external-link-alt
                    </v-icon>
                  </v-chip>
                  <span v-else>{{ format(item, detailedObject[item]) }}</span>
                </div>
              </v-timeline-item>
            </v-timeline>
          </v-container>

        </v-card>
      </template>
    </v-card>
  </v-container>

</template>

<script>
import Utils from "../../scripts/Utils"
import Algorithms from "./toolbox/Algorithms.vue"
import Jobs from "./toolbox/Jobs"
// import Legend from "./toolbox/Legend"
import Selection from "./toolbox/Selection";
import * as CONFIG from "../../Config"

export default {
  props: {
    options: Object,
    selectedTab: Number,
    filters: Object,
    metagraph: Object,
  },
  name: "SideCard",
  title: "",
  description: "",
  selectedNode: undefined,
  neighborNodes: [],
  // filterLabel: "",
  // filterType: "",
  // filterTypes: [],
  // filterTypeModel: [],
  // filterModel: "",
  // filterName: "",
  detailedObject: undefined,


  data() {
    return {
      gid: undefined,
      show: {
        options: true,
        // filters: false,
        // filterAdd: false,
        // filterAdding: false,
        // filterSelectDisabled: false,
        info: false,
        legend: false,
        detail: false,
        algorithms: false,
        jobs: false,
      },

      menu: {
        options: {
          list: {
            tab: 0,
            tabs: [{id: 0, label: "General"}, {id: 1, label: "Nodes"}, {id: 2, label: "Edges"}]
          }
        }
      },

      selectedNode: this.selectedNode,
      neighborNodes: this.neighborNodes,
      title: this.title,
      description: this.description,
      // filterLabel: this.filterLabel,
      // filterTypes: this.filterTypes,
      // filterTypeModel: this.filterTypeModel,
      // filterModel: this.filterModel,
      // filterEntity: "",
      detailedObject: this.detailedObject,
      hover: {arrow: false,},
      details: {redirected: false},
    }
  },
  created() {
    // this.filterTypes = ['startsWith', 'contain', 'match']
    this.gid = this.$route.params["gid"]
  },

  methods: {
    reload: function () {
      this.gid = this.$route.params["gid"]
      if (this.$refs.algorithms !== undefined)
        this.$refs.algorithms.resetAlgorithms()
      if (this.$refs.jobs !== undefined)
        this.$refs.jobs.reload()
      this.detailedObject = undefined
    },
    setAllSelected: function () {
      this.$emit("nodeSelectionEvent")
    },
    setOptions: function (name, options) {
      this.options[name] = options;
      // this.$refs.options.$forceUpdate()
    },
    setSelectedNode: function (nodeId) {
      this.$emit("nodeSelectionEvent", nodeId)
    },
    loadSelection: function (params) {
      this.gid = this.$route.params["gid"]
      if (params !== undefined) {
        this.selectedNode = params.primary;
        this.neighborNodes = params.neighbors;
      } else {
        this.selectedNode = undefined;
        this.neighborNodes = [];
      }
    },

    reloadHistory: function () {
      this.$emit("reloadHistoryEvent")
    },

    format: function (item, value) {
      if (item === "SourceIDs" || item === "SourceID" || item === "TargetID" || item === "TargetIDs" || item === "MemberOne" || item === "MemberTwo") {
        let split = value.split(".")
        switch (split[0]) {
          case "entrez":
            return split[1]
          case "drugbank":
            return split[1]
          case "uniprot":
            return split[1]
          case "reactome":
            return split[1]
          case "mondo":
            return split[1]
          case "ncit":
            return split[1]
          case "mesh":
            return split[1]
          case "doid":
            return split[1]
          case "snomedct":
            return split[1]
          case "omim":
            return split[1]
          case "orpha":
            return split[1]
          case "umls":
            return split[1]
          case "meddra":
            return split[1]
          case "medgen":
            return split[1]
        }
      }
      if (item === "_cls")
        return value.split('.')[1]
      if (item === "mapLocation") {
        let split = value.indexOf("p") > 0 ? value.split("p") : value.split("q");
        return "Chr" + split[0] + ":" + split[1]
      }
      return value
    },
    getExternalSource: function (item, value) {
      if (item === "SourceIDs" || item === "SourceID" || item === "TargetID" || item === "TargetIDs" || item === "MemberOne" || item === "MemberTwo") {
        let split = value.split(".")
        switch (split[0]) {
          case "entrez":
            return "Entrez"
          case "drugbank":
            return "DrugBank"
          case "uniprot":
            return "UniProt"
          case "reactome":
            return "Reactome"
          case "mondo":
            return "Mondo"
          case "ncit":
            return "NCIthesaurus"
          case "mesh":
            return "Mesh"
          case "doid":
            return "DiseaseOntology"
          case "snomedct":
            return "SnomedCT"
          case "omim":
            return "OMIM"
          case "orpha":
            return "orpha"
          case "umls":
            return "NCImetathesaurus"
          case "meddra":
            return "BioPortal"
          case "medgen":
            return "NCBI"
        }
      }
      if (item === "ICD-10")
        return "ICD"
      if (item === "Symbol")
        return "GeneCards"
      if (item === "Genomic Location")
        return "UCSC"
      if (item === "CAS-Number")
        return "Molbase"
      if (item === "Formula")
        return "ChemCalc"
      return value
    },
    getUrl: function (item, value) {
      let url = '';
      if (value === undefined || value.length === 0)
        return ""
      if (item === "SourceIDs" || item === "SourceID" || item === "TargetID" || item === "TargetIDs" || item === "MemberOne" || item === "MemberTwo") {
        let split = value.split(".")
        switch (split[0]) {
          case "entrez":
            return 'https://www.ncbi.nlm.nih.gov/gene/' + split[1]
          case "drugbank":
            return 'https://go.drugbank.com/drugs/' + split[1]
          case "uniprot":
            return 'https://www.uniprot.org/uniprot/' + split[1]
          case "reactome":
            return 'https://reactome.org/content/detail/' + split[1]
          case "mondo":
            return "https://monarchinitiative.org/disease/MONDO:" + split[1]
          case "ncit":
            return "https://ncit.nci.nih.gov/ncitbrowser/ConceptReport.jsp?dictionary=NCI_Thesaurus&code=" + split[1]
          case "mesh":
            return "https://meshb.nlm.nih.gov/record/ui?ui=" + split[1]
          case "doid":
            return "https://disease-ontology.org/term/DOID%3" + split[1]
          case "snomedct":
            return "http://snomed.info/id/" + split[1]
          case "omim":
            return "https://www.omim.org/entry/" + split[1]
          case "orpha":
            return "https://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=EN&Expert=" + split[1]
          case "umls":
            return "https://ncim.nci.nih.gov/ncimbrowser/ConceptReport.jsp?dictionary=NCI%20MetaThesaurus&code=" + split[1]
          case "meddra":
            return "http://purl.bioontology.org/ontology/MEDDRA/" + split[1]
          case "medgen":
            return "https://www.ncbi.nlm.nih.gov/medgen/?term=" + split[1]

        }
      }

      if (item === "ICD-10")
        return "https://icd.who.int/browse10/2019/en#/" + value
      if (item === "Symbol")
        return "https://www.genecards.org/cgi-bin/carddisp.pl?gene=" + value
      if (item === "Genomic Location")
        return "http://genome.ucsc.edu/cgi-bin/hgTracks?db=hg38&position=" + value
      if (item === "CAS-Number")
        return "http://www.molbase.com/en/cas-" + value + ".html"
      if (item === "Formula")
        return "https://www.chemcalc.org/?mf=" + value
      if (item === "Databases" || item === "Datasets" || item === "Primary Dataset")
        switch (value) {
          case "biogrid":
            return "https://thebiogrid.org/"
          case "hprd":
            return "https://www.hprd.org/"
          case "DrugBank":
            return "https://go.drugbank.com/"
          case "DrugCentral":
            return "https://drugcentral.org/"
          case "iid-pred":
            return "http://iid.ophid.utoronto.ca/"
          case "mint":
            return "https://mint.bio.uniroma2.it/"
          case "intact":
            return "https://www.ebi.ac.uk/intact/"
          case "iid":
            return "http://iid.ophid.utoronto.ca/"
          case "iid-ortho":
            return "http://iid.ophid.utoronto.ca/"
          case "bci":
            return "http://califano.c2b2.columbia.edu/b-cell-interactome"
          case "dip":
            return "https://dip.doe-mbi.ucla.edu/dip/Main.cgi"
          case "i2d":
            return "http://ophid.utoronto.ca/ophidv2.204/"
          case "innatedb":
            return "https://www.innatedb.com/"
        }
      if (item === "AssertedBy") {
        if (value === "omim")
          return "https://www.omim.org/"
        if (value === "disgenet")
          return "https://www.disgenet.org/"
      }
      return url;
    },
    getExternalColor: function (item, value) {
      if (item === "SourceIDs" || item === "SourceID" || item === "TargetID" || item === "TargetIDs" || item === "MemberOne" || item === "MemberTwo") {
        let split = value.split(".")
        switch (split[0]) {
          case "entrez":
            return "#369"
          case "drugbank":
            return "#ff00b8"
          case "uniprot":
            return "#5caecd"
          case "reactome":
            return "#2F9EC2"
          case "mondo":
            return "#15556a"
          case "ncit":
            return "#9f1314"
          case "mesh":
            return "#20558a"
          case "doid":
            return "#073399"
          case "snomedct":
            return "#00a9e0"
          case "omim":
            return "#333333"
          case "orpha":
            return "#d42c56"
          case "umls":
            return "#c31f40"
          case "meddra":
            return "#234979"
          case "medgen":
            return "#369"
        }
      }

      if (item === "ICD-10")
        return "#006000"
      if (item === "Symbol")
        return "#f07b05"
      if (item === "Genomic Location")
        return "#00457c"
      if (item === "CAS-Number")
        return "#749bc4"
      if (item === "Formula")
        return "#33484d"

      if (item === "Databases" || item === "Datasets" || item === "Primary Dataset")
        switch (value) {
          case "biogrid":
            return "773a3a"
          case "DrugBank":
            return "#ff00b8"
          case "DrugCentral":
            return "#e00600"
          case "iid-pred":
            return "#3a332d"
          case "hprd":
            return "#333466"
          case "mint":
            return "#228b22"
          case "intact":
            return "#57A7A7"
          case "iid":
            return "#3a332d"
          case "iid-ortho":
            return "#3a332d"
          case "bci":
            return "#4f54b0"
          case "dip":
            return "#a0c0f5"
          case "i2d":
            return "#3b3b6b"
          case "innatedb":
            return "#2572a6"
        }
      if (item === "assertedBy") {
        if (value === "omim")
          return "#333333"
        if (value === "disgenet")
          return "#ff00de"
      }
      return "black"
    }
    ,
    openExternal: function (item, i) {
      window.open(this.getUrl(item, i), '_blank')
    }
    ,
    getColoring: function (type, name) {
      // if (name.endsWith("Drug"))
      //   name = "drug"
      return Utils.getColoring(this.metagraph, type, name)
    },

    getExtendedColoring: function (type, name) {
      // if (name.endsWith("Drug"))
      //   name = "drug"
      return Utils.getColoringExtended(this.metagraph, this.options.list.entityGraph, type, name)
    },
    getNodeNames: function (type) {
      return Utils.getNodes(this.metagraph, type)
    },
    getExtendedNodeNames: function (type) {
      return Utils.getNodesExtended(this.options.list.entityGraph, type)
    },

    visualizeGraph: function () {
      // this.options.graph.visualized = true;
      this.$emit('applyEvent');
      this.show.options = false;
      this.options.graph.physics = false;
      this.options.graph.loops = true;
      this.options.graph.single = true;
      this.$nextTick(() => {
        this.show.options = true;
      })

    },
    toggleSelectMode: function (select) {
      this.$emit('selectModeEvent', select)
    }
    ,
    setFiltering: function () {
      this.show.filterAdding = true;
    }
    ,

    loadFilter: function (data) {
      if (data !== undefined) {
        this.filters = data.filters
        if (this.filters === undefined)
          this.filters = []
        this.filterName = data.name
      }
    }
    ,
    applyMultiSelect: function(selection){
      this.$emit("applyMultiSelect",selection);
    },
    setMultiSelect: function(selection){
      this.$refs.selection.setSelection(selection)
    },
    nodeDetails: function (id) {
      let str = id.split("_")
      this.$emit("nodeDetailsEvent", {prefix: str[0], id: str[1]})
    },
    printNotification: function (message, type) {
      this.$emit("printNotificationEvent", message, type)
    }
    ,
    loadDetails: function (data, redirect) {
      this.details.redirected = false;
      if (redirect)
        this.details.redirected = redirect
      if (data.type === "node") {
        this.$http.get("getNodeDetails?name=" + data.name + "&id=" + data.id).then(response => {
          if (response.data !== undefined) {
            this.detailedObject = response.data
            this.detailedObject.node = true;
            this.description = "for " + data["Type"] + " " + this.detailedObject["Name"] + " (id:" + this.detailedObject["ID"] + ")"
          }
        }).catch(err => {
          console.log(err)
        })
      } else if (data.type === "edge") {
        this.$http.get("getEdgeDetails?name=" + data.name + "&id1=" + data.id1 + "&id2=" + data.id2 + "&gid=" + this.gid).then(response => {
          if (response.data !== undefined) {
            this.detailedObject = response.data
            this.detailedObject.edge = true;
            this.detailedObject.directed = Utils.directionExtended(this.options.list.entityGraph, data.name)
            if (this.detailedObject['Source'] !== undefined)
              this.description = "for " + data.name + " " + this.detailedObject['Name'] + " (id:" + this.detailedObject['Source'] + "->" + this.detailedObject['Target'] + ")"
            if (this.detailedObject['IDOne'] !== undefined)
              this.description = "for " + data.name + " id:" + this.detailedObject['IDOne'] + "<->" + this.detailedObject['IDTwo']
          }
        }).catch(err => {
          console.log(err)
        })
      }
      this.show.detail = true
    },

    // clearModels: function () {
    //   this.filterModel = ""
    //   this.filterTypeModel = ""
    // }
    // ,
    // saveFilter: function () {
    //   let data = {type: this.filterTypeModel, expression: this.filterModel};
    //
    //   if (this.filters[this.filterEntity] === undefined)
    //     this.filters[this.filterEntity] = []
    //
    //   if (this.filters[this.filterEntity].filter(f => (f.type === this.filterTypeModel && f.expression === this.filterModel)).length === 0) {
    //     this.filters[this.filterEntity].push(data)
    //   }
    //   this.filterTypeModel = ""
    //   this.filterModel = ""
    // }
    // ,
    // removeFilter: function (idx) {
    //   this.filters[this.filterEntity].splice(idx, 1)
    //   this.$refs.filterTable.$forceUpdate()
    // }
    // ,
    getDetailDotColor: function (attribute) {
      if (this.detailedObject.node)
        return this.getColoring('nodes', this.detailedObject["Type"]);
      let basic = "#464e53";
      let colors = this.getExtendedColoring('edges', this.detailedObject["Type"]);
      if (["Source", "Node1", "SourceDomainID", "SourceID", "SourceDomainIDs", "IDOne", "MemberOne"].indexOf(attribute) > -1)
        return colors[0]
      if (["Target", "Node2", "TargetDomainID", "TargetID", "TargetDomainIDs", "IDTwo", "MemberTwo"].indexOf(attribute) > -1)
        return colors[1]
      return basic;
    },
    submitAlgorithm: function (algorithm, params) {
      this.$emit('executeAlgorithmEvent', algorithm, params)
    },
    graphViewEvent: function (data) {
      this.$emit("graphViewEvent", data)
    },
    addJob: function (data) {
      this.$refs.algorithms.resetAlgorithms()
      this.$refs.jobs.addJob(data)
    },
    graphLoadEvent: function (data) {
      this.$emit("graphLoadEvent", data)
    },
    formatTime: function (timestamp) {
      Utils.formatTime(timestamp)
    },
    requestGraphDownload: function () {
      window.open(CONFIG.HOST_URL+CONFIG.CONTEXT_PATH+'/api/downloadGraph?gid=' + this.gid)
    }
  }
  ,
  components: {
    Algorithms,
    Jobs,
    // Legend,
    Selection
  },

}
</script>

<style lang="sass">


.span::-webkit-scrollbar
  display: none


.span
  -ms-overflow-style: none
  scrollbar-width: none


</style>
