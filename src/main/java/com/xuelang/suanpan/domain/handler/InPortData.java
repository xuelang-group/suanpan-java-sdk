package com.xuelang.suanpan.domain.handler;

import com.xuelang.suanpan.domain.io.InPort;


public class InPortData {
    private InPort inPort;
    private Object data;

    public InPort getInPort() {
        return inPort;
    }

    public void setInPort(InPort inPort) {
        this.inPort = inPort;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
