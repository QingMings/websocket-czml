package com.yhl.websocket_czml;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.List;

public class Test {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle(),ar->{
      if (ar.succeeded()){
        System.out.println("启动成功");
      }else {
        System.out.println(ar.cause().getMessage());
      }
    });
  }

  @org.junit.jupiter.api.Test
  public  void convertToJson(){
    JsonArray jsonArray = new JsonArray();
    List<String> lines = FileUtil.readUtf8Lines("E:\\yhl_work\\websocket-czml\\src\\main\\resources\\3le-20220715.txt");
    for (int i = 0; i < lines.size(); i+=3) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.put("name", StrUtil.replace(lines.get(i),"0 ",""));
      jsonObject.put("line1",lines.get(i+1));
      jsonObject.put("line2",lines.get(i+2));
      jsonObject.put("id",lines.get(i+2).split("\\s+")[1]);
      jsonArray.add(jsonObject);
    }
  FileUtil.writeUtf8String(jsonArray.encode(),"testTleJson.json");
  }

  @org.junit.jupiter.api.Test
  public void testSplit(){
    String s = "2    50  47.2313 291.3661 0113156  62.3727 298.8498 12.20121086763129";
    String[] split = s.split("\\s+");
    Arrays.stream(split).forEach(t ->{
      System.out.println(":"+t+":");
    });
  }
}
