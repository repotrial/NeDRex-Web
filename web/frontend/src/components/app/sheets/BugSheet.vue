<template>
  <v-sheet dark :color="color" style="max-height: 90vh; overflow-y: auto">
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
          <v-list-item-subtitle><a :href="'mailto:andreas.maier-1@uni-hamburg.de?subject='+getMailSubject()">andreas.maier-1(at)uni-hamburg.de</a></v-list-item-subtitle>
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
<!--        <v-list-item>-->
<!--          <v-list-item-icon>-->
<!--            <v-icon left>fas fa-table</v-icon>-->
<!--          </v-list-item-icon>-->
<!--          <v-list-item-title>Adding to spreadsheet:</v-list-item-title>-->
<!--          <v-list-item-subtitle>-->
<!--            <v-chip outlined-->
<!--                    @click="openExternal('https://docs.google.com/spreadsheets/d/15ZqaWko_EH4filOLfwC_ZxtEAb-FxJ2sKSPv9MmV75I/edit?usp=sharing')">-->
<!--              Google-Spreadsheet-->
<!--              <v-icon right>fas fa-external-link-alt</v-icon>-->
<!--            </v-chip>-->
<!--          </v-list-item-subtitle>-->
<!--        </v-list-item>-->
        <v-divider style="margin: 15px;"></v-divider>
        <v-list-item>
          <v-list-item-subtitle>UID: {{$cookies.get("uid")? $cookies.get("uid") : "N/A"}}</v-list-item-subtitle>
        </v-list-item>
        <v-list-item>
          <v-list-item-subtitle>GID: {{$route.params.gid ? $route.params.gid : "N/A"}}</v-list-item-subtitle>
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
    },
    getMailSubject: function () {
      let subject = "NeDRex-Web Issue!"
      subject += " (UID=" + this.$cookies.get("uid") + ")"
      if (this.$route.params["gid"])
        subject += " [GID=" + this.$route.params["gid"] + "]"
      return subject
    }
  }
}
</script>

<style scoped>

</style>
