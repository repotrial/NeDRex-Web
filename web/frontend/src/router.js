import Vue from "vue";
import Router from "vue-router";
import * as CONFIG from "./Config"
import Page from "./Page.vue";

Vue.use(Router);

const router = new Router({
    mode: "history",
    base: CONFIG.PATH_PREFIX,
    routes: [
      // {path:"/explore/:view/result/:job", component:App},
      // {path:"/", redirect:"/home"},
      {path: "/", component: Page},
      {path:"/explore/:view/:tab/:gid", component:Page}
    ]
  },

);

export default router
