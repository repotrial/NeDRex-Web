
import Vue from 'vue'
import App from './App.vue'

import router from './router'
import vuetify from './plugins/vuetify'
import VueCookies from "vue-cookies";

import 'babel-polyfill'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";

import * as CONFIG from "./Config"
import Utils from "./scripts/Utils"

Vue.component('font-awesome-icon', FontAwesomeIcon)
Vue.use(VueCookies);
Vue.$cookies.config("10000d")
import ApiService from "./services/api.service";
import NedrexService from "./services/nedrex.service"

ApiService.init(CONFIG.HOST_URL+CONFIG.CONTEXT_PATH+"/api/")
ApiService.setNedrex(CONFIG.NEDREX_API)
Vue.prototype.$http = ApiService;


Vue.prototype.$global = {metagraph:undefined, metadata: undefined}

import Socket from "./services/socket";
Vue.prototype.$socket = Socket
Vue.prototype.$utils = Utils
Vue.prototype.$config = CONFIG



new Vue({
  router,
  vuetify,
  globalVariables: global,
  render: h => h(App)
}).$mount("#app")
