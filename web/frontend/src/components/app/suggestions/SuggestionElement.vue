<template>
  <v-list-item dense style="width: 25vw; max-width: 35vw; max-height: 5vh">
    <div tile style="display:flex;align-items:center;margin-right:16px;margin-left: -20px">
      <v-tooltip right :open-delay="styling.tooltipDelay">
        <template v-slot:activator="{ props }">

          <v-icon size="25" v-bind="props">{{ getIcon(data.type) }}</v-icon>
        </template>
        <span>{{ data.type }}</span>
      </v-tooltip>
    </div>
    <div style="flex:1 1 auto;overflow:hidden;padding:12px 0;width: 20vw; max-width: 30vw">
      <v-tooltip bottom :open-delay="styling.tooltipDelay">
        <template v-slot:activator="{ props }">
          <v-list-item-title style="font-size: medium" v-bind="props">{{ data.text }}</v-list-item-title>
        </template>
        <span>{{ data.text }}</span>
      </v-tooltip>
      <v-list-item-subtitle>
        <v-tooltip top :open-delay="styling.tooltipDelay">
          <template v-slot:activator="{ props }">
            <span style="font-size: smaller; overflow-y:auto"><b>{{ data.type.replaceAll("_", " ") }}</b>: <i
                                                                                                              v-bind="props">'{{
                data.key
              }}'</i></span>
          </template>
          <span>{{ data.key }}</span>
        </v-tooltip>

      </v-list-item-subtitle>
    </div>
    <v-list-item-action style="margin-right: -10px">
      <span>
        <v-tooltip left>
          <template v-slot:activator="{ props }">
            <v-chip pill style="padding:2px 5px; " v-bind="props">
              {{ data.size }}
            </v-chip>
            </template>
            <span>This entry {{
                data.size === 1 ? ("depicts one specific " + sourceType) : ("combines exactly " + data.size + " " + sourceType + "s")
              }}</span>
          </v-tooltip>
      <template v-if="data.targetCount != null">
        <v-icon style="margin: 3px">fas fa-caret-right</v-icon>
        <v-tooltip left>
           <template v-slot:activator="{ props }">
        <v-chip pill style="padding:2px 5px;" v-bind="props">
          {{ data.targetCount }}
        </v-chip>
           </template>
          <span>This entry {{
              data.targetCount === 0 ? ("is not linked to any " + targetType) : (data.targetCount === 1 ? ("is linked to one "+targetType) :("is linked to " + data.targetCount + " " + targetType + "s"))
            }}</span>
          </v-tooltip>
      </template>
        </span>
    </v-list-item-action>
  </v-list-item>
</template>

<script>
export default {
  props: {
    data: Object,
    total: Number,
    sourceType: String,
    targetType: String,
  },
  name: "SuggestionElement",

  data() {
    return {
      styling: {tooltipDelay: 400}
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
