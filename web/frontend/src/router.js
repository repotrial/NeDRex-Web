import Vue from "vue";
import Router from "vue-router";
import App from "./App.vue"
import * as CONFIG from "./Config"

Vue.use(Router);

const router = new Router({
    mode: "history",
    base: CONFIG.PATH_PREFIX,
    routes: [
      // {path:"/explore/:view/result/:job", component:App},
      {path:"/explore/:view/:tab/:gid", component:App},
      {path:"/", redirect:"/home"},
      {path: "/home", component: App}
    ]
  },

);

export default router
