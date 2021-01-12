<template>
  <div style="margin-top:20px">
    <v-card>
      <v-list>
        <v-list-item>
          <v-list-item-title class="title">Select your exploration path</v-list-item-title>
        </v-list-item>
        <v-list-item>
          <v-list-item-subtitle>For new users or basic tasks like drug-target identification, the guided path is
            recommended.
          </v-list-item-subtitle>
        </v-list-item>
        <v-divider></v-divider>
        <v-list-item>
          <v-tabs v-model="selectionTab" centered>
            <v-tabs-slider color="blue"></v-tabs-slider>
            <v-tab>Guided Exploration</v-tab>
            <v-tab>Quick Start</v-tab>
            <v-tab>Advanced Exploration</v-tab>
          </v-tabs>
        </v-list-item>
      </v-list>
    </v-card>

    <v-container v-show="selectionTab===0">
      <v-card class="mx-auto">
        <v-list>
          <v-list-item>
            <v-list-item-title class="title">
              Guided exploration
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-subtitle>
              Selection of a starting point (e.g. Disorder) and then targets of interest (e.g. Drug or Gene/Protein) in
              different steps for easy usage.
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
      </v-card>
    </v-container>

    <Quick v-show="selectionTab===1" :metagraph="metagraph" @printNotificationEvent="printNotification"
        @graphLoadEvent="loadGraph"
    >
    </Quick>


    <Advanced ref="advanced" v-show="selectionTab===2"
              :metagraph="metagraph" :options="options" :colors="colors" :filters="filters"
              @printNotificationEvent="printNotification"
              @graphLoadEvent="loadGraph"
    />

  </div>
</template>

<script>
import Utils from "../scripts/Utils"
import Advanced from "./start/Advanced";
import Quick from "./start/Quick"

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
      selectionTab: 1,
    }
  },
  watch: {
    selectionTab: function (val) {
      this.$emit("showSideEvent", val === 2)
    }
  },
  created() {
    this.$emit("showSideEvent", this.selectionTab === 2)
  },
  mounted() {
  },
  methods: {
    printNotification: function (message, style) {
      this.$emit("printNotificationEvent", message, style)
    },

    executeGraphLoad: function (bool) {
      if (this.selectionTab === 2)
        this.$refs.advanced.loadGraph(bool)
    },

    loadGraph: function (data) {
      this.$emit("graphLoadEvent", data)
    },

    direction: function (edge) {
      if (Utils.isEdgeDirected(this.metagraph, edge))
        return 1
      return 0
    },
    getColoring: function (entity, name) {
      return Utils.getColoring(this.metagraph, entity, name)
    },
    getStartType: function () {
      return this.selectionTab;
    },
  },
  components: {
    Advanced,
    Quick
  },

}
</script>

<style scoped>

</style>
