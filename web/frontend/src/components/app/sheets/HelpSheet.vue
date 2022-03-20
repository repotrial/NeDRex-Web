<template>
  <v-sheet dark :color="color" style="max-height: 90vh; overflow-y: auto">
    <v-list>
      <v-list-item>
        <v-list-item-title>
          Need help?
        </v-list-item-title>
      </v-list-item>
      <v-list-item>
        <v-list-item-content>
          You can either write an email, check the tutorial videos or refer to the documentation sheet:
        </v-list-item-content>
      </v-list-item>

      <v-list-item>
        <v-list-item-icon>
          <v-icon left>fas fa-at</v-icon>
        </v-list-item-icon>
        <v-list-item-title>By Mail:</v-list-item-title>
        <v-list-item-subtitle><v-chip outlined><a style="text-decoration: none; color: white" :href="'mailto:andreas.maier-1@uni-hamburg.de?subject='+getMailSubject()">andreas.maier-1(_at_]uni-hamburg.de</a>
          <v-icon right small>fas fa-envelope</v-icon>
        </v-chip>
        </v-list-item-subtitle>
      </v-list-item>

      <v-list-item>
        <v-list-item-icon>
          <v-icon left>fab fa-youtube</v-icon>

        </v-list-item-icon>
        <v-list-item-title>Tutorial playlist:</v-list-item-title>
        <v-list-item-subtitle>
          <v-chip outlined
                  @click="openExternal('https://youtube.com/playlist?list=PLY5U8UsdXZRncOAO5f9QjCMZSbO65WKfV')">
            YouTube
            <v-icon small right>fas fa-external-link-alt</v-icon>
          </v-chip>
        </v-list-item-subtitle>
      </v-list-item>
      <v-list-item>
        <v-list-item-icon>
          <v-icon left>fas fa-file-word</v-icon>

        </v-list-item-icon>
        <v-list-item-title>Documentation page:</v-list-item-title>
        <v-list-item-subtitle>
          <v-chip outlined
                  @click="openExternal('https://docs.google.com/document/d/1BGp0wovJk_ERonojc6s9XQ7W2sV_Bgb-ljtjI7yj2bg/edit?usp=sharing')">
            Google-Docs
            <v-icon small right>fas fa-external-link-alt</v-icon>
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
