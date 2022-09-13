package com.yhl.websocket_czml.helper;

import java.util.Map;

public class Helper {

  public static void printStompHeader(Map<String,String> headers,String ... headerKeys){
    for (String headerKey : headerKeys) {
      System.out.println(headers.get(headerKey));
    }
  }
}
