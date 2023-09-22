<template>
  <v-tooltip top>
    <template v-slot:activator="{attrs, on}">
      <v-btn style="margin-left: 5px; margin-right: 5px;" v-on="on" v-bind="attrs" color="error" @click="verify=true"
             :disabled="disabled">
        <v-icon left>fas fa-times</v-icon>
        <v-divider vertical style="border-color: white; margin-right: 5px;"></v-divider>
        {{ label }}
      </v-btn>
    </template>
    <span>Return to the pipeline selection and reset all progress!</span>
    <v-dialog
      v-model="verify"
      persistent
      style="z-index: 1001"
      max-width="500"
    >
      <v-card>
        <v-card-title>Confirm Navigation</v-card-title>
        <v-card-text>Do you really ant to reset the current page?
        </v-card-text>
        <v-divider></v-divider>

        <v-card-actions style="display: flex; justify-content: flex-end">
          <v-btn style="margin-left: 5px; margin-right: 5px;" color="primary"
                 @click="verify = false">
            <v-icon left>fas fa-angle-left</v-icon>
            <v-divider vertical style="border-color: white; margin-right: 5px;"></v-divider>
            Stay
          </v-btn>
          <v-btn style="margin-left: 5px; margin-right: 5px;" color="error"
                 @click="verify = false; $emit('click','cancel')">
            <v-icon left>fas fa-times</v-icon>
            <v-divider vertical style="border-color: white; margin-right: 5px;"></v-divider>
            {{ label }}
          </v-btn>
        </v-card-actions>
      </v-card>

    </v-dialog>
  </v-tooltip>
</template>

<script>
export default {
  name: "ButtonCancel",

  props: {
    label: {
      type: String,
      default: "RESTART"
    },
    disabled: {
      type: Boolean,
      default: false,
    }
  },
  data() {
    return {
      verify: false,
    }
  },
}
</script>

<style scoped>

</style>
