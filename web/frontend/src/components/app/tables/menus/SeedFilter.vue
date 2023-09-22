<template>
  <div style="margin-left: 5px;">
    <v-menu bottom offset-y transition="slide-y-transition">
      <template v-slot:activator="{on,attrs}">
        <v-btn x-small outlined right v-bind="attrs" v-on="on" :disabled="attributes==null || Object.keys(attributes).length===0">
          <v-icon x-small left color="primary">
            fas fa-filter
          </v-icon>
          <v-divider vertical style="border-color: black; margin-right: 5px; margin-top: 3px; margin-bottom: 3px;"></v-divider>
          Filter
          <v-icon right>fas fa-caret-down</v-icon>
        </v-btn>
      </template>
      <v-list style="font-size: smaller; color: rgb(128,128,128)" dense>
        <template v-if="attributes!=null">
          <v-menu right offset-x transition="slide-x-transition" open-on-hover v-for="(list,attribute) in attributes"
                  :key="attribute">
            <template v-slot:activator="{on,attrs}">
              <v-list-item v-bind="attrs" v-on="on">
                By {{ attribute }}
                <v-icon right>fas fa-caret-right</v-icon>
              </v-list-item>
            </template>
            <v-list dense>
              <template v-for="value in list">
                <v-list-item style="cursor:pointer; font-size: smaller; color: gray" :key="'only_'+attribute+'-'+value" @click="$emit('filterEvent',{all:false,attribute:attribute, value:value})">
                  <v-icon left size="1em">
                    fas fa-filter
                  </v-icon>
                  keep only {{ getLabel(value) }}
                </v-list-item>
                <v-list-item style="cursor:pointer; font-size: smaller; color: gray" :key="'all_'+attribute+'-'+value" @click="$emit('filterEvent',{all:true,attribute:attribute, value:value})">
                  <v-icon left size="1em">
                    fas fa-filter
                  </v-icon>
                   keep all with {{ getLabel(value) }}
                </v-list-item>
              </template>
            </v-list>
          </v-menu>
        </template>
      </v-list>
    </v-menu>
  </div>
</template>

<script>
export default {
  name: "SeedFilter",
  props: {
    attributes: Object,
  },

  methods: {

    getLabel: function (attr) {
      if (attr.indexOf(":") === -1)
        return attr;
      let sp = attr.split(":")
      return sp[1] + " (" + sp[0] + " connection)"
    }
  }
}
</script>

<style scoped>

</style>
