package com.yhl.websocket_czml.handlers;


import cn.hutool.core.util.StrUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.Credentials;

public class UserNamePasswordAuth implements AuthenticationProvider {
  @Override
  public void authenticate(JsonObject credentials, Handler<AsyncResult<User>> resultHandler) {
    System.out.println("loginUser:" + credentials.encode());
    if (StrUtil.equals("guest", credentials.getString("username")) &&
      StrUtil.equals("guest", credentials.getString("password"))){
      resultHandler.handle(Future.succeededFuture(User.create(credentials)));
    }else {
      resultHandler.handle(Future.failedFuture("用户名或密码不正确"));
    }
  }

  @Override
  public Future<User> authenticate(JsonObject credentials) {
    return AuthenticationProvider.super.authenticate(credentials);
  }

  @Override
  public void authenticate(Credentials credentials, Handler<AsyncResult<User>> resultHandler) {
    AuthenticationProvider.super.authenticate(credentials, resultHandler);
  }

  @Override
  public Future<User> authenticate(Credentials credentials) {
    return AuthenticationProvider.super.authenticate(credentials);
  }
}
