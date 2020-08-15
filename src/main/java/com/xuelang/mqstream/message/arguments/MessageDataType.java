package com.xuelang.mqstream.message.arguments;

/**
 * @author ellison
 * @Date: 2019/11/7 09:24
 * @Description:
 */
public enum MessageDataType {
  /**
   *
   */
  COMMON(CommonType.class),
  /**
   *
   */
  EVENT(EventType.class);

  private Class cls;

  MessageDataType(Class cls) {
      this.cls = cls;
  }

  public Class getCls() {
      return cls;
  }
}
