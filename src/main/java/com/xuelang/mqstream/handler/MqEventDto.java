package com.xuelang.mqstream.handler;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class MqEventDto {

    public static final String EMPTY_EVENT = "empty_event";

    private String input;

    private String event = EMPTY_EVENT;

    private JSONObject data;

    private String extra;

    private String requestId;
}
