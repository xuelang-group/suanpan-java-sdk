package com.xuelang.suanpan.stream.handler;

import com.xuelang.suanpan.stream.message.Extra;
import com.xuelang.suanpan.stream.message.Context;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class HandlerRequest extends Context {
    private List<InPortData> msg;

    public List<InPortData> getMsg() {
        return msg;
    }

    public void setMsg(List<InPortData> msg) {
        this.msg = msg;
    }

    public Extra getExtra() {
        return extra;
    }

    public void setExtra(Extra extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("msg", msg)
                .append("extra", extra)
                .toString();
    }
}
