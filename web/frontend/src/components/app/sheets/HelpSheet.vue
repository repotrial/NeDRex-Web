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
        <v-list-item-subtitle>maieran@cip.ifi.lmu.de</v-list-item-subtitle>
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
