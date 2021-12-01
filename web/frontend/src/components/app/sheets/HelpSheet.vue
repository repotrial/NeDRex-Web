<template>
  <v-sheet dark :color="color">
    <v-list>
      <v-list-item>
        <v-list-item-title>
          Need help?
        </v-list-item-title>
      </v-list-item>
      <v-list-item>
        <v-list-item-content>
          You can either write an email or checking resources explaining each page of NeDRex-Web:
        </v-list-item-content>
      </v-list-item>

      <v-list-item>
        <v-list-item-icon>
          <v-icon left>fas fa-at</v-icon>
        </v-list-item-icon>
        <v-list-item-title>By Mail:</v-list-item-title>
        <v-list-item-subtitle><v-chip outlined><a :href="'mailto:andreas.maier-1@uni-hamburg.de?subject='+getMailSubject()">andreas.maier-1(_at_]uni-hamburg.de</a>
        </v-chip>
        </v-list-item-subtitle>
      </v-list-item>

      <v-list-item>
        <v-list-item-icon>
          <v-icon left>fab fa-github</v-icon>

        </v-list-item-icon>
        <v-list-item-title>Application-Example:</v-list-item-title>
        <v-list-item-subtitle>
          <v-chip outlined
                  @click="openExternal('https://github.com/AndiMajore/RepoScapeWeb/tree/master/material/page_explanation')">
            Page-Explanations
            <v-icon size="18" right>fas fa-external-link-alt</v-icon>
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

  methods: {
    openExternal: function (url) {
      window.open(url, '_blank')
    },
    getMailSubject: function () {
      let subject = "Needing help with NeDRex-Web!"
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
