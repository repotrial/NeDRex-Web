<template>
  <v-tooltip top>
    <template v-slot:activator="{on, attrs}">
      <v-list-item style="min-height: 2rem; max-height: 2.5rem">
        <v-list-item-avatar>
          <v-icon size="20" :color="model ? 'primary':''">{{icon}}</v-icon>
        </v-list-item-avatar>
        <v-list-item-subtitle>{{label}}</v-list-item-subtitle>
        <v-list-item-action v-on="on" v-bind="attrs">
          <v-switch v-model="model" :disabled="disabled" @change="$emit('input',model)"
                    @click="$emit('click')" dense></v-switch>
        </v-list-item-action>
      </v-list-item>
    </template>
    <slot name="tooltip" v-if="!disabled">
    </slot>
    <slot name="tooltip:disabled" v-if="disabled">
    </slot>
  </v-tooltip>
</template>

<script>
export default {
  name: "ToolSwitch",
  props:{
    value: Boolean,
    icon: String,
    label: String,
    disabled: {
      type: Boolean,
      default: false,
    }
  },

  data (){
    return{
      model:this.value,
    }
  }

}
</script>

<style scoped>

</style>
