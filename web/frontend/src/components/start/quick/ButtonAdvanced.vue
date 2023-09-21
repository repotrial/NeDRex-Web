<template>
  <v-tooltip top>
    <template v-slot:activator="{attrs, on}">
      <v-btn style="margin-left: 5px; margin-right: 5px;" v-on="on" v-bind="attrs" color="primary" :disabled="disabled" @click="$emit('click')"><v-icon left>fas fa-angle-double-right</v-icon><v-divider vertical style="border-color: white; margin-right: 5px;"></v-divider>ADVANCED</v-btn>
    </template>
    <span>Load this result into the advanced view, which opens as a new tab.</span>
  </v-tooltip>
</template>

<script>
export default {
  name: "ButtonAdvanced",

  props:{
    disabled:{
      type:Boolean,
      default:false,
    }
  }
}
</script>

<style scoped>

</style>
