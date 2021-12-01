<template>
  <v-card dark :color="bgColor" elevation="5">
    <v-card-title style="font-size: xx-large">NeDRex-Web</v-card-title>
    <v-card-text>
      <b>NeDRex-Web</b> is build upon the <b>NeDRex platform</b> and aims to deliver a web-based user interface for
      easy and
      user-friendly access of the NeDRex data. With a focus on <b>drug repurposing</b> we want to support research of
      new
      therapeutic means through <b>network medicine</b> approaches, while giving as much control and exploratory freedom
      as possible to any user.
      <div style=" margin-top: 10px;">
        <v-btn outlined :color="color" @click="$router.push('/explore'); $router.go()">Web-App
          <v-icon right>fas fa-angle-right</v-icon>
        </v-btn>
      </div>
    </v-card-text>
    <v-card-title style="font-size: x-large">NeDRex</v-card-title>
    <v-card-text>
      <div>NeDRex is an interactive network medicine platform for disease module identification and drug repurposing. It
        is build of three main components: a knowledgebase (<b>NeDRexDB</b>), a Cytoscape app (<b>NeDRexApp</b>), and an API
        (<b>NeDRexAPI</b>).
      </div>
      <div>
        <v-btn outlined :color="color" style="margin-top:10px"><a :style="{textDecoration: 'none', color: color}" href="https://nedrex.net"
                                           target="_blank">NeDRex</a>
          <v-icon right>fas fa-angle-right</v-icon>
        </v-btn>
      </div>
    </v-card-text>
  </v-card>
</template>

<script>
export default {
  name: "Project",
  props: {
    bgColor: String,
    color: String,
  },
}
</script>

<style scoped lang="scss">
.v-card__text {
  font-size: 1rem;
  color: #c9c8c8 !important;
  text-align: justify !important;
}
</style>
