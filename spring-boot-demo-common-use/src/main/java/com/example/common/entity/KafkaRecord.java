package com.example.common.entity;

/**
 * @ClassName: $
 * @Description:
 * @Author: summer
 * @Date: 2025-01-03 16:35
 * @Version: 1.0
 **/


import java.util.Map;
import java.util.HashMap;

public class KafkaRecord {
  private long netlinkTime;
  private int netlinkId;
  private Map<String, Object> fields;

  public KafkaRecord() {
    this.fields = new HashMap<>();
  }

  public void setNetlinkTime(long netlinkTime) {
    this.netlinkTime = netlinkTime;
  }

  public void setNetlinkId(int netlinkId) {
    this.netlinkId = netlinkId;
  }

  public void addField(String name, Object value) {
    fields.put(name, value);
  }

  public long getNetlinkTime() {
    return netlinkTime;
  }

  public int getNetlinkId() {
    return netlinkId;
  }

  public Map<String, Object> getFields() {
    return fields;
  }
}
