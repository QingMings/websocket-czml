package com.yhl.websocket_czml.handlers;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.yhl.websocket_czml.helper.Helper;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.nio.charset.Charset;

public class ToBusMessageHandler implements Handler<Message<Buffer>> {

  private final Vertx vertx;
  public static JsonArray orbits;
  private Long periodicId;
  private DateTime dateTime = new DateTime();

  public ToBusMessageHandler(Vertx vertx) {
    this.vertx = vertx;
    loadtleJson();
  }

  @Override
  public void handle(Message<Buffer> message) {
    System.out.println(this);
    System.out.println(message.body().copy());
    handleMessage(message.body().toJsonObject());
  }

  private void loadtleJson() {
    String jsonStr = FileUtil.readString(this.getClass().getResource("/testTle.json"), Charset.defaultCharset());
    orbits = new JsonArray(jsonStr);
    System.out.println("orbits size :" + orbits.size());
  }

  private void handleMessage(JsonObject body) {
    switch (body.getString("commandType")) {
      case "START_ORBIT_PREDICTION":
        startOrbitPrediction(body.getJsonObject("body"));
        break;
      case "STOP_ORBIT_PREDICTION":
        stopOrbitPrediction();
        break;
      case "PLUS_SPEED":
        plusSpeed(body);
        break;
      case "MINS_SPEED":
        minsSpeed(body);
        break;
    }
  }

  private void minsSpeed(JsonObject body) {
    startOrbitPrediction(body.getJsonObject("body"));
  }

  private void plusSpeed(JsonObject body) {
    startOrbitPrediction(body.getJsonObject("body"));
  }

  private void startOrbitPrediction(JsonObject body) {
    dateTime = DateTime.parse(body.getString("startTime")
      , DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    int maxOrbits= ObjectUtil.defaultIfNull(body.getInteger("maxOrbits"),100);
    if (maxOrbits>6000){
      maxOrbits=6000;
    }
    int finalMaxOrbits = maxOrbits;
    setPeriodic(body.getLong("dylay"), a -> {
      if (CollectionUtil.isNotEmpty(orbits)) {
        StopWatch stopWatch = new StopWatch("耗时");
        stopWatch.start("生成一帧数据");
        JsonArray results = new JsonArray();
        for (int i = 0; i < orbits.size(); i++) {
          if (i>(finalMaxOrbits -1)){
            break;
          }
          JsonObject orbit = orbits.getJsonObject(i);
          JsonObject result = Helper.getOrbitNextPosition(orbit, dateTime);
          if (ObjectUtil.isNotNull(result)) {
            results.add(result);
          }
        }

        stopWatch.stop();
        sendToStomp(results, dateTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS")),stopWatch.getTotalTimeMillis());
        dateTime = dateTime.plusSeconds(1);
      }
    });
  }

  private void sendToStomp(JsonArray jsonArray, String dateTimeStr, long ms) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("replyType", "Orbits_Position")
      .put("time", dateTimeStr)
      .put("body", jsonArray)
      .put("ms", ms);

    vertx.eventBus().publish("/toStomp", jsonObject.toBuffer());
  }

  private void stopOrbitPrediction() {
    if (ObjectUtil.isNotNull(periodicId))
      vertx.cancelTimer(periodicId);
  }

  private void setPeriodic(long delay, Handler<Void> handle) {
    if (ObjectUtil.isNotNull(periodicId)) {
      vertx.cancelTimer(periodicId);
    }
    periodicId = vertx.setPeriodic(ObjectUtil.defaultIfNull(delay, 1000L), l -> {
      handle.handle(null);
    });
  }
}
