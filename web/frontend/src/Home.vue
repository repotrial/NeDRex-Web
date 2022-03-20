<template>
  <v-app :style="{backgroundColor: colors.main.bg1}">
    <div>
      <img :src="getConfig().STATIC_PATH+'nedrex-web-logo-transparent.png'" :width="600"
           :style="{'padding-top':'25px', cursor: 'pointer'}" @click="$emit('redirectEvent','/home',true)">
    </div>
    <div style="display: flex; justify-content: center; width: 100%">
      <v-tabs fixed-tabs :color="colors.main.primary" :background-color="colors.main.bg1" dark
              style="max-width: 1000px" v-model="tabModel">
        <v-tab>
          <v-icon left size="16">fas fa-project-diagram</v-icon>
          Application
        </v-tab>
        <v-tab>
          <v-icon left size="16">fas fa-question-circle</v-icon>
          Help
        </v-tab>
        <v-tab>
          <v-icon left size="16">fas fa-info-circle</v-icon>
          About
        </v-tab>
        <v-tab>
          <v-icon left size="16">fas fa-feather</v-icon>
          Cite
        </v-tab>
        <v-tabs-items dark v-model="tabModel">
          <v-tab-item>
            <Project :bg-color="colors.main.bg1" :color="colors.main.primary" :cookiesAccepted="cookiesAccepted"></Project>
          </v-tab-item>
          <v-tab-item>
            <Help :bg-color="colors.main.bg1" :color="colors.main.primary" :cookiesAccepted="cookiesAccepted"></Help>
          </v-tab-item>
          <v-tab-item>
            <About :bg-color="colors.main.bg1" :color="colors.main.primary"></About>
          </v-tab-item>
          <v-tab-item>
            <Cite :bg-color="colors.main.bg1" :color="colors.main.primary"></Cite>
          </v-tab-item>
        </v-tabs-items>
      </v-tabs>
    </div>
    <v-bottom-sheet v-model="showCookieConsent" :overlay-color="colors.main.bg1" style="z-index: 1001">
      <v-sheet :color="colors.main.bg2">
          <v-list-item style="padding: 8px">
            <v-list-item-subtitle>
              <i>This page stores browser cookies to improve the user experience. To continue you have to confirm once that you comply with NeDRex-Web setting browser cookies.</i>
              <v-btn small outlined @click="acceptedCookies()" style="margin-left: 8px"><v-icon left>fas fa-check</v-icon>OK</v-btn>
            </v-list-item-subtitle>
          </v-list-item>
      </v-sheet>
    </v-bottom-sheet>
  </v-app>
</template>

<script>

import * as CONFIG from "./Config"
import Project from "@/home/pages/Project";
import Help from "@/home/pages/Help";
import About from "@/home/pages/About";
import Cite from "@/home/pages/Cite";

export default {
  name: "Home",
  components: {Cite, About, Help, Project},
  data() {
    return {
      showCookieConsent: false,
      cookiesAccepted: false,
      colors: {},
      tabModel: 0,
    }
  },

  created: function () {
    this.colors = {
      main: {bg1: '#383838', bg2:'#dedede', primary: '#35d0d4'},
      buttons: {graphs: {active: "deep-purple accent-2", inactive: undefined}},
      tabs: {active: "#35d0d4", inactive: "white"}
    }
    this.cookiesAccepted = this.$cookies.get('cookies')
    if(!this.cookiesAccepted)
      this.showCookieConsent =true
  },

  methods: {
    getConfig: function () {
      return CONFIG;
    },
    acceptedCookies: function(){
      this.$cookies.set('cookies',true)
      this.cookiesAccepted = true;
      this.showCookieConsent =false;
    }
  }
}
</script>

<style scoped lang="scss">

.v-tab:not(.v-tab--active) {
  color: white !important;

  .v-icon {
    color: white !important;
  }
}

.v-tabs-items {
  padding: 15px;
  background-color: transparent !important;
}

</style>
