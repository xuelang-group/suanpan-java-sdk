package com.xuelang.mqstream.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GraphConnection {
    private JSONObject tgt;
    private JSONObject metadata;
    private JSONObject src;
}
