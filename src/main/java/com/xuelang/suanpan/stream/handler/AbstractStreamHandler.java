package com.xuelang.suanpan.stream.handler;

import com.xuelang.suanpan.stream.message.InflowMessage;
import com.xuelang.suanpan.stream.message.OutflowMessage;

public interface AbstractStreamHandler {
    OutflowMessage handle(InflowMessage inflowMessage);
}
