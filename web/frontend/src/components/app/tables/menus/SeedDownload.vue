<template>
  <div>
    <v-menu bottom offset-y transition="slide-y-transition">
      <template v-slot:activator="{on,attrs}">
        <v-btn x-small outlined right v-bind="attrs" v-on="on">
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
      <v-list style="font-size: smaller; color: rgb(128,128,128)" dense>
        <v-list-item @click="$emit('downloadListEvent')" style="cursor:pointer">
          <v-icon left size="1em">
            fas fa-download
          </v-icon>
          IDs only
        </v-list-item>
        <v-menu right offset-x transition="slide-x-transition" open-on-hover>
          <template v-slot:activator="{on,attrs}">
            <v-list-item v-bind="attrs" v-on="on">
              With names
              <v-icon right>fas fa-caret-right</v-icon>
            </v-list-item>
          </template>
          <v-list dense>
            <v-list-item @click="$emit('downloadListEvent',true,'\t')"
                         style="cursor:pointer; font-size: smaller; color: rgb(128,128,128)">
              <v-icon left size="1em">
                fas fa-download
              </v-icon>
              As .tsv
            </v-list-item>
            <v-list-item @click="$emit('downloadListEvent',true,',')"
                         style="cursor:pointer; font-size: smaller; color: rgb(128,128,128)">
              <v-icon left size="1em">
                fas fa-download
              </v-icon>
              As .csv

            </v-list-item>
          </v-list>
        </v-menu>
      </v-list>
    </v-menu>
  </div>
</template>

<script>
export default {
  name: "SeedDownload"
}
</script>

<style scoped>

</style>
