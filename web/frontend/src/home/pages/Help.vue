<template>
  <v-card dark :color="bgColor" elevation="5">
    <v-card-title>
      Help
    </v-card-title>
    <v-card-text>
      <div>TODO text intro</div>
    </v-card-text>
    <v-card-title>
      Video-Tutorials
    </v-card-title>
    <v-card-text>
      TODO
      <div style="display: flex; justify-content: center">
        <div style="width: 192px; height: 108px; background-color: black; margin: 10px; color: white">General</div>
        <div style="width: 192px; height: 108px; background-color: black; margin: 10px; color: white">Quick</div>
        <div style="width: 192px; height: 108px; background-color: black; margin: 10px; color: white">Guided</div>
        <div style="width: 192px; height: 108px; background-color: black; margin: 10px; color: white">Advanced</div>
      </div>
    </v-card-text>
  </v-card>
</template>

<script>
export default {
  name: "Help",
  props: {
    bgColor: String,
    color: String,
  },
}
</script>

<style scoped>

</style>
