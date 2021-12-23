<template>
  <v-tooltip top>
    <template v-slot:activator="{attrs, on}">
      <v-btn style="margin-left: 5px; margin-right: 5px;" v-on="on" v-bind="attrs" :color="!save? 'warning':'primary'" :disabled="disabled" @click="$emit('click','back')"><v-icon left>fas fa-angle-left</v-icon>{{label}}</v-btn>
    </template>
    <span v-if="!save">Return to the last step. This will potentially reset the <br> progress from this site and trigger some re-execution!</span>
    <span v-else>Return to the last step. Progress from this step will not be lost.</span>
  </v-tooltip>
</template>

<script>
export default {
  name: "ButtonBack",
  props:{
    disabled:{
      type:Boolean,
      default:false,
    },
    label:{
      type:String,
      default: "BACK"
    },
    save:{
      type:Boolean,
      default:false
    }
  }
}
</script>

<style scoped>

</style>
