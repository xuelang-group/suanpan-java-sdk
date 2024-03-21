package com.xuelang.suanpan.stream.message;

import com.xuelang.suanpan.entities.io.InPort;
import org.apache.commons.lang3.builder.ToStringBuilder;


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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("port", inPort.getUuid())
                .append("data", data)
                .toString();
    }
}