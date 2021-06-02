<template>
  <v-list-item dense style="max-width: 50vw">
    <v-list-item-avatar tile>
      <v-icon>{{ getIcon(data.type) }}</v-icon>
    </v-list-item-avatar>
    <v-list-item-content>
      <v-list-item-title>{{ data.key }}</v-list-item-title>
      <v-list-item-subtitle>{{data.type}}</v-list-item-subtitle>
      <v-list-item-subtitle>
        <span style="font-size: smaller">for <i>'{{ data.text }}'</i></span>
      </v-list-item-subtitle>
    </v-list-item-content>
    <v-list-item-action>
      <v-chip>
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
