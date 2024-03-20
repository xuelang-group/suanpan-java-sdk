package com.xuelang.suanpan.stream.message;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Extra {
    private JSONObject global;

    public JSONObject getGlobal() {
        return global;
    }

    public void setGlobal(JSONObject global) {
        this.global = global;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("global", global)
                .toString();
    }
}
