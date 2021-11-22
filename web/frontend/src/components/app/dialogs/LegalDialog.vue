<template>
  <v-dialog
    v-model="model"
    :persistent="$cookies.get('uid')==null"
    max-width="600"
    style="z-index: 1001;"
    scrollable
  >
    <v-sheet height="60vh">
      <v-card >
        <v-card-title>Legal Notice</v-card-title>
        <v-divider></v-divider>
        <v-card-subtitle>Cookies</v-card-subtitle>
        <v-card-text>
          <div style="text-align: justify">
            This network browser uses cookies to store an identification for your user. This is used to show
            you
            your previous explorations.
          </div>
        </v-card-text>
        <v-divider></v-divider>
        <v-card-subtitle>Data Licensing</v-card-subtitle>
        <v-card-text v-if="licence!=null">
          <div v-html="licence" style="text-align: justify">
          </div>
        </v-card-text>
        <v-divider></v-divider>

        <v-card-actions>
          <v-btn
            color="green darken-1"
            text
            @click="resolveDialog()"
          >
            {{$cookies.get('uid')==null?'Accept':'Close'}}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-sheet>
  </v-dialog>
</template>

<script>
export default {
  name: "LegalDialog",
  data() {
    return {
      model: false,
      licence: undefined
    }
  },
  created() {
    this.loadLegalText()
  },
  methods: {
    resolveDialog: function () {
      this.model = false;
      // this.$emit("input", this.model)
      if (this.$cookies.get("uid") == null) {
        this.$emit("initUserEvent")
      }
    },
    loadLegalText: function () {
      this.$http.get("getLicence").then(response => {
        if (response.data != null)
          this.licence = response.data.replaceAll("\n", "<br>");
      })
    },
    show: function(){
      this.model = true;
}
  }
}
</script>

<style scoped>

</style>
