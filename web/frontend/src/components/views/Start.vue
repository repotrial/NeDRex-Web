<template>
  <div style="margin-top:20px">
    <v-card v-show="showStartSelection">
      <v-list>
        <v-list-item>
          <v-list-item-title class="title">Select your exploration path</v-list-item-title>
        </v-list-item>
        <a ref="top"></a>
        <div class="v-card__subtitle">
          Select the method to start exploring the NeDRex graph. For immediate and easy algorithmic discovery
          pipelines use the <i>Quick Start</i> page. <i>Guided Exploration</i> may be used for the creation of
          graphs based on some specific path through the metagraph and the derivation of induced graphs. In the <i>Advanced
          Exploration</i> graphs can be freely constructed.
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
<!--    <v-container v-show="startTab===1">-->
<!--      <v-card class="mx-auto">-->
<!--        <v-list>-->
<!--          <v-list-item>-->
<!--            <v-list-item-title class="title">-->
<!--              Guided exploration-->
<!--            </v-list-item-title>-->
<!--          </v-list-item>-->
<!--          <v-list-item>-->
<!--            <div class="v-card__subtitle">-->
<!--              Use the Guided Exploration Start to create a graph based on specific start and target nodes types. Select-->
<!--              a path connecting these metanodes and control the result through additional parameters.-->
<!--            </div>-->
<!--          </v-list-item>-->
<!--        </v-list>-->
<!--      </v-card>-->
<!--    </v-container>-->
    <Quick v-if="startTab===0" :metagraph="metagraph" @printNotificationEvent="printNotification"
           @graphLoadEvent="loadGraphNewTab" @focusEvent="focusTop" @clearURLEvent="$emit('clearURLEvent','quick')"
           ref="quick" @showStartSelectionEvent="toggleStartSelection"></Quick>
    <Guided v-if="startTab===1" :metagraph="metagraph" @printNotificationEvent="printNotification"
            @graphLoadEvent="loadGraphNewTab" @clearURLEvent="$emit('clearURLEvent', 'guided')" ref="guided"></Guided>


    <Advanced ref="advanced" v-if="startTab===2"
              :metagraph="metagraph" :options="options" :colors="colors" :filters="filters"
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
    metagraph: Object,
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

    toggleStartSelection: function(bool){
      this.showStartSelection=bool
    },

    direction: function (edge) {
      if (this.$utils.isEdgeDirected(this.metagraph, edge))
        return 1
      return 0
    },
    getColoring: function (entity, name) {
      return this.$utils.getColoring(this.metagraph, entity, name)
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
