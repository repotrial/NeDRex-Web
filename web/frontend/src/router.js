import Vue from "vue";
import Router from "vue-router";
import App from "./App.vue"
import Home from "@/components/views/Home";

Vue.use(Router);

const router = new Router({
    mode: "history",
    base: "/nedrex",
    routes: [
      // {path:"/:gid", component: App, redirect:"/:gid/start"},
      // {path:"/:gid/:tab", name:"App", component: App, props:false},
      {path:"/explore/:view/:tab/:gid", component:App},
      {path:"/", redirect:"/home"},
      {path: "/home", component: App}
    ]
  },

);

export default router
