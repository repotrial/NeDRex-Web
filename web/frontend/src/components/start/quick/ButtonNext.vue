<template>
  <v-tooltip top>
    <template v-slot:activator="{attrs, on}">
      <v-btn style="margin-left: 5px; margin-right: 5px;" v-on="on" v-bind="attrs" color="primary" :disabled="disabled" @click="$emit('click','continue')"><v-icon left>fas fa-angle-right</v-icon>{{label}}</v-btn>
    </template>
    <span>Go to the next step!</span>
  </v-tooltip>
</template>

<script>
export default {
  name: "ButtonNext",

  props:{
    label:{
      type:String,
      default: "CONTINUE"
    },
    disabled:{
      type:Boolean,
      default:false,
    }
  }
}
</script>

<style scoped>

</style>
