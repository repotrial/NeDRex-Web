<template>
  <div style=" white-space: nowrap; display: inline-flex; align-content: center;">
    <span>
      <span style="color: dimgray">{{ labelOff }}</span>
      <v-tooltip top>
        <template v-slot:activator="{on, attrs}" style="display: inline-block;">
          <span v-bind="attrs" v-on="on">
            <v-switch @change="$emit('input',model)" @click="$emit('click')" v-model="model" :disabled="disabled"
                    style="display: inline-block;margin-left: 10px; margin-top: 0"></v-switch>
          </span>
        </template>
        <slot name="tooltip"></slot>
      </v-tooltip>
      <span style="color: dimgray">{{ labelOn }}</span>
    </span>
  </div>

</template>

<script>
export default {
  name: "LabeledSwitch",
  props: {
    labelOff: String,
    labelOn: String,
    value: Boolean,
    disabled: {
      default: false,
      type: Boolean,
    }
  },

  data() {
    return {
      model: this.value,
    }
  },
}
</script>

<style scoped lang="scss">

span {
  display: inline-block;
  margin-top: 5px;
}

</style>
