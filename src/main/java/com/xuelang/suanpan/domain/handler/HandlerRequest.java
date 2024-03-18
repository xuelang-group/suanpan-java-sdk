package com.xuelang.suanpan.domain.handler;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class HandlerRequest {
    private List<InPortData> msg;

    public List<InPortData> getMsg() {
        return msg;
    }

    public void setMsg(List<InPortData> msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("msg", msg)
                .toString();
    }
}
