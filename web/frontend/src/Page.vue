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
  data(){
    return{
      homePage:false
    }
  },

  created() {
    if (this.$route.path === "/" || this.$route.path === "/home")
      this.homePage = true
    console.log(this.homePage)
  }

}
</script>

<style scoped>

</style>
