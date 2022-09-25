package com.yhl.websocket_czml.helper;

import cn.hutool.core.date.StopWatch;
import io.vertx.core.json.JsonObject;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.libtleprop.DELE_TLE;
import org.bytedeco.libtleprop.DTIME_ID;
import org.bytedeco.libtleprop.elsetrec;
import org.bytedeco.libtleprop.global.libtleprop;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Map;

public class Helper {
  public static final double TO_JUTC1 = 2400000.5d;

  public static void printStompHeader(Map<String, String> headers, String... headerKeys) {
    for (String headerKey : headerKeys) {
      System.out.println(headers.get(headerKey));
    }
  }

  public static double date2mjd(org.joda.time.DateTime date) {
    int yr = date.getYear();
    int mo = date.getMonthOfYear() + 1;
    int dy = date.getDayOfMonth();
    int hr = date.getHourOfDay();
    int mi = date.getMinuteOfHour();
    int sec = date.getSecondOfMinute();
    int millise = date.getMillisOfSecond();
    double se = sec + millise / 100f;

    return libtleprop.YMDHMS2MJD(yr, mo, dy, hr, mi, se);
  }

  public static JsonObject getOrbitNextPosition(JsonObject orbit, DateTime dateTime) {
    double mjd = date2mjd(dateTime);
    String line1 = orbit.getString("line1");
    String line2 = orbit.getString("line2");


    BytePointer line1P = new BytePointer(line1.getBytes().length);
    BytePointer line2P = new BytePointer(line2.getBytes().length);
    DELE_TLE tle_rec = new DELE_TLE();
    IntPointer yr = new IntPointer(4);
    IntPointer mo = new IntPointer(2);
    IntPointer dy = new IntPointer(2);
    IntPointer hr = new IntPointer(2);
    IntPointer mi = new IntPointer(2);
    DoublePointer se = new DoublePointer(8);
    elsetrec sat_rec = new elsetrec();


    line1P.put(line1.getBytes());
    line2P.put(line2.getBytes());
    //双行根数初始化
    libtleprop.InitializeTwoLineElement(line1P, line2P, tle_rec);

    tle_rec.satname().put(orbit.getString("name").getBytes());
    libtleprop.MJD2YMDHMS(tle_rec.mjd(), yr, mo, dy, hr, mi, se);

    libtleprop.InitializeTleSetRec(tle_rec, sat_rec);

    double mjdStart = mjd;
    double mjdStop = mjd + 1.0;
    double mjdCur = mjdStart;
    float stepEph = 1.0f / 30.0f;
    float stepRef = 60.0f;

    //初始化参考点1
    double jutc1 = mjd2JUTC1(mjdStart);
    DTIME_ID TT1 = libtleprop.GetTIMEValue_JUTC(jutc1);
    DoublePointer sateph_array_t1 = new DoublePointer(54);
    int flag = libtleprop.GetSatEph_Array_SGP4_TT_2(tle_rec.jtt(), sat_rec, TT1, sateph_array_t1);

    //初始化参考点2
    double jutc2 = jutc1 + stepRef / 8640.0f;
    DTIME_ID TT2 = libtleprop.GetTIMEValue_JUTC(jutc2);
    DoublePointer sateph_array_t2 = new DoublePointer(54);
    flag = libtleprop.GetSatEph_Array_SGP4_TT_2(tle_rec.jtt(), sat_rec, TT2, sateph_array_t2);

    int nStep = 0;
    DoublePointer jtt_t = new DoublePointer(8);
    DoublePointer jut1_t = new DoublePointer(8);
    double jutc_t = mjd2JUTC1(mjdCur);
    libtleprop.GetTT_UT1(jutc_t, jtt_t, jut1_t);
    DoublePointer sateph_array_t = new DoublePointer(54);

    flag = libtleprop.GetSatEph_Array_SGP4_J2_2(tle_rec.jtt(), sat_rec,
      stepRef, sateph_array_t1, sateph_array_t2, jutc_t, jtt_t.get(), jut1_t.get(), sateph_array_t);
    if (flag < 0) {
      return null;
    }
    //J2000
    double y = sateph_array_t.get(22);
    double z = sateph_array_t.get(23);
    double x = sateph_array_t.get(21);

    //地固系
    double yy = sateph_array_t.get(28); // -y
    double zz = sateph_array_t.get(29); // z
    double xx = sateph_array_t.get(27) ; // x

    double L = sateph_array_t.get(51);// lon
    double B = sateph_array_t.get(52); // lat
    double H = sateph_array_t.get(53);// alt

    return new JsonObject()
      .put("id",orbit.getString("id"))
      .put("name",orbit.getString("name"))
      .put("eci", new double[]{x, y, z})
      .put("ecf", new double[]{xx, yy, zz})
      .put("lla", new double[]{L, B, H});
  }

  private static double mjd2JUTC1(double mjd) {
    return mjd + TO_JUTC1;
  }
}
