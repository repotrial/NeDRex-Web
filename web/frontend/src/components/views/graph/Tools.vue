<template>
  <v-card ref="tools" elevation="3" style="margin:15px" v-if="metagraph!==undefined && entityGraph!==undefined">
    <v-container v-show="show">
      <v-list ref="list">
        <v-list-item>
          TODO
        </v-list-item>
      </v-list>
    </v-container>
  </v-card>
</template>

<script>

import Utils from "../../../scripts/Utils"

export default {
  props: {
    metagraph: undefined,
    countMap: undefined,
    entityGraph: undefined,
    options: Object,
  },
  name: "Tools",

  data() {
    return {
      show: true,
      tabModel: 0,
      nodeMap: {}
    }
  },
  created() {
    if (this.options.toggled === undefined) {
      this.options.toggled = {}
    }
  },
  methods: {


  }
}
</script>

<style scoped>

</style>
