<template>
  <div style="margin-left: 5px;">
    <v-menu top offset-y transition="slide-y-reverse-transition">
      <template v-slot:activator="{on,attrs}">
        <v-btn small outlined right v-bind="attrs" v-on="on" :disabled="attributes==null || Object.keys(attributes).length===0">
          <v-icon left color="primary">
            fas fa-filter
          </v-icon>
          Filter
        </v-btn>
      </template>
      <v-list style="font-size: smaller; color: gray" dense>
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
                  keep only {{ value }}
                </v-list-item>
                <v-list-item style="cursor:pointer; font-size: smaller; color: gray" :key="'all_'+attribute+'-'+value" @click="$emit('filterEvent',{all:true,attribute:attribute, value:value})">
                  <v-icon left size="1em">
                    fas fa-filter
                  </v-icon>
                   keep all with {{ value }}
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
  }
}
</script>

<style scoped>

</style>
