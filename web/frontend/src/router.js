import Vue from "vue";
import Router from "vue-router";
import App from "./App.vue"

Vue.use(Router);

const router = new Router({
    mode: "history",
    base: "/nedrex",
    routes: [
      {path:"/explore/:view/:tab/:gid", component:App},
      {path:"/", redirect:"/home"},
      {path: "/home", component: App}
    ]
  },

);

export default router
