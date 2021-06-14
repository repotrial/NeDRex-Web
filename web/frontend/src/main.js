
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

import ApiService from "./services/api.service";

ApiService.init(CONFIG.HOST_URL+CONFIG.CONTEXT_PATH+"/api/")
Vue.prototype.$http = ApiService;


import Socket from "./services/socket";
Vue.prototype.$socket = Socket
Vue.prototype.$utils = Utils


new Vue({
  router,
  vuetify,
  globalVariables: global,
  render: h => h(App)
}).$mount("#app")
