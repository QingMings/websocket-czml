package com.yhl.websocket_czml;

import io.vertx.core.Vertx;

public class Test {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }
}
