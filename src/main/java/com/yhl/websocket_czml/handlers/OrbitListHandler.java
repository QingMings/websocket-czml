package com.yhl.websocket_czml.handlers;

import cn.hutool.core.io.FileUtil;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class OrbitListHandler implements Handler<RoutingContext> {
  @Override
  public void handle(RoutingContext context) {
    String jsonStr =  FileUtil.readUtf8String("testTle.json");
    context.response().putHeader("content-type", "application/json").end(jsonStr);
  }
}
