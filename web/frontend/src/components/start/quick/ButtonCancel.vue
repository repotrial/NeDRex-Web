<template>
  <v-tooltip top>
    <template v-slot:activator="{attrs, on}">
      <v-btn style="margin-left: 5px; margin-right: 5px;" v-on="on" v-bind="attrs" color="error" @click="$emit('click','cancel')" :disabled="disabled"><v-icon left>fas fa-times</v-icon>{{label}}</v-btn>
    </template>
    <span>Return to the pipeline selection and reset all progress!</span>
  </v-tooltip>
</template>

<script>
export default {
  name: "ButtonCancel",

  props:{
    label:{
      type:String,
      default: "CANCEL"
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
