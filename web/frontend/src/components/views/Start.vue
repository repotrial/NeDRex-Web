<template>
  <div style="margin-top:20px">
    <v-card v-show="showStartSelection">
      <v-list>
        <v-list-item>
          <v-list-item-title class="title">Select one way to start exploring the NeDRex knowledge-graph!
          </v-list-item-title>
        </v-list-item>
        <a ref="top"></a>
        <div class="v-card__subtitle" style="width: 100%; align-content: center; display: flex; font-size: .9rem">
          <div style="width: 70vw; margin: auto">
            <ul>
              <li>
                <b><i>
                  <v-icon left small>fas fa-arrow-right</v-icon>
                  Quick Drug Repurposing:</i></b> For quick and easy algorithmic drug-repurposing candidate discovery
                pipelines.
              </li>
              <li>
                <b style="margin-left: 32px">Starting point</b> is a list of genes/proteins or a disease of interest.
              </li>
              <li style="margin-top: 8px">
                <b><i>
                  <v-icon left small>fas fa-arrow-right</v-icon>
                  Guided Connectivity Search:</i></b> May be used for the creation of networks based on some specific
                path
                through the metagraph and the derivation of induced graphs.
              </li>
              <li>
                <b style="margin-left: 32px">Starting point</b> is a list of any entities of interest.
              </li>
              <li style="margin-top: 8px">
                <b><i>
                  <v-icon left small>fas fa-arrow-right</v-icon>
                  Advanced Exploration:</i></b> Networks can be freely constructed and explored using more powerful
                functions.
              </li>
              <li>
                <b style="margin-left: 32px">Starting point</b> is a metagraph representation of the initial network
                with filters applied to nodes and edges.
              </li>
            </ul>
          </div>
        </div>
        <v-divider></v-divider>
        <v-list-item class="justify-center">
          <v-radio-group v-model="startTab" row>
            <v-radio label="Quick Drug Repurposing" @change="checkURLclear('quick')" :value="0"></v-radio>
            <v-radio label="Guided Connectivity Search" @change="checkURLclear('guided')" :value="1"></v-radio>
            <v-radio label="Advanced Exploration" @change="checkURLclear('advanced')" :value="2"></v-radio>
          </v-radio-group>
        </v-list-item>
      </v-list>
      <v-divider v-if="startTab !== 1"></v-divider>
      <div class="v-card__subtitle" style="width: 100%; align-content: center; display: flex; font-size: .9rem">
        <div style="margin-left: 15vw">
          <div v-if="startTab===0">
            <ul>
              <li>
                Pick one of the three following options for quick drug repurposing:
              </li>
              <li><b>1. Module Identification:</b> Identifies a disease module based on a set of given input genes or a
                disease.
              </li>
              <li><b>2. Drug Prioritization:</b> Identifies a ranked list of drugs given a set of input genes or a
                disease.
              </li>
              <li><b>3. Drug Repurposing:</b> Chains <i>Module Identification</i> and<i>Drug Prioritization</i></li>
              <li style="margin-top: 8px"><i>For each method you can choose a 'Quick Mode' in which a default method
                with default parameters is used.</i></li>
            </ul>
          </div>
        </div>
        <div v-if="startTab===1"></div>
        <div v-if="startTab===2">
          <ul>
            <li>
              Create a specified starting network by selecting the nodes and edge types it should contain:
            </li>
            <li><b>1. Meta-network: </b>Select nodes and edges from the left. You will see the connections between selected entities on the right.</li>
            <li><b>2. Filtering: </b>Click on <i>edges</i> and <i>nodes</i> in the meta-network right side to select of filter elements of this kind.</li>
            <li><b>3. Create: </b>Scroll down and click on 'Generate Network'!</li>
          </ul>
        </div>

      </div>
    </v-card>
    <Quick v-if="startTab===0" @printNotificationEvent="printNotification"
           @graphLoadNewTabEvent="loadGraphNewTab" @graphLoadEvent="loadGraph" @focusEvent="focusTop"
           @clearURLEvent="$emit('clearURLEvent','quick')"
           ref="quick" @showStartSelectionEvent="toggleStartSelection" @newGraphEvent="$emit('newGraphEvent')"></Quick>
    <Guided v-if="startTab===1" @printNotificationEvent="printNotification" @newGraphEvent="$emit('newGraphEvent')"
            @graphLoadEvent="loadGraph" @graphLoadNewTabEvent="loadGraphNewTab"
            @clearURLEvent="$emit('clearURLEvent', 'guided')" ref="guided"></Guided>
    <Advanced ref="advanced" v-if="startTab===2" :options="options" :colors="colors" :filters="filters"
              @printNotificationEvent="printNotification"
              @graphLoadEvent="loadGraph"
    />

  </div>
</template>

<script>
import Advanced from "./start/Advanced";
import Quick from "./start/Quick"
import Guided from "./start/Guided";

export default {
  name: "Start",
  props: {
    options: Object,
    filters: Object,
    jid: String,
    colors: {
      type: Object
    },
  },
  data() {
    return {
      startTab: 0,
      job: this.jid,
      showStartSelection: true,
    }
  },
  watch: {
    startTab: function (val) {
      this.$emit("showSideEvent", false)
      this.focusTop()
    },
  },
  created() {
    // if (this.$route.path.split("/").indexOf("start") > -1)
    //   this.$emit("showSideEvent", this.startTab === 2)
    this.setView()
    this.job = this.$route.query["job"]
    if (this.job != null)
      this.loadJob();
  },
  mounted() {
  },
  methods: {
    reset: function () {
      this.resetIndex(this.startTab)
      this.startTab = 0;
    }
    ,
    reload: function () {
      if (this.job != null)
        this.loadJob();
    },

    loadJob: function () {
      this.$http.get("getJob?id=" + this.job).then(response => {
        console.log(response.data)
        this.startTab = 0
        this.$nextTick(() => {
          this.$refs.quick.reloadJob(response.data)
        })
      })
    },

    resetIndex: function (idx) {
      if (idx === 0 && this.$refs.quick)
        this.$refs.quick.reset()
      if (idx === 1 && this.$refs.guided)
        this.$refs.guided.reset()
      if (idx === 2 && this.$refs.advanced)
        this.$refs.advanced.reset()
    },
    printNotification: function (message, style, timeout) {
      this.$emit("printNotificationEvent", message, style, timeout)
    },

    checkURLclear: function (view) {
      if (!this.$route.params.gid)
        this.$emit("clearURLEvent", view)
      else
        this.$emit("modifyURLEvent", view)
    },

    executeGraphLoad: function (bool) {
      if (this.startTab === 2)
        this.$refs.advanced.loadGraph(bool)
    },

    loadGraph: function (data) {
      this.$emit("graphLoadEvent", data)
      this.reset()
    },
    loadGraphNewTab: function (data) {
      this.$emit("graphLoadNewTabEvent", data)
      // this.reset()
    },

    toggleStartSelection: function (bool) {
      this.showStartSelection = bool
    },

    direction: function (edge) {
      if (this.$utils.isEdgeDirected(this.$global.metagraph, edge))
        return 1
      return 0
    },
    getColoring: function (entity, name) {
      return this.$utils.getColoring(this.$global.metagraph, entity, name)
    },
    getStartType: function () {
      return this.startTab;
    },
    focusTop: function () {
      let element = this.$refs["top"];
      this.$nextTick(() => element.scrollIntoView(true))
    },
    showTab: function (idx) {
      this.startTab = idx
    },
    setView: function () {
      let path = this.$route.path.split("/")
      let start = path.indexOf('start') > -1
      let mode = path.length > 1 ? path[2] : undefined

      if (start) {
        if (mode === "quick")
          this.showTab(0)
        else if (mode === "guided")
          this.showTab(1)
        else if (mode === "advanced")
          this.showTab(2)
      }

    }
  },
  components: {
    Advanced,
    Quick,
    Guided
  },

}
</script>

<style scoped>
.title {
  font-weight: bold !important;
}

.v-card__subtitle ul {
  padding-left: 20px; /* Adjusts space from the left */
  margin: 0; /* Removes default margin */
}

.v-card__subtitle ul li {
  display: block; /* Ensures each item appears on a new line */
  text-align: left; /* Left-aligns the text */
}


</style>
