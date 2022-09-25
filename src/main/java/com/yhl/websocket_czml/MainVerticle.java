package com.yhl.websocket_czml;

import com.yhl.websocket_czml.handlers.OrbitListHandler;
import com.yhl.websocket_czml.handlers.ToBusMessageHandler;
import com.yhl.websocket_czml.handlers.UserNamePasswordAuth;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.auth.authorization.AuthorizationProvider;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.stomp.*;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.bytedeco.libtleprop.global.libtleprop;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MainVerticle extends AbstractVerticle {

  private Map<String,StompServerConnection> websocketClient = new HashMap<>();

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    // 初始化dll
    libtleprop.InitializationConst();
    // 初始化 jodaTime
    new DateTime();
    new LocalDateTime();
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    LocalMap<Destination,String> destinations = vertx.sharedData().getLocalMap("stomp.destinations");

    AuthenticationProvider authorizationProvider = new UserNamePasswordAuth();
    StompServerHandler stompServerHandler = StompServerHandler.create(vertx)
      .authProvider(authorizationProvider)
      .bridge(new BridgeOptions()
        .addInboundPermitted(new PermittedOptions().setAddress("/toBus"))
        .addOutboundPermitted(new PermittedOptions().setAddress("/toStomp"))
      )
      .destinationFactory((v, name) -> {
        System.out.println(name);
        if (name.startsWith("/queue")) {
          System.out.println();
          return Destination.queue(vertx, name);
        } else {
          return Destination.topic(vertx, name);
        }
      });


    StompServer stompServer = StompServer.create(vertx, new StompServerOptions()
      .setHeartbeat(
        new JsonObject().put("x", 1000).put("y", 1000))
      .setPort(-1)
        .setSecured(true)
      .setWebsocketBridge(true)
      .setWebsocketPath("/stomp")).handler(stompServerHandler);

    HttpServer server = vertx.createHttpServer(new HttpServerOptions()
      .setWebSocketSubProtocols(Arrays.asList("v10.stomp", "v11.stomp","v12.stomp")));
    Router router = Router.router(vertx);
    Route orbitListRoute = router.get("/orbit/list");
    orbitListRoute.handler(new OrbitListHandler());
    router.route("/*").handler(StaticHandler.create());
    server.requestHandler(router).webSocketHandler(stompServer.webSocketHandler()).listen(8081,"127.0.0.1");

    AtomicInteger atomicInteger = new AtomicInteger(0);
//    vertx.setPeriodic(5000,l->{
//      vertx.eventBus().publish("/queue/czml", JsonObject.of("fromServer",atomicInteger.incrementAndGet()).toBuffer());
////      vertx.eventBus().sender()
//    });
    vertx.eventBus().consumer("/toBus",new ToBusMessageHandler(vertx));
    startPromise.complete();
  }
}
