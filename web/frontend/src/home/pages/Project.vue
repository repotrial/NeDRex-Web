<template>
  <v-card dark :color="bgColor" elevation="5">
    <v-card-title>
      Application
    </v-card-title>
    <v-card-text>
      TODO general blabla
    </v-card-text>
    <v-card-title>NeDRex-Web</v-card-title>
    <v-card-text>
      <div>TODO specific blabla</div>
      <div>
        <v-chip outlined :color="color" @click="$router.push('/explore'); $router.go()">Web-App
          <v-icon right>fas fa-angle-right</v-icon>
        </v-chip>
      </div>
    </v-card-text>
    <v-card-title>NeDRex</v-card-title>
    <v-card-text>
      <div>TODO specific blabla</div>
      <div>
        <v-chip outlined :color="color"><a :style="{textDecoration: 'none', color: color}" href="https://nedrex.net" target="_blank">NeDRex</a>
          <v-icon right>fas fa-angle-right</v-icon>
        </v-chip>
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

<style scoped>

</style>
