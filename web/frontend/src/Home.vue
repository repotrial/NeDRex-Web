<template>
  <!--  <v-app :style="{backgroundColor: colors.main.bg1}">-->
  <v-app :style="{backgroundColor: colors.main.bg1}">
    <div class="content-container">
      <div class="gradient-overlay"></div>
      <div class="content">
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
                <Project :bg-color="colors.main.bg1" :color="colors.main.primary"
                         :cookiesAccepted="cookiesAccepted"></Project>
              </v-tab-item>
              <v-tab-item>
                <Help :bg-color="colors.main.bg1" :color="colors.main.primary"
                      :cookiesAccepted="cookiesAccepted"></Help>
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
            <v-container>
              <v-row justify="center">
                <v-col>
                  <div><i>This page stores browser cookies to improve the user experience. To continue you have to
                    confirm
                    once
                    that you comply with NeDRex-Web setting browser cookies.</i></div>
                  <div>
                    <v-btn small outlined @click="acceptedCookies()" style="margin-left: 8px">
                      <v-icon left>fas fa-check</v-icon>
                      OK
                    </v-btn>
                  </div>
                </v-col>
              </v-row>
            </v-container>
          </v-sheet>
        </v-bottom-sheet>
      </div>
    </div>
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
      video: false,
      anchors: [
        '#app',
        '#help',
        '#about',
        '#cite'
      ]
    }
  },

  created: function () {
    this.colors = {
      main: {bg1: '#383838', bg2: '#dedede', primary: '#35d0d4'},
      buttons: {graphs: {active: "deep-purple accent-2", inactive: undefined}},
      tabs: {active: "#35d0d4", inactive: "white"}
    }
    this.checkAnchor();
    this.cookiesAccepted = this.$cookies.get('cookies') === 'true'
    if (!this.cookiesAccepted)
      this.showCookieConsent = true
  },
  watch: {
    '$route'() {
      this.checkAnchor()
    },
  },

  methods: {
    getConfig: function () {
      return CONFIG;
    },
    checkAnchor: function () {
      let idx = this.anchors.indexOf(this.$route.hash)
      if (idx > -1 && this.$route.path === "/")
        this.tabModel = idx
    },
    acceptedCookies: function () {
      this.$cookies.set('cookies', true)
      this.cookiesAccepted = true;
      this.showCookieConsent = false;
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

.content-container {
  position: relative;
  min-height: 100vh;
}

.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
      to right,
      rgb(237, 137, 244),
      rgb(0, 37, 169) 62%,
      rgb(0, 37, 169) 99%
  );
  opacity: 0.5;
  z-index: 1;
}

.content {
  position: relative;
  z-index: 2;
  /* Add padding or other styles as needed */
}

</style>
