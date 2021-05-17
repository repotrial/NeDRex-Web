<template>
  <v-sheet dark :color="color">
    <v-list>
      <v-list-item>
        <v-card-title>DATA SOURCE INFORMATION</v-card-title>
      </v-list-item>
      <v-list-item>
        <v-row>
          <v-col cols="5">
            <v-list>
              <v-list-item>
                <v-list-item-icon>
                  <v-icon left>fas fa-server</v-icon>
                </v-list-item-icon>
                <v-list-item-title>RepoTrialDB-version:</v-list-item-title>
                <span>{{
                    metadata.repotrial && metadata.repotrial.version ? metadata.repotrial.version : "?"
                  }}</span>
              </v-list-item>
              <v-list-item>
                <v-list-item-icon>
                  <v-icon left>fas fa-sync</v-icon>
                </v-list-item-icon>
                <v-list-item-title>Last Check:</v-list-item-title>
                <span>{{
                    metadata.lastCheck !== undefined ? formatTimestamp(metadata.lastCheck)[1] + " ago" : "?"
                  }}</span>
              </v-list-item>
              <v-list-item>
                <v-list-item-icon>
                  <v-icon left>fas fa-cloud-download-alt</v-icon>
                </v-list-item-icon>
                <v-list-item-title>Last Update:</v-list-item-title>
                <span>{{
                    metadata.lastUpdate !== undefined ? formatTimestamp(metadata.lastUpdate)[1] + " ago" : "?"
                  }}</span>
              </v-list-item>
            </v-list>
          </v-col>
          <v-divider vertical></v-divider>
          <v-col cols="6">
            <v-list v-if="metadata.repotrial &&metadata.repotrial.source_databases">
              <v-list-item v-for="source in Object.keys(metadata.repotrial.source_databases)" :key="source">
                <v-list-item-icon>
                  <v-icon left>fas fa-database</v-icon>
                  {{ source }}
                </v-list-item-icon>
                <v-list-item-title>
                  <span>{{ metadata.repotrial.source_databases[source].date }}</span>
                </v-list-item-title>
                <v-list-item-subtitle v-if="metadata.repotrial.source_databases[source].version!=null">
                  <span>(Version: {{ metadata.repotrial.source_databases[source].version }})</span>
                </v-list-item-subtitle>
                <v-list-item-subtitle v-else></v-list-item-subtitle>
              </v-list-item>
            </v-list>
          </v-col>
        </v-row>

      </v-list-item>
    </v-list>
    <v-divider style="margin-left:25px; margin-right: 25px"></v-divider>
  </v-sheet>
</template>

<script>

import Utils from "@/scripts/Utils";

export default {
  name: "BugSheet",

  props: {
    color: String,
    metadata: Object,
  },

  methods: {
    openExternal: function (url) {
      window.open(url, '_blank')
    },
    formatTimestamp(ts) {
      return Utils.formatTime(ts)
    }
  }
}
</script>

<style scoped>

</style>
