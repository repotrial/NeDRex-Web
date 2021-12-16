<template>
  <v-card dark :color="bgColor" elevation="5">
    <v-card-title>
      Cite
    </v-card-title>
    <v-card-text>
      <div>Information on how to cite NeDRex-web will be added in the future.</div>
    </v-card-text>
  </v-card>
</template>

<script>
export default {
  name: "Cite",
  props: {
    bgColor: String,
    color: String,
  },
}
</script>

<style scoped>
b {
  color: #e5e5e5;
}
</style>
