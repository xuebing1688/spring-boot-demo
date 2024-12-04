package com.example.ice.test;

import org.apache.commons.lang.time.DateUtils;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @ClassName: $
 * @Description:
 * @Author: summer
 * @Date: 2024-10-09 11:31
 * @Version: 1.0
 **/
public class TestTime {


  @Test
  public void test01()
  {
    // 使用 java.time 包进行时区转换

    Instant instant = Instant.ofEpochMilli(1728444985295L);
    ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
    Timestamp timestamp = Timestamp.valueOf(zonedDateTime.toLocalDateTime());
    System.out.println(timestamp);
  }



  @Test
  public  void    determineAggregationUnit() {
    //把String类型转换为LocalDateTime类型
    long startTime = 1728531143000L;
    long endTime = System.currentTimeMillis();
    Duration duration = Duration.ofMillis(endTime-startTime);
    long durationHours = duration.toHours();
    long day = duration.toDays();
    System.out.println(durationHours);
    System.out.println(day);

  }

  @Test
  public void test02()
  {
    // 传递一个Date类型时间 往前推8小时
    Date date = new Date();
    Date date1 = DateUtils.addHours(date, -8);
    System.out.println(date1);

    System.out.println(date1.getTime());
    System.out.println(date1.getTime()/1000);
    System.out.println(date1.getTime()/1000/60/60);



  }

  @Test
  public void test03()
  {
    // 传递一个Date类型时间 往前推8小时
    Date date = new Date();
    Date date1 = DateUtils.addHours(date, -8);
    System.out.println(date1);

    System.out.println(date1.getTime());
    System.out.println(date1.getTime()/1000);
    System.out.println(date1.getTime()/1000/60/60);

    int i = 1000;
    int j = 1000;

    int add = add(i, j);
    System.out.println(add);

  }

  public int add(int i,int j){
    return i+j;
  }


}
