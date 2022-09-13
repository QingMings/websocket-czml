package com.yhl.websocket_czml;

import com.yhl.websocket_czml.helper.Helper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.stomp.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.Arrays;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    StompServer stompServer = StompServer.create(vertx, new StompServerOptions()
      .setHeartbeat(
        new JsonObject().put("x", 1000).put("y", 1000))
      .setPort(-1)
      .setWebsocketBridge(true)
      .setWebsocketPath("/stomp")).handler(
      StompServerHandler.create(vertx)
        .destinationFactory((v, name) -> {
          if (name.startsWith("/queue")) {
            return Destination.queue(vertx, name);
          } else {
            return Destination.topic(vertx, name);
          }
        })
        .connectHandler(serverFrame -> {
          Helper.printStompHeader(serverFrame.frame().getHeaders(),"projectName","clientId","thinkId");
          System.out.println(serverFrame.frame().getReceipt());
        })
    );


    HttpServer server = vertx.createHttpServer(new HttpServerOptions()
      .setWebSocketSubProtocols(Arrays.asList("v10.stomp", "v11.stomp")));
    Router router = Router.router(vertx);
    router.route("/*").handler(StaticHandler.create());


    server.requestHandler(router).webSocketHandler(stompServer.webSocketHandler()).listen(8080);
  }

}
