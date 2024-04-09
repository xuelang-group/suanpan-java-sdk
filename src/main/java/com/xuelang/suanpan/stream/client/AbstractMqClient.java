package com.xuelang.suanpan.stream.client;

import com.xuelang.suanpan.common.observable.Observer;
import com.xuelang.suanpan.stream.handler.HandlerProxy;
import com.xuelang.suanpan.stream.message.InflowMessage;
import com.xuelang.suanpan.stream.message.MetaOutflowMessage;

import java.util.concurrent.TimeUnit;

public abstract class AbstractMqClient implements Observer {
    protected HandlerProxy proxy;
    public abstract InflowMessage polling(long timeout, TimeUnit unit);
    public abstract void infiniteConsume();
    public abstract String publish(MetaOutflowMessage metaOutflowMessage);
    public abstract void destroy();
}
