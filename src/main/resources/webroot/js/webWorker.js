importScripts("/js/stomp.umd.min.js");

const host = self.location.host;
// stompConfig
const stompConfig = {
  connectHeaders: {
    login: "guest", passcode: "guest"
  },
  brokerURL: "ws://"+host+"/stomp",
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000,
}


const stompClient = new StompJs.Client(stompConfig);

stompClient.onConnect = function (frame) {

  const session = frame.headers.session;
  handlers.toStompHandler({replyType:"Connected"})
  console.log("websocket 连接成功,session:"+session);
  stompClient.subscribe("/toStomp", message => {
    const payload = JSON.parse(message.body);
    handlers.toStompHandler(payload);
  });

};
stompClient.onDisconnect = function () {
  console.log("websocket断开连接")
}
stompClient.onStompError = function (frame) {
  // Will be invoked in case of error encountered at Broker
  // Bad login/passcode typically will cause an error
  // Complaint brokers will set `message` header with a brief message. Body may contain details.
  // Compliant brokers will terminate the connection after any error
  console.log('Broker reported error: ' + frame.headers['message']);
  console.log('Additional details: ' + frame.body);
};
stompClient.debug = function (str) {
  // console.log("STOMP:" + str);
}
stompClient.activate();
// *WebWorker* onmessage implementation
onmessage = function (event) {
  const command = event.data;
  switch (command.commandType) {
    case "ADD_ORBIT":
      console.log("ADD_ORBIT")
      break;
    case "REMOVE_ORBIT":
      break;
    case "START_ORBIT_PREDICTION":
      handlers.startOrbitPrediction(command);
      break;
    case "STOP_ORBIT_PREDICTION":
      handlers.stopOrbitPrediction(command);
      break;
    case "PLUS_SPEED":
      handlers.plusSpeed(command);
      break;
    case "MINS_SPEED":
      handlers.minsSpeed(command);
      break;
    default:
      console.log("no handle found!")
      break
  }
};


const handlers = {
  defaultMessageHandler: function (data){
    stompClient.publish({
      destination: '/toBus',
      body: JSON.stringify(data)
    })
  },
  testMessageHandler: function (data) {
   this.defaultMessageHandler(data);
  },
  toStompHandler: function (data){
    postMessage(data);
  },
  startOrbitPrediction(command) {
    this.defaultMessageHandler(command);
  },
  stopOrbitPrediction(command) {
    this.defaultMessageHandler(command);
  },
  plusSpeed(command) {
    this.defaultMessageHandler(command);
  },
  minsSpeed(command) {
    this.defaultMessageHandler(command);
  }
}
