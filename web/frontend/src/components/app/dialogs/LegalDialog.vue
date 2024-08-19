<template>
  <v-dialog
    v-model="model"
    :persistent="!alreadyAccepted()"
    max-width="600"
    style="z-index: 1001;"
    scrollable
  >
    <v-sheet height="60vh">
      <v-card>
        <v-card-title>Legal Notice</v-card-title>
        <v-divider></v-divider>
        <div style="max-height: calc(60vh - 118px); overflow-y: auto">
          <template v-if="!$cookies.get('cookies')">
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
          </template>
          <template v-if="licence!=null" v-for="entry in licence">
            <v-card-title>{{ entry.title }}</v-card-title>
            <v-card-text>
              <div v-html="entry.content" style="text-align: justify"></div>
            </v-card-text>
          </template>
          <v-card-text>
            <div
              style="justify-self: center; margin-left: auto; margin-right: auto; padding-left: 10px; padding-right: 10px">
              <b>This project has received funding from the European Union’s Horizon 2020 research and innovation
                programme under grant agreement No 777111. This publication reflects only the authors' view and the
                European Commission is not responsible for any use that may be made of the information it contains. This
                project is funded by the European Union under grant agreement No. 101057619. Views and opinions
                expressed are however those of the author(s) only and do not necessarily reflect those of the European
                Union or European Health and Digital Executive Agency (HADEA). Neither the European Union nor the
                granting authority can be held responsible for them. This work was also partly supported by the Swiss
                State Secretariat for Education, Research and Innovation (SERI) under contract No. 22.00115.</b>
            </div>
            <div style="display: flex; justify-content: center">
              <div style="width: 500px; height: 75px; display: flex; ">
                <div style="margin: auto; display: flex">
                  <img :src="$config.STATIC_PATH+'/assets/EU-emblem.png'" :height="50"
                       style="align-self: center; margin-top: auto; margin-bottom: auto"/>
                  <img :src="$config.STATIC_PATH+'/assets/cropped-repo-trial_logo.png'" :height="50"
                       style="align-self: center; background-color: white; margin: auto 15px; padding: 5px"/>
                  <img :src="$config.STATIC_PATH+'/assets/REPO4EU-logo-main.png'" :height="50"
                       style="align-self: center; margin-top: auto; margin-bottom: auto; background-color: white; padding: 5px"/>
                </div>
              </div>
            </div>
          </v-card-text>
        </div>
        <v-divider></v-divider>
        <v-card-actions style="justify-content: center; display: flex">
          <v-btn style="margin-left: 5px; margin-right: 5px;" color="success"
                 @click="resolveDialog()">
            <v-icon left>fas fa-check</v-icon>
            <v-divider vertical style="border-color: white; margin-right: 5px;"></v-divider>
            Accept
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
      if (!this.alreadyAccepted()) {
        sessionStorage.setItem("tos", "true")
      }
      if (!this.$cookies.get('cookies'))
        this.$emit("acceptCookiesEvent")
      if (this.$cookies.get("uid") == null) {
        this.$emit("initUserEvent")
      }
    },
    loadLegalText: function () {
      this.$http.getLicense().then(data => {
        this.licence = data;
      })
    },
    show: function () {
      this.model = true;
    },
    alreadyAccepted: function () {
      return sessionStorage.getItem("tos") != null
    }
  }
}
</script>

<style scoped>

</style>
