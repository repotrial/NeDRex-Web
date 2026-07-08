import { createRouter, createWebHistory } from "vue-router";
import * as CONFIG from "./Config"
import Page from "./Page.vue";

const router = createRouter({
  history: createWebHistory(CONFIG.PATH_PREFIX),
  routes: [
    // {path:"/explore/:view/result/:job", component:App},
    // {path:"/", redirect:"/home"},
    {path: "/", component: Page},
    {path: "/explore/:view/:tab/:gid", component: Page}
  ]
});

export default router
