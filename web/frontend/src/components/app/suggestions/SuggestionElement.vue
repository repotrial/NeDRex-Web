<template>
  <v-list-item dense style="width: 25vw; max-width: 35vw; max-height: 5vh">
    <v-list-item-avatar tile style="margin-left: -20px">
      <v-tooltip right :open-delay="styling.tooltipDelay">
        <template v-slot:activator="{on, attrs}">

          <v-icon size="25" v-on="on" v-bind="attrs">{{ getIcon(data.type) }}</v-icon>
        </template>
        <span>{{ data.type }}</span>
      </v-tooltip>
    </v-list-item-avatar>
    <v-list-item-content style="width: 20vw; max-width: 30vw">
      <v-tooltip bottom :open-delay="styling.tooltipDelay">
        <template v-slot:activator="{on, attrs}">
          <v-list-item-title style="font-size: medium" v-bind="attrs" v-on="on">{{ data.text }}</v-list-item-title>
        </template>
        <span>{{ data.text }}</span>
      </v-tooltip>
      <v-list-item-subtitle>
        <v-tooltip top :open-delay="styling.tooltipDelay">
          <template v-slot:activator="{on, attrs}">
            <span style="font-size: smaller; overflow-y:auto"><b>{{ data.type.replaceAll("_", " ") }}</b>: <i v-on="on"
                                                                                                              v-bind="attrs">'{{
                data.key
              }}'</i></span>
          </template>
          <span>{{ data.key }}</span>
        </v-tooltip>

      </v-list-item-subtitle>
    </v-list-item-content>
    <v-list-item-action style="margin-right: -10px">
      <v-chip pill>
        {{ data.size }}
      </v-chip>
    </v-list-item-action>
  </v-list-item>
</template>

<script>
export default {
  props: {
    data: Object,
    total: Number,
  },
  name: "SuggestionElement",

  data(){
    return{
        styling:{tooltipDelay:400}
    }
  },


  methods: {
    getIcon: function (type) {
      let icon = 'fas fa-question'
      switch (type) {
        case 'ID':
          icon = 'fas fa-fingerprint'
          break;
        case 'NAME' || 'SYMBOLS' :
          icon = 'fas fa-tv'
          break;
        case 'ICD10':
          icon = 'fas fa-disease'
          break;
        case 'SYNONYM' :
          icon = 'fas fa-sync'
          break;
        case 'IUPAC':
          icon = 'mdi-molecule'
          break;
        case 'ORIGIN' || 'CHROMOSOME' :
          icon = 'fas fa-dna'
          break;
        case 'UMBRELLA_DISORDER' :
          icon = 'fas fa-umbrella'
          break;
        case 'DESCRIPTION' || 'COMMENTS':
          icon = 'fas fa-info'
          break;

        case 'INDICATION':
          icon = 'fas fa-pills'
          break;
        case 'TYPE' || 'GROUP' || 'CATEGORY':
          icon = 'fas fa-layer-group'
          break;
      }
      return icon;
    }
  }

}
</script>

<style scoped>

</style>
