<template>
  <v-sheet dark :color="color">
      <v-list>
        <v-list-item>
          <v-list-item-title>
            Found a Bug?
          </v-list-item-title>
        </v-list-item>
        <v-list-item>
          <v-list-item-content>
            Did you find a bug or have other ideas to improve NeDRex-Web? Great! Just let us know using one of the
            following options:
          </v-list-item-content>
        </v-list-item>

        <v-list-item>
          <v-list-item-icon>
            <v-icon left>fas fa-at</v-icon>
          </v-list-item-icon>
          <v-list-item-title>By Mail:</v-list-item-title>
          <v-list-item-subtitle>maieran@cip.ifi.lmu.de</v-list-item-subtitle>
        </v-list-item>

        <v-list-item>
          <v-list-item-icon>
            <v-icon left>fab fa-github</v-icon>

          </v-list-item-icon>
          <v-list-item-title>Filing an Issue:</v-list-item-title>
          <v-list-item-subtitle>
            <v-chip outlined @click="openExternal('https://github.com/AndiMajore/RepoScapeWeb/issues')">
              Github-Page
              <v-icon right>fas fa-external-link-alt</v-icon>
            </v-chip>
          </v-list-item-subtitle>
        </v-list-item>
        <v-list-item>
          <v-list-item-icon>
            <v-icon left>fas fa-table</v-icon>

          </v-list-item-icon>
          <v-list-item-title>Adding to spreadsheet:</v-list-item-title>
          <v-list-item-subtitle>
            <v-chip outlined
                    @click="openExternal('https://docs.google.com/spreadsheets/d/15ZqaWko_EH4filOLfwC_ZxtEAb-FxJ2sKSPv9MmV75I/edit?usp=sharing')">
              Google-Spreadsheet
              <v-icon right>fas fa-external-link-alt</v-icon>
            </v-chip>
          </v-list-item-subtitle>
        </v-list-item>
      </v-list>
    </v-sheet>
</template>

<script>

export default {
  name: "BugSheet",

  props: {
    color: String,
  },

  methods:{
    openExternal: function (url) {
      window.open(url, '_blank')
    }
  }
}
</script>

<style scoped>

</style>
