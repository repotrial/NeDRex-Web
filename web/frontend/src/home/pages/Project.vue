<template>
  <v-card dark :color="bgColor" elevation="5">
    <v-card-title style="font-size: xx-large">NeDRex-Web</v-card-title>
    <v-card-text style="display: flex">
      <div>
        <b>NeDRex-Web</b> is part of the <b>NeDRex</b> platform which serves as a web-based user interface for easy and
        user-friendly
        access to the <b>NeDRex knowledgebase</b>. With a focus on <b>drug repurposing</b>, our tool supports
        researchers
        to identify new therapeutics using the <b>network medicine algorithms</b> integrated in our platform, while
        giving
        as much control and exploratory power as possible to users.
        <div style=" margin-top: 10px;">
          <v-btn outlined :color="color" @click="$router.push('/explore'); $router.go()">Web-App
            <v-icon right>fas fa-angle-right</v-icon>
          </v-btn>
        </div>
      </div>
      <div style="justify-self: flex-end; margin-right: auto; margin-left: auto; padding-left: 10px; margin-top: -50px">
        <iframe width="360" height="203" src="https://www.youtube.com/embed/NDx2YVN7TpQ" title="YouTube video player"
                frameborder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowfullscreen v-if="cookiesAccepted"></iframe>
        <div v-else style="width: 360px; height: 203px">

        </div>
        <div style="display: flex; justify-content: center"><i style="padding-top: 3px">Go to tutorial playlist:</i>
          <a style="text-decoration: none"
             href="https://www.youtube.com/watch?v=NDx2YVN7TpQ&list=PLY5U8UsdXZRncOAO5f9QjCMZSbO65WKfV" target="_blank">
            <v-btn icon small>
              <v-icon small>fab fa-youtube</v-icon>
            </v-btn>
          </a>
        </div>
      </div>
    </v-card-text>
    <v-card-title style="font-size: x-large">NeDRex</v-card-title>
    <v-card-text>
      <div>NeDRex is an interactive network medicine platform for disease module identification and drug repurposing. It
        is build of three main components: a knowledgebase (<b>NeDRexDB</b>), a Cytoscape app (<b>NeDRexApp</b>), and an
        API
        (<b>NeDRexAPI</b>).
      </div>
      <div>
        <v-btn outlined :color="color" style="margin-top:10px"><a :style="{textDecoration: 'none', color: color}"
                                                                  href="https://nedrex.net"
                                                                  target="_blank">NeDRex</a>
          <v-icon right>fas fa-angle-right</v-icon>
        </v-btn>
      </div>
    </v-card-text>
    <v-card-text style="margin-top: 64px;">
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
  </v-card>
</template>

<script>
import * as CONFIG from "@/Config";

export default {
  name: "Project",
  props: {
    bgColor: String,
    color: String,
    cookiesAccepted : Boolean,
  },
  methods: {
    getConfig: function () {
      return CONFIG
    }
  }
}
</script>

<style scoped lang="scss">
.v-card__text {
  font-size: 1rem;
  color: #c9c8c8 !important;
  text-align: justify !important;
}

b {
  color: #e5e5e5;
}
</style>
