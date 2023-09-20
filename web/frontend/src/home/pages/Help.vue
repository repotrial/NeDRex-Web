<template>
  <v-card dark :color="bgColor" elevation="5">
    <v-card-title>
      Help
    </v-card-title>
    <v-card-text>
      <div style="text-align: justify">
        We provide two kinds of helpful materials to make sure you will have the best experience with <b>NeDRex-Web</b>:
      </div>
      <div style="margin-left: 16px; margin-top:8px;text-align: justify">1. To give an overview of the <b>NeDRex-Web
        application</b> we provide a short
        <i><b>video material</b></i>. It provides a <i><b>general introduction</b></i> and three additional <i><b>hands-on
          tutorials</b></i> explaining the three <i><b>different ways of starting</b></i> to explore the
        <b>NeDRex</b> knowledge graph with <b>NeDRex-Web</b>.
      </div>
      <div style="text-align: justify; margin-left: 16px; margin-top:8px;">
        2. For a more detailed documentation of the features and functions of NeDRex-Web please refer to our
        <a :style="{textDecoration: 'none', color:color}"
           href="https://docs.google.com/document/d/1BGp0wovJk_ERonojc6s9XQ7W2sV_Bgb-ljtjI7yj2bg/edit?usp=sharing"
           target="_blank">documentation sheet
          <v-icon :color="color" small style="margin-top: -2px;">fas fa-external-link-alt</v-icon>
        </a>
        <br>
        <i>Please note this document might be updated over time</i>
      </div>
    </v-card-text>
    <v-card-title>
      Video-Tutorials
    </v-card-title>
    <v-card-text style="width: 100%">
      <div style="width: 480px; height: 270px; display: flex; align-items: center; justify-content: center; margin: auto">
        <v-btn @click="video = true" v-if="!video && cookiesAccepted" style="align-self: center; justify-self: center; margin: auto">
          <v-icon left>fab fa-youtube</v-icon>Allow videos
        </v-btn>
      <iframe width="480" height="270" v-if="video" src="https://www.youtube.com/embed/NDx2YVN7TpQ" title="YouTube video player"
              frameborder="0"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowfullscreen></iframe>
      </div>
      <div style="color: white; width: 480px; margin: auto;" v-if="video" >
        Conceptual introduction of network-based drug repurposing with the NeDRex platform
      </div>
      <div style="display: flex; justify-content: center; margin-top:16px;">
        <div style="">
          <iframe  v-if="video" width="240" height="135" src="https://www.youtube.com/embed/lvuS9Bt_T6M" title="YouTube video player"
                  frameborder="0"
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                  allowfullscreen></iframe>
          <div v-else style="width: 240px; height: 135px">

          </div>
          <div style="color: white; width: 240px" v-if="video" >
            First steps in NeDRex-Web application: Quck Start & Drug Repurposing
          </div>
        </div>
        <div>
        <iframe  v-if="video" width="240" height="135" src="https://www.youtube.com/embed/ftLDwNTcLD4" title="YouTube video player"
                frameborder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowfullscreen></iframe>
          <div v-else style="width: 240px; height: 135px">

          </div>
          <div style="color: white; width: 240px"  v-if="video" >
            Application example of NeDRex-Web and Guided Connectivity Search: Induced Networks (Diseasome)
          </div>
        </div>
        <div>
        <iframe v-if="video" width="240" height="135" src="https://www.youtube.com/embed/jiMlZceH70E" title="YouTube video player"
                frameborder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowfullscreen></iframe>
          <div v-else style="width: 240px; height: 135px">

          </div>
          <div style="color: white; width: 240px" v-if="video" >
            Advanced exploration in NeDRex-Web & Research History
          </div>
        </div>
      </div>
    </v-card-text>
    <div style="margin-bottom: 16px"></div>
    <v-card-title>Browser Compatability</v-card-title>
    <div style="display: flex; justify-content: center">
      <CompatabilityTable style="margin-bottom: 20px; width: 650px" bg-color="transparent"/>
    </div>
  </v-card>
</template>

<script>
import CompatabilityTable from "@/components/app/tables/CompatabilityTable";

export default {
  name: "Help",
  components: {
    CompatabilityTable,
  },
  props: {
    bgColor: String,
    color: String,
    cookiesAccepted : Boolean,
  },
  data() {
    return {
      video: false,
    }
  },

}
</script>

<style scoped lang="scss">
b {
  color: #e5e5e5;
}

iframe {
  margin: 4px
}
</style>
