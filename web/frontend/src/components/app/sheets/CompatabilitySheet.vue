<template>
  <v-sheet dark :color="color">
    <v-card>
      <v-card-title>Browser Compatability</v-card-title>
      <v-card-text>
        <CompatabilityTable/>
      </v-card-text>
      <v-card-subtitle>Not limited to but explicitly tested for the following browser and OS versions</v-card-subtitle>
    </v-card>
  </v-sheet>
</template>

<script>

import CompatabilityTable from "@/components/app/tables/CompatabilityTable";
export default {
  name: "BugSheet",
  components: {CompatabilityTable},
  props: {
    color: String,
  },

  methods: {
  }
}
</script>

<style scoped>

</style>
