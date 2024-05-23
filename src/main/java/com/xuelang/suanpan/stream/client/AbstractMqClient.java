package com.xuelang.suanpan.stream.client;

import com.xuelang.suanpan.common.exception.StreamGlobalException;
import com.xuelang.suanpan.stream.message.InflowMessage;
import com.xuelang.suanpan.stream.message.MetaOutflowMessage;

import java.util.List;

public abstract class AbstractMqClient{
    public abstract List<InflowMessage> polling(int count, long timeoutMillis) throws StreamGlobalException;
    public abstract void subscribe();
    public abstract String publish(MetaOutflowMessage metaOutflowMessage);
    public abstract void destroy();
}
