<template>
  <v-footer app dark padless height="35px">
    <v-card
      width="100%"
      flat
      tile
      :color="color"
      class="white--text text-center"
    >
      <v-card-text class="white--text" style="padding-top: 0">
        NeDRex-Web
        <v-btn icon @click="openExternal('https://github.com/AndiMajore/RepoScapeWeb')">
          <v-icon size="22px">fab fa-github</v-icon>
        </v-btn>
        v2.0.0 | {{ new Date().getFullYear() }} | <a href="https://www.cosy.bio/contact" target="_blank"><strong>CoSyBio</strong></a>
      </v-card-text>
    </v-card>

  </v-footer>
</template>

<script>
export default {
  props: {
    color: String

  },

  name: "Footer"
  ,

  methods:{

    openExternal: function (url) {
      window.open(url, '_blank')
    }

  }
}
</script>

<style scoped>

</style>
