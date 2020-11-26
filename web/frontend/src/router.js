import Vue from "vue";
import Router from "vue-router";
import Graph from "./views/Graph.vue"
import Start from "./views/Start.vue"
import List from "./views/List.vue"
import History from "./views/History";
import App from "./App.vue"
import Algorithms from "./views/toolbox/Algorithms";
import Jobs from "./views/toolbox/Jobs"

Vue.use(Router);

const router = new Router({
    mode: "history",
    // base: "/backend",
    routes: [
      // {path: "/:gid/graph", name: "Graph", component: Graph, props: true},
      // {path: "/:gid", name: "Start", component: Start, props: true},
      // {path: "/:gid/list", name: "List", component: List, props: false},
      // {path: "/:gid/history", name: "History", component: History, props: false},
      {path:"/:gid", component: App, redirect:"/:gid/start"},
      {path:"/:gid/:tab", name:"App", component: App, props:false},
      // {path:"/",name:"Algorithms", component:Algorithms,props: false},
      // {path:"/",name:"Jobs",component:Jobs,props:false}
    ]
  },
  // { path:"/",
  //   name: "Welcome",
  //   component: Welcome
  // },

);

export default router
