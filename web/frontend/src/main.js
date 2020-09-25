import Vue from 'vue'
import App from './App.vue'

import router from  './router'
import vuetify from './plugins/vuetify'

import 'babel-polyfill'
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { library } from '@fortawesome/fontawesome-svg-core'
import { faUserSecret } from '@fortawesome/free-solid-svg-icons'
import { Network } from "vue-vis-network";
library.add(faUserSecret)

Vue.component('font-awesome-icon', FontAwesomeIcon)
Vue.component('network', Network);

import ApiService from "./services/api.service";
ApiService.init("/backend/api/");
Vue.prototype.$http = ApiService;


new Vue({
  router,
  vuetify,
  globalVariables: global,
  render: h => h(App)
}).$mount("#app")