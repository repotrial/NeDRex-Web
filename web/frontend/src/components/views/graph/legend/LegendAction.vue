<template>
  <v-list-item-action>
    <v-tooltip bottom>
      <template v-slot:activator="{on,attrs}">
        <v-btn icon x-small :color="color"
               @click="$emit('click')" v-on="on" v-bind="attrs">
          <v-icon>{{ icon }}</v-icon>
        </v-btn>
      </template>
      <slot name="tooltip"></slot>
    </v-tooltip>
  </v-list-item-action>
</template>

<script>
export default {
  name: "LegendAction",
  props: {
    color: String,
    tooltip: String,
    icon: String,
  }
}
</script>

<style scoped>

</style>
