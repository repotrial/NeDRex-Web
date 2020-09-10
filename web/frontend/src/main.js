import Vue from 'vue'
import App from './App.vue'

import router from  './router'
import vuetify from './plugins/vuetify'

import 'babel-polyfill'
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { library } from '@fortawesome/fontawesome-svg-core'
import { faUserSecret } from '@fortawesome/free-solid-svg-icons'
library.add(faUserSecret)

Vue.component('font-awesome-icon', FontAwesomeIcon)

new Vue({
  router,
  vuetify,
  render: h => h(App)
}).$mount("#app")
