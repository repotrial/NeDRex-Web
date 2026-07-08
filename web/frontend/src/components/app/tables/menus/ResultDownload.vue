<template>
  <v-menu bottom offset-y transition="slide-y-transition">
    <template v-slot:activator="{ props }">
      <v-btn variant="outlined" x-small right v-bind="props">
        <v-icon left x-small color="primary">
          fas fa-download
        </v-icon>
        <v-divider vertical style="border-color: black; margin-top: 3px; margin-bottom: 3px;  margin-right: 5px;"></v-divider>
        Save
        <v-icon right x-small>
          fas fa-caret-down
        </v-icon>
      </v-btn>
    </template>
    <v-list style="font-size: smaller; color: gray" dense>
      <v-menu v-if="seeds" right offset-x transition="slide-x-transition" open-on-hover>
        <template v-slot:activator="{ props }">
          <v-list-item v-bind="props">
            {{label}}
            <v-icon right>fas fa-caret-right</v-icon>
          </v-list-item>
        </template>
        <v-list style="color: gray" dense>
          <v-list-item @click="$emit('downloadEvent',true,'\t')"
                       style="cursor:pointer; font-size: smaller; color: gray">
            <v-icon left size="1em">
              fas fa-download
            </v-icon>
            As .tsv
          </v-list-item>
          <v-list-item @click="$emit('downloadEvent',true,',')"
                       style="cursor:pointer; font-size: smaller; color: gray">
            <v-icon left size="1em">
              fas fa-download
            </v-icon>
            As .csv
          </v-list-item>
        </v-list>
      </v-menu>
      <v-menu v-if="results" right offset-x transition="slide-x-transition" open-on-hover>
        <template v-slot:activator="{ props }">
          <v-list-item v-bind="props">
            {{resultLabel}}
            <v-icon right>fas fa-caret-right</v-icon>
          </v-list-item>
        </template>
        <v-list style="color: gray" dense>
          <v-list-item @click="$emit('downloadResultsEvent','\t',true)"
                       style="cursor:pointer; font-size: smaller; color: gray">
            <v-icon left size="1em">
              fas fa-download
            </v-icon>
            As .tsv
          </v-list-item>
          <v-list-item @click="$emit('downloadResultsEvent',',',true)"
                       style="cursor:pointer; font-size: smaller; color: gray">
            <v-icon left size="1em">
              fas fa-download
            </v-icon>
            As .csv
          </v-list-item>
        </v-list>
      </v-menu>
      <v-list-item v-if="raw" @click="$emit('downloadRawEvent')">
        <v-icon left size="1em">
          fas fa-download
        </v-icon>
        {{rawLabel}}
      </v-list-item>
    </v-list>
  </v-menu>
</template>

<script>
export default {
  name: "ResultDownload",

  props:{
    seeds: Boolean,
    results: Boolean,
    resultLabel:{
      default: "Module"
    },
    raw: Boolean,
    rawLabel: {
      default:"Raw Results"
    },
    label:{
      default: "Seeds"
    }
  }
}
</script>

<style scoped>

</style>
