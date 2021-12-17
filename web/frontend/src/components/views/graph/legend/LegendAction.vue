<template>
  <v-list-item-action>
    <v-tooltip bottom>
      <template v-slot:activator="{on,attrs}">
        <v-btn icon :color="color" :disabled="disabled"
               @click="$emit('click')" v-on="on" v-bind="attrs" style="height:14px; width:14px;">
          <v-icon size="14px">{{ icon }}</v-icon>
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
    disabled:{
      default: false,
      type: Boolean,
    },
    color: String,
    tooltip: String,
    icon: String,
  }
}
</script>

<style scoped>

.v-list-item__action {
  margin-left: 0 !important;
  margin-right: 0;
  margin-top:0;
  margin-bottom:0;
}

</style>
