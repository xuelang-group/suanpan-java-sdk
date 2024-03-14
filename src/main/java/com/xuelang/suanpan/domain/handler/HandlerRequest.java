package com.xuelang.suanpan.domain.handler;

import java.util.List;

public class HandlerRequest {
    private List<InPortData> msg;

    public List<InPortData> getMsg() {
        return msg;
    }

    public void setMsg(List<InPortData> msg) {
        this.msg = msg;
    }
}
