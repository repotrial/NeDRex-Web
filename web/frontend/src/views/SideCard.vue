<template>
  <v-container style="position: fixed; width: 25%">

    <v-card elevation="3" style="padding-top: 15px; overflow-y: auto; max-height: 75vh">
      <v-card-title>Toolbox</v-card-title>
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
                  <v-chip outlined @click="$emit('applyEvent')">
                    <v-icon left>far fa-check-circle</v-icon>
                    Apply Selection
                  </v-chip>
                </v-list-item>
                <v-list-item>
                  <v-switch v-model="options.start.onlyConnected" label="Hide unconnected"></v-switch>
                </v-list-item>
                <v-list-item>
                  <v-switch v-model="options.start.skipVis" label="Skip visualisation"></v-switch>
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
              <v-tabs
                fixed-tabs
                v-model="menu.options.list.tab"
              >
                <v-tabs-slider></v-tabs-slider>
                <v-tab v-for="tab in menu.options.list.tabs" class="primary--text" :key=tab.id>
                  {{ tab.label }}
                </v-tab>
              </v-tabs>
              <v-list-item>
                <v-switch
                  v-model="options.list.showAll"
                  @click="$emit('reloadTablesEvent')"
                  :label="'Show all Items ('+options.list.selected+'/'+options.list.total+')'">
                  >
                </v-switch>
              </v-list-item>
              <v-divider></v-divider>
              <template v-if="menu.options.list.tab===0">
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
                    Collapse Graph
                  </v-chip>
                </v-list-item>
                <v-list-item>
                  <v-chip
                    icon
                    outlined
                    v-on:click="$emit('graphModificationEvent','subselect')"
                  >
                    <v-icon left>fas fa-project-diagram</v-icon>
                    Load Selection
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
                    Select All
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
                    Unselect All
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
                    Select All
                  </v-chip>
                </v-list-item>
                <v-list-item>
                  <v-chip
                    icon
                    outlined
                    v-on:click="$emit('selectionEvent','edges','none')"
                  >
                    <v-icon left>fas fa-ban</v-icon>
                    Unselect All
                  </v-chip>
                </v-list-item>
              </template>
            </template>
            <template v-if="selectedTab===3">
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

      <v-card ref="filters" elevation="3" style="margin:15px" v-if="selectedTab===0">

        <v-list-item @click="show.filters=!show.filters">
          <v-list-item-title>
            <v-icon left>{{ show.filters ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
            Filters
          </v-list-item-title>
        </v-list-item>
        <v-divider></v-divider>

        <v-container v-show="show.filters">
          <v-row>
            <v-col>
              <v-list-item>
                <v-chip outlined
                        @click="show.filterAdd=!show.filterAdd; show.filterSelectDisabled=false; filterEntity=''">
                  <v-icon left>{{ show.filterAdd ? "fas fa-check" : "fas fa-filter" }}</v-icon>
                  {{ show.filterAdd ? "Done" : "Edit Filters" }}
                </v-chip>
              </v-list-item>
            </v-col>
            <v-col>
              <v-select v-if="show.filterAdd "
                        v-model="filterEntity"
                        :items="options.start.selectedElements"
                        :autofocus="show.filterAdd"
                        item-text="name"
                        item-value="name"
                        @focusout="show.filterSelectDisabled=true"
                        :disabled="show.filterSelectDisabled"
                        @change="setFiltering"
              >Select Entity
              </v-select>
            </v-col>
          </v-row>
          <v-simple-table fixed-header ref="filterTable" v-if="filterEntity.length>0">
            <template v-slot:default>
              <thead>
              <tr>
                <th class="text-center">Type</th>
                <th class="text-center">Filter</th>
                <th class="text-center">Operation</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="(item,index) in filters[filterEntity]" :key="item.type+item.expression">
                <td>{{ item.type }}</td>
                <td>{{ item.expression }}</td>
                <td>
                  <v-chip outlined v-on:click="removeFilter(index)">
                    <v-icon dense>fas fa-trash</v-icon>
                  </v-chip>
                </td>
              </tr>
              <tr v-if="show.filterAdding">
                <td>
                  <v-select
                    v-model="filterTypeModel"
                    :items="filterTypes"
                    label="type"
                  ></v-select>
                </td>
                <td>
                  <v-text-field
                    v-model="filterModel"
                    :label="filterLabel"
                    placeholder="Pattern"
                  ></v-text-field>
                </td>
                <td>
                  <v-chip outlined v-on:click="saveFilter"
                          :disabled="filterModel ===undefined|| filterModel.length ===0 ||filterTypeModel ===undefined">
                    <v-icon dense>fas fa-plus</v-icon>
                  </v-chip>
                </td>
              </tr>
              </tbody>
            </template>
          </v-simple-table>
        </v-container>
      </v-card>


      <v-card ref="legend" elevation="3" style="margin:15px" v-if="(selectedTab===1 && options.graph.visualized)">
        <v-list-item @click="show.legend=!show.legend">
          <v-list-item-title>
            <v-icon left>{{ show.legend ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
            Legend
          </v-list-item-title>
        </v-list-item>
        <v-divider></v-divider>


      </v-card>

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
        <v-card elevation="3" style="margin:15px">
          <v-list-item @click="show.algorithms=!show.algorithms">
            <v-list-item-title>
              <v-icon left>{{ show.algorithms ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
              Algorithms
            </v-list-item-title>
          </v-list-item>
          <v-container v-show="show.algorithms">
            <v-row>
              <v-col cols="6">
                <v-select
                  v-model="algorithms.categoryModel"
                  :items="algorithms.categories"
                  item-text="label"
                  item-value="id"
                  label="Algorithm Category"
                >
                </v-select>
              </v-col>
              <v-col cols="6" v-if="algorithms.categoryModel >-1">
                <v-select
                  v-model="algorithms.categories.methodModel"
                  :items="algorithms.categories[algorithms.categoryModel].methods"
                  item-text="label"
                  item-value="id"
                  label="Method"
                >
                </v-select>
              </v-col>
            </v-row>
            <v-divider></v-divider>
            <v-row
              v-if="algorithms.categoryModel >-1 && algorithms.categories.methodModel ==='diamond'">
              <v-col cols="6">
                <v-switch
                  label="Use Selection"
                  v-model="algorithms.selectionSwitch"
                >
                </v-switch>
              </v-col>
              <v-col cols="6">
                <v-select
                  item-text="label"
                  item-value="id"
                  :items="[{id:'gene',label:'Gene'},{id:'protein',label:'Protein'}]"
                  label="Node"
                  v-model="algorithms.nodeModel"
                >
                </v-select>
              </v-col>
            </v-row>
            <v-row>
              <v-col>
                <v-chip outlined color="green"
                        :disabled="algorithms.nodeModel === undefined || algorithms.nodeModel.length ===0"
                        @click="$emit('executeAlgorithmEvent','diamond',{node:algorithms.nodeModel,selection:algorithms.selectionSwitch})">
                  Submit
                </v-chip>
              </v-col>
            </v-row>
          </v-container>

        </v-card>
        <v-card ref="jobs" elevation="3" style="margin:15px">
          <v-list-item @click="show.jobs=!show.jobs">
            <v-list-item-title>
              <v-icon left>{{ show.jobs ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
              Jobs
            </v-list-item-title>
          </v-list-item>
          <v-divider></v-divider>
          <v-container v-if="show.jobs">
            <v-list-item v-for="job in jobs" :key="job.jid">
              <v-chip :color="job.state==='DONE'?'green':'orange'" :disabled="job.state!=='DONE'"
                      @click="$emit('graphLoadEvent', {post: {id: job.gid}})">
                <v-icon left v-if="job.state==='DONE'">
                  fas fa-check
                </v-icon>
                <v-icon left v-else>
                  fas fa-circle-notch fa-spin
                </v-icon>
                [{{ job.state }}] {{formatTime(job.created)[1]}} ago ({{formatTime(job.created)[0]}})
              </v-chip>
            </v-list-item>
          </v-container>
        </v-card>


        <v-card ref="detail" elevation="3" style="margin:15px" v-if="detailedObject !== undefined">

          <v-list-item @click="show.detail=!show.detail">
            <v-list-item-title>
              <v-icon left>{{ show.detail ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
              Detail
            </v-list-item-title>
          </v-list-item>
          <v-divider></v-divider>


          <v-container v-if="show.detail">
            <v-list class="transparent">
              <template
                v-for="item in detailedObject.order"
              >

                <v-list-item :key="item">

                  <v-list-item-title>{{ item }}</v-list-item-title>
                  <v-list-item-subtitle class="text-right">
                    <v-list v-if="typeof detailedObject[item] === 'object'">
                      <v-list-item v-for="i in detailedObject[item]" :key="i">
                        <v-chip outlined v-if="getUrl(item,i).length>0" @click="openExternal(item,i)"
                                :title="getExternalSource(item,i)">
                          {{ format(item, i) }}
                          <v-icon right size="14px" :color="getExternalColor(item,i)">fas fa-external-link-alt</v-icon>
                        </v-chip>
                        <span v-else>{{ format(item, i) }}</span>
                      </v-list-item>
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
                  </v-list-item-subtitle>
                </v-list-item>
                <v-divider></v-divider>
              </template>
            </v-list>
          </v-container>

        </v-card>
      </template>
    </v-card>
  </v-container>

</template>

<script>
export default {
  props: {
    options: Object,
    selectedTab: Number,
    filters: Object,
  },
  name: "SideCard",
  title: "",
  description: "",
  selectedNode: undefined,
  neighborNodes: [],
  filterLabel: "",
  filterType: "",
  filterTypes: [],
  filterTypeModel: [],
  filterModel: "",
  filterName: "",
  detailedObject: undefined,
  gid: undefined,

  data() {
    return {
      show: {
        options: true,
        filters: false,
        filterAdd: false,
        filterAdding: false,
        filterSelectDisabled: false,
        info: false,
        legend: false,
        detail: false,
        algorithms: false,
        jobs: false,
      },
      jobs: [],
      menu: {
        options: {
          list: {
            tab: 0,
            tabs: [{id: 0, label: "General"}, {id: 1, label: "Nodes"}, {id: 2, label: "Edges"}]
          }
        }
      },
      algorithms: {
        categoryModel: -1,
        methodModel: "",
        nodeModel: undefined,
        selectionSwitch: false,
        categories: [{
          id: 0,
          label: "Disease Modules",
          methods: [{id: "diamond", label: "DIAMOnD"}, {id: "bicon", label: "BiCoN"}]
        }, {
          id: 1,
          label: "Drug Ranking",
          methods: [{id: "trustrank", label: "TrustRank"}, {id: "centrality", label: "Centrality"}]
        }]
      },
      selectedNode: this.selectedNode,
      neighborNodes: this.neighborNodes,
      title: this.title,
      description: this.description,
      filterLabel: this.filterLabel,
      filterTypes: this.filterTypes,
      filterTypeModel: this.filterTypeModel,
      filterModel: this.filterModel,
      detailedObject: this.detailedObject,
      filterEntity: "",
    }
  },
  created() {
    this.filterTypes = ['startsWith', 'contain', 'match']
    this.gid = this.$route.params["gid"]
    console.log(this.options)
    this.$socket.$on("jobUpdateEvent", this.updateJob)
  },

  methods: {
    print: function (message) {
      console.log(message)
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
    format: function (item, value) {
      if (item === "primaryDomainId" || item === "primaryDomainIds" || item === "domainIds" || item === "sourceDomainId" || item === "targetDomainId" || item === "memberOne" || item === "memberTwo") {
        console.log("Format value='" + value + "'")
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
        console.log(value)
        let split = value.indexOf("p") > 0 ? value.split("p") : value.split("q");
        return "Chr" + split[0] + ":" + split[1]
      }
      return value
    },
    getExternalSource: function (item, value) {
      if (item === "primaryDomainId" || item === "primaryDomainIds" || item === "domainIds" || item === "sourceDomainId" || item === "targetDomainId" || item === "memberOne" || item === "memberTwo") {
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
      if (item === "icd10")
        return "ICD"
      if (item === "approvedSymbol")
        return "GeneCards"
      if (item === "mapLocation")
        return "UCSC"
      if (item === "casNumber")
        return "Molbase"
      if (item === "molecularFormula")
        return "ChemCalc"
      return value
    },
    getUrl: function (item, value) {
      let url = '';
      if (value.length === 0)
        return value
      if (item === "primaryDomainId" || item === "primaryDomainIds" || item === "domainIds" || item === "sourceDomainId" || item === "targetDomainId" || item === "memberOne" || item === "memberTwo") {
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
            return "https://snomedbrowser.com/Codes/Details/" + split[1]
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
      if (item === "icd10")
        return "https://icd.who.int/browse10/2016/en#/" + value
      if (item === "approvedSymbol")
        return "https://www.genecards.org/cgi-bin/carddisp.pl?gene=" + value
      if (item === "mapLocation")
        return "http://genome.ucsc.edu/cgi-bin/hgTracks?db=hg38&position=" + value
      if (item === "casNumber")
        return "http://www.molbase.com/en/cas-" + value + ".html"
      if (item === "databases" || item === "allDatasets" || item === "primaryDataset")
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
      if (item === "assertedBy") {
        if (value === "omim")
          return "https://www.omim.org/"
        if (value === "disgenet")
          return "https://www.disgenet.org/"
      }
      if (item === "molecularFormula")
        return "https://www.chemcalc.org/?mf=" + value
      return url;
    },
    getExternalColor: function (item, value) {
      if (item === "primaryDomainId" || item === "primaryDomainIds" || item === "domainIds" || item === "sourceDomainId" || item === "targetDomainId" || item === "memberOne" || item === "memberTwo") {
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
            return "#234979"
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
      if (item === "icd10")
        return "#006000"
      if (item === "approvedSymbol")
        return "#f07b05"
      if (item === "mapLocation")
        return "#00457c"
      if (item === "casNumber")
        return "#749bc4"
      if (item === "databases" || item === "allDatasets" || item === "primaryDataset")
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
      if (item === "molecularFormula")
        return "#33484d"
      return "black"
    }
    ,
    openExternal: function (item, i) {
      window.open(this.getUrl(item, i), '_blank')
    }
    ,

    visualizeGraph: function () {
      // this.options.graph.visualized = true;
      this.$emit('applyEvent');
      this.show.options = false;
      this.$nextTick(() => {
        this.show.options = true;
      })

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
    nodeDetails: function (id) {
      let str = id.split("_")
      this.$emit("nodeDetailsEvent", {prefix: str[0], id: str[1]})
    }
    ,
    loadDetails: function (data) {
      if (data.type === "node") {
        this.$http.get("getNodeDetails?name=" + data.name + "&id=" + data.id).then(response => {
          if (response.data !== undefined) {
            this.detailedObject = response.data
            this.description = "for " + data.name + " " + this.detailedObject.displayName + " (id:" + this.detailedObject.id + ")"
          }
        }).catch(err => {
          console.log(err)
        })
      } else if (data.type === "edge") {
        this.$http.get("getEdgeDetails?name=" + data.name + "&id1=" + data.id1 + "&id2=" + data.id2 + "&gid=" + this.gid).then(response => {
          if (response.data !== undefined) {
            this.detailedObject = response.data
            if (this.detailedObject.sourceId !== undefined)
              this.description = "for " + data.name + " " + this.detailedObject.displayName + " (id:" + this.detailedObject.sourceId + "->" + this.detailedObject.targetId + ")"
            if (this.detailedObject.idOne !== undefined)
              this.description = "for " + data.name + " id:" + this.detailedObject.idOne + "<->" + this.detailedObject.idTwo
          }
        }).catch(err => {
          console.log(err)
        })
      }
    },
    addJob: function (data) {
      this.$socket.subscribeJob(data.jid, "jobUpdateEvent")
      this.jobs.push({gid: data.gid, jid: data.jid,created:data.created, state: data.state})
    }
    ,
    updateJob: function (response) {
      let params = JSON.parse(response)
      this.jobs.forEach(j => {
        if (j.jid === params.jid) {
          j.state = params.state;
          j.gid = params.gid;
        }
      })
      if(params.state==='DONE')
        this.$socket.unsubscribeJob(params.jid)
    },
    clearModels: function () {
      this.filterModel = ""
      this.filterTypeModel = ""
    }
    ,
    saveFilter: function () {
      let data = {type: this.filterTypeModel, expression: this.filterModel};

      if (this.filters[this.filterEntity] === undefined)
        this.filters[this.filterEntity] = []

      if (this.filters[this.filterEntity].filter(f => (f.type === this.filterTypeModel && f.expression === this.filterModel)).length === 0) {
        this.filters[this.filterEntity].push(data)
      }
      this.filterTypeModel = ""
      this.filterModel = ""
    }
    ,
    removeFilter: function (idx) {
      this.filters[this.filterEntity].splice(idx, 1)
      this.$refs.filterTable.$forceUpdate()
    }
    ,
    formatTime: function (timestamp) {
      timestamp *= 1000
      let d = new Date();
      let date = new Date(timestamp);
      let diff = ((d.getTime() - (d.getTimezoneOffset() * 60 * 1000)) - timestamp) / 1000;
      let diff_str = "~";
      if (diff < 60)
        diff_str = "<1m";
      else if (diff < 60 * 60)
        diff_str += Math.round(diff / (60)) + "m";
      else if (diff < 60 * 60 * 24)
        diff_str += Math.round(diff / (60 * 60)) + "h";
      else if (diff < 60 * 60 * 24 * 365)
        diff_str += Math.round(diff / (60 * 60 * 24)) + "d";
      else
        diff_str += Math.round(diff / (60 * 60 * 24 * 356)) + "a";
      return [date.toISOString().slice(0, -8), diff_str]
    },
  }
  ,

}
</script>

<style lang="sass">


.span::-webkit-scrollbar
  display: none


.span
  -ms-overflow-style: none
  scrollbar-width: none


</style>
