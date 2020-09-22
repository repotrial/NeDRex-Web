import Vue from "vue";
import Router from "vue-router";
import Graph from "./views/Graph.vue"
import Start from "./views/Start.vue"

Vue.use(Router);

const router = new Router({
    mode: "history",
    base: "/backend",
    routes: [
     {path: "/", name: "Graph", component: Graph, props:true},
      {path: "/", name: "Start", component: Start, props:true}
    ]
  },
  // { path:"/",
  //   name: "Welcome",
  //   component: Welcome
  // },

);

export default router
