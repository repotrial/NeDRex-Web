import {Client, Stomp} from "@stomp/stompjs";
import SockJS from 'sockjs-client';
import mitt from "mitt";
import * as CONFIG from "../Config"

const emitter = mitt();

const subscriptions = {};

const client = new Client()
client.configure({
  /* uses SockJS as websocket */
  webSocketFactory: function () {
    console.log("Try opening Websocket connection!")
    return new SockJS(CONFIG.SOCKET_URL+CONFIG.CONTEXT_PATH+"/jobs")
    // return new WebSocket(CONFIG.SOCKET_URL+CONFIG.CONTEXT_PATH+"/jobs")
  },
  onConnect: function (frame) {
    console.log("connected!");
  },
  onDisconnect: function (frame) {
    console.log("disconnected!");
  },
  onStompError: function (frame) {
    console.error("ERROR!");
    console.error(frame);
  }
});
client.activate();
console.log("[ INFO ] Communicator initialized to "+CONFIG.HOST_URL+CONFIG.CONTEXT_PATH+"/jobs");

function init() {
  return new Promise(function (resolve, reject) {
    (function waitForConnect() {
      if (client.connected) return resolve();
      setTimeout(waitForConnect, 30);
    })();
  });
}

function print(message) {
  console.log(message)
}

function subscribe(route, event) {
  init().then(() => {
    subscriptions[route] = client.subscribe(route, message => {
      emitter.emit(event, message.body)
    })
  })
}

function unsubscribe(route) {
  subscriptions[route].unsubscribe()
}

function subscribeJob(id, event) {
  let route = "/graph/status-job" + id
  subscribe(route, event)
}

function unsubscribeJob(id) {
  try {
    unsubscribe("/graph/status-job" + id)
  } catch (e) {
    console.warn("Not subscribed to "+id)
  }
}

function subscribeThumbnail(id, event) {
  let route = "/graph/status-thumbnail_"+id
  subscribe(route, event)
}

function unsubscribeThumbnail(id) {
  unsubscribe("/graph/status-thumbnail_"+id)
}

export default {
  ...emitter,
  init,
  print,
  subscribe,
  unsubscribe,
  subscribeJob,
  unsubscribeJob,
  subscribeThumbnail,
  unsubscribeThumbnail,
}
