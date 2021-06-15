<template>
  <div style="margin-left: 5px;">
    <v-menu top offset-y transition="slide-y-reverse-transition">
      <template v-slot:activator="{on,attrs}">
        <v-btn small outlined right v-bind="attrs" v-on="on">
          <v-icon left color="primary">
            fas fa-trash-alt
          </v-icon>
          Remove
        </v-btn>
      </template>
      <v-list style="font-size: smaller; color: gray" dense>
        <v-list-item @click="$emit('clearEvent')" style="cursor:pointer">
          All
        </v-list-item>
        <v-list-item @click="$emit('intersectionEvent')" style="cursor:pointer">
          With one origin only
        </v-list-item>
      </v-list>
    </v-menu>
  </div>
</template>

<script>
export default {
  name: "seedRemove"
}
</script>

<style scoped>

</style>
