<template>
  <div style="margin-top:20px">
    <v-card v-show="showStartSelection">
      <v-list>
        <v-list-item>
          <v-list-item-title class="title">Select your exploration path</v-list-item-title>
        </v-list-item>
        <a ref="top"></a>
        <div class="v-card__subtitle">
          Select the method to start exploring the NeDRex network. For immediate and easy algorithmic discovery
          pipelines use the <i>Quick Start</i> page. <i>Guided Exploration</i> may be used for the creation of
          networks based on some specific path through the metagraph and the derivation of induced graphs. In the <i>Advanced
          Exploration</i> networks can be freely constructed.
        </div>
        <v-divider></v-divider>
        <v-list-item>
          <v-tabs v-model="startTab" centered>
            <v-tabs-slider color="blue"></v-tabs-slider>
            <v-tab @click="checkURLclear('quick')">Quick Start</v-tab>
            <v-tab @click="checkURLclear('guided')">Guided Exploration</v-tab>
            <v-tab @click="checkURLclear('advanced')">Advanced Exploration</v-tab>
          </v-tabs>
        </v-list-item>
      </v-list>
    </v-card>
    <Quick v-if="startTab===0" @printNotificationEvent="printNotification"
           @graphLoadNewTabEvent="loadGraphNewTab" @graphLoadEvent="loadGraph" @focusEvent="focusTop" @clearURLEvent="$emit('clearURLEvent','quick')"
           ref="quick" @showStartSelectionEvent="toggleStartSelection"></Quick>
    <Guided v-if="startTab===1" @printNotificationEvent="printNotification"
            @graphLoadEvent="loadGraph" @graphLoadNewTabEvent="loadGraphNewTab" @clearURLEvent="$emit('clearURLEvent', 'guided')" ref="guided"></Guided>
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
    colors: {
      type: Object
    },
  },
  data() {
    return {
      startTab: 0,
      showStartSelection: true,
    }
  },
  watch: {
    startTab: function (val) {
      this.$emit("showSideEvent", val === 2)
      this.focusTop()
    }
  },
  created() {
    if (this.$route.path.split("/").indexOf("start") > -1)
      this.$emit("showSideEvent", this.startTab === 2)
    this.setView()
  },
  mounted() {
  },
  methods: {
    reset: function () {
      this.resetIndex(this.startTab)
      //TODO try to find out why this in not working
      // this.startTab = 0;
    }
    ,

    resetIndex: function (idx) {
      if (idx === 0 && this.$refs.quick)
        this.$refs.quick.reset()
      if (idx === 1 && this.$refs.guided)
        this.$refs.guided.reset()
      if (idx === 2 && this.$refs.advanced)
        this.$refs.advanced.reset()
    },
    printNotification: function (message, style) {
      this.$emit("printNotificationEvent", message, style)
    },

    checkURLclear: function (view) {
      if (!this.$route.params.gid)
        this.$emit("clearURLEvent", view)
      else
        this.$emit("modifyURLEvent",view)
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

</style>
