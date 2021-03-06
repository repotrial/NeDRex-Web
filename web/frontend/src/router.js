import Vue from "vue";
import Router from "vue-router";
import App from "./App.vue"

Vue.use(Router);

const router = new Router({
    mode: "history",
    base: "/nedrex",
    routes: [
      {path:"/:gid", component: App, redirect:"/:gid/start"},
      {path:"/:gid/:tab", name:"App", component: App, props:false},
    ]
  },

);

export default router
