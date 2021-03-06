import {Client, Stomp} from "@stomp/stompjs";
import Vue from "vue"
import * as CONFIG from "../Config"


const emitter = new Vue({
  created() {
    this.subscriptions = {};
    this.client = new Client()
    this.client.configure({
      /* uses SockJS as websocket */
      webSocketFactory: function () {
        console.log("ENVIRONMENT: "+process.env.BACKEND_HOST)
        console.log("Try opening connection to "+CONFIG.SOCKET_URL+"/backend/jobs")
        return new WebSocket(CONFIG.SOCKET_URL+"/backend/jobs")
      },
      onConnect: function (frame) {
        console.log("connected!");
        this.isConnected = true;
      },
      onDisconnect: function (frame) {
        console.log("disconnected!");
        this.isConnected = false;
      },
      onStompError: function (frame) {
        console.log("ERROR!");
        console.log(frame);
      }
    });
    this.client.activate();
    console.log("[ INFO ] Communicator initialized to "+CONFIG.HOST_URL+"/backend/jobs");
  },
  methods: {

    init: function () {
      let context = this
      return new Promise(function (resolve, reject) {
        (function waitForConnect() {
          if (context.client.connected) return resolve();
          setTimeout(waitForConnect, 30);
        })();
      });
    },

    print: function (message) {
      console.log(message)
    },

    subscribe: function (route, event) {
      let context = this
      context.init().then(() => {
        context.subscriptions[route] = context.client.subscribe(route, message => {
          this.$emit(event, message.body)
        })
      })
    },

    unsubscribe: function (route) {
      this.subscriptions[route].unsubscribe()
    },

    subscribeJob: function (id, event) {
      let route = "/graph/status-job" + id
      this.subscribe(route, event)
    },
    unsubscribeJob: function (id) {
      this.unsubscribe("/graph/status-job" + id)
    },

    subscribeThumbnail: function(id,event){
      let route = "/graph/status-thumbnail_"+id
      this.subscribe(route,event)
    },

    unsubscribeThumbnail: function(id){
      this.unsubscribe("/graph/status-thumbnail_"+id)
    }
  }
})

export default emitter
