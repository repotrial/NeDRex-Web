import { createApp } from 'vue'
import Page from './Page.vue'

import router from './router'
import vuetify from './plugins/vuetify'
import VueCookies from "vue-cookies";

import * as CONFIG from "./Config"
import Utils from "./scripts/Utils"
import ApiService from "./services/api.service";
// import NedrexService from "./services/nedrex.service"
import Socket from "./services/socket";

ApiService.init(CONFIG.HOST_URL+CONFIG.CONTEXT_PATH+"/api/")
// ApiService.setNedrex(CONFIG.NEDREX_API)

const app = createApp(Page)

app.config.globalProperties.$http = ApiService;
app.config.globalProperties.$global = {metagraph: undefined, metadata: undefined}
app.config.globalProperties.$socket = Socket
app.config.globalProperties.$utils = Utils
app.config.globalProperties.$config = CONFIG

app.use(VueCookies);
VueCookies.config("10000d")

app.use(router)
app.use(vuetify)
router.isReady().then(() => app.mount("#app"))
