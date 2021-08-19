<template>
  <div style="margin-left: 5px;">
    <v-menu top offset-y transition="slide-y-reverse-transition">
      <template v-slot:activator="{on,attrs}">
        <v-btn small outlined right v-bind="attrs" v-on="on">
          <v-icon left color="primary">
            fas fa-trash-alt
          </v-icon>
          Remove
        </v-btn>
      </template>
      <v-list style="font-size: smaller; color: gray" dense>
        <v-list-item @click="$emit('clearEvent')" style="cursor:pointer">
          All
        </v-list-item>
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
                <v-list-item style="cursor:pointer; font-size: smaller; color: gray" :key="'only_'+attribute+'-'+value" @click="$emit('removeEvent',{all:false,attribute:attribute, value:value})">
                  <v-icon left size="1em">
                    fas fa-trash-alt
                  </v-icon>
                  with only {{ getLabel(value) }}
                </v-list-item>
                <v-list-item style="cursor:pointer; font-size: smaller; color: gray" :key="'all_'+attribute+'-'+value" @click="$emit('removeEvent',{all:true,attribute:attribute, value:value})">
                  <v-icon left size="1em">
                    fas fa-trash-alt
                  </v-icon>
                  all with {{ getLabel(value) }}
                </v-list-item>
              </template>
            </v-list>
          </v-menu>
        </template>
        <v-list-item @click="$emit('intersectionEvent')" style="cursor:pointer">
          With single origin
        </v-list-item>
      </v-list>
    </v-menu>
  </div>
</template>

<script>
export default {
  name: "seedRemove",
  props: {
    attributes: Object,
  },


  methods:{
    getLabel: function(attr){
     if(attr.indexOf(":")===-1)
       return attr;
     let sp = attr.split(":")
      return sp[1]+" ("+sp[0]+" connection)"
    }
  }
}
</script>

<style scoped>

</style>
