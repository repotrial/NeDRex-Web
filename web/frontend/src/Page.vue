<template>
  <v-app>
    <Home v-if="homePage"></Home>
    <App v-else></App>
  </v-app>
</template>

<script>
import Home from "@/Home";
import App from "@/App";

export default {
  name: "Page",
  components: {Home, App},
  data() {
    return {
      homePage: false
    }
  },
  watch: {
    '$route'(to, from) {
      if (to === from)
        return
      //TODO think about full always go routing on change to "/explore"
      if ((from.fullPath !== "/" && to.fullPath === "/") || (to.fullPath.startsWith("/explore") && from.fullPath ==="/"))
        this.$router.go()
    }
  },

  created() {
    if (this.$route.path === "/" || this.$route.path === "/home")
      this.homePage = true
  }

}
</script>

<style scoped>

</style>
