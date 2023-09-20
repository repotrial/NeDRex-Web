<template>
  <v-card dark :color="bgColor" elevation="5">
    <v-card-text style="margin-top: 64px;">
<!--      <div style="display: flex; justify-content: center">-->
<!--        <div style="width: 200px; height: 75px; display: flex; ">-->
<!--          <img :src="getConfig().STATIC_PATH+'/assets/EU-emblem.png'" :width="75"-->
<!--               style="align-self: center; margin-top: auto; margin-bottom: auto"/>-->
<!--          <div style="width: 25px"></div>-->
<!--          <img :src="getConfig().STATIC_PATH+'/assets/cropped-repo-trial_logo.png'" :width="75"-->
<!--               style="align-self: center; margin-top: auto; margin-bottom: auto; background-color: white"/>-->
<!--        </div>-->
<!--      </div>-->
<!--      <div-->
<!--        style="justify-self: center; margin-left: auto; margin-right: auto; padding-left: 10px; padding-right: 10px">-->
<!--        <b>This application which is part of REPO-TRIAL project has received funding from the European Union’s Horizon-->
<!--          2020 research and innovation programme under grant agreement No 777111. This reflects only the author’s view-->
<!--          and the European Commission is not responsible for any use that may be made of the information it-->
<!--          contains.</b>-->
<!--      </div>-->

    </v-card-text>

    <v-card-title>About</v-card-title>

    <v-card-text>
      <b>NeDRex-Web</b> is a userfriendly web interface to access data from the NeDRex project. It's main
      focus lies on module identification, drug repurposing and general network exploration. NeDRex-Web utilizes the <b>NeDRexDB</b>
      accessed through the <b>NeDRexAPI</b>.
    </v-card-text>
    <v-card-title>Contact</v-card-title>
    <v-card-text>
      <v-tooltip right>
        <template v-slot:activator="{on, attrs}">
          <v-btn icon><a style="text-decoration: none"
                         href="mailto:andreas.maier-1@uni-hamburg.de?subject=NeDRex-Web%20contact">
            <v-icon v-on="on" v-bind="attrs" color="white">fas fa-envelope</v-icon>
          </a></v-btn>
          andreas.maier-1(_at_]uni-hamburg.de
        </template>
        <div>Open in your default mail client</div>
      </v-tooltip>
    </v-card-text>
    <v-card-title>Disclaimer</v-card-title>
    <v-card-text>
      <b><i>This website is free and open to all users, subject to agreeing with terms of use described in the End User
        License Agreement below, and there is no login requirement.</i></b>
    </v-card-text>

    <template v-for="entry in eula">
      <v-card-title :key="entry.title+'_title'">
        {{ entry.title }}
      </v-card-title>
      <v-card-text :key="entry.title+'_content'">
        <div v-html="entry.content"></div>
      </v-card-text>
    </template>
    <v-card-text>
      <div
        style="justify-self: center; margin-left: auto; margin-right: auto; padding-left: 10px; padding-right: 10px">
        <b>This application which is part of REPO-TRIAL project has received funding from the European Union’s Horizon
          2020 research and innovation programme under grant agreement No 777111. This reflects only the author’s view
          and the European Commission is not responsible for any use that may be made of the information it
          contains.</b>
      </div>
      <div style="display: flex; justify-content: center">
        <div style="width: 200px; height: 75px; display: flex; ">
          <img :src="getConfig().STATIC_PATH+'/assets/EU-emblem.png'" :width="75"
               style="align-self: center; margin-top: auto; margin-bottom: auto"/>
          <div style="width: 25px"></div>
          <img :src="getConfig().STATIC_PATH+'/assets/cropped-repo-trial_logo.png'" :width="75"
               style="align-self: center; margin-top: auto; margin-bottom: auto; background-color: white"/>
        </div>
      </div>
    </v-card-text>
    <v-card-subtitle style="font-size: 1.75rem; color: white">Impressum</v-card-subtitle>
    <v-card-text style="text-align: center !important; margin-top: 15px;">
      <div>Prof. Dr. Jan Baumbach</div>
      <div>Chair of Computational Systems Biology</div>
      <br>
      <div>Phone: +49-40-42838-7313</div>
      <div>E-Mail: <a :href="'mailto:cosyb@zbh.uni-hamburg.de'">cosyb[at)zbh.uni-hamburg.de</a></div>
      <div>Address: Prof. Dr. Jan Baumbach</div>
      <div>University of Hamburg</div>
      <div>Notkestraße 9</div>
      <div>22607 Hamburg</div>
      <div>Germany</div>
    </v-card-text>
  </v-card>
</template>

<script>
import * as  CONFIG from "../../Config"

export default {
  name: "About",
  props: {
    bgColor: String,
    color: String,
  },

  data() {
    return {
      eula: undefined,
    }
  },
  created() {
    if (this.eula == null)
      this.$http.getLicense().then(license => {
        this.eula = license
      })
  },
  methods: {
    getConfig: function () {
      return CONFIG
    }
  }
}
</script>

<style scoped lang="scss">
.v-card__title {
  font-size: 1.75rem;
}

b {
  color: #e5e5e5;
}

.v-card__text {
  font-size: 1rem;
  color: #c9c8c8 !important;
  text-align: justify !important;
}
</style>
