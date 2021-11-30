<template>
  <v-list-item>
    <v-list-item-avatar style="margin-top:-12px">
      <v-icon size="20" color="primary">{{ icon }}</v-icon>
    </v-list-item-avatar>
    <v-list-item-action style="min-width: 10rem; max-width: 10rem">
      <v-select v-model="model" :items="items" :label="label" outlined @change="emitEvent" dense></v-select>
    </v-list-item-action>
    <v-list-item-action style="margin-top:-12px">
      <v-tooltip top>
        <template v-slot:activator="{on,attrs}">
          <v-icon size="20" v-on="on" v-bind="attrs">
            far fa-question-circle
          </v-icon>
        </template>
        <slot name="tooltip"></slot>
      </v-tooltip>
    </v-list-item-action>
  </v-list-item>
</template>

<script>
export default {
  name: "ToolDropdown",

  props: {
    items: Array,
    label: String,
    icon: String,
    value: String,
  },

  data() {
    return {
      model: this.value
    }
  },

  methods: {
    emitEvent: function (event) {
      this.$emit("input",this.model)
      this.$emit("change", this.model)
    }
  }
}
</script>

<style scoped>

</style>
