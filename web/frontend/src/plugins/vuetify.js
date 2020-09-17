import Vue from 'vue'
import Vuetify from 'vuetify'
import '@fortawesome/fontawesome-free/css/all.css' // Ensure you are using css-loader
// import { library } from '@fortawesome/fontawesome-svg-core'
import 'vuetify/dist/vuetify.min.css'
// import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
// import { fas } from '@fortawesome/free-solid-svg-icons'

// import { faSvg } from '@fortawesome/fontawesome-svg-core'


// Vue.component('font-awesome-icon', FontAwesomeIcon)

Vue.use(Vuetify)
// library.add(fas)
const opts = {
  icons: {
    iconfont:'fa',
    // iconfont: 'faSvg',
    // values:{
    //   search:'fas fa-search',
    //   filter:'fas fa-filter',
    // }
  },
}

export default new Vuetify(opts)
