package com.xuelang.suanpan.stream.client;

import com.xuelang.suanpan.stream.handler.HandlerProxy;
import com.xuelang.suanpan.stream.handler.HandlerRequest;
import com.xuelang.suanpan.stream.message.StreamContext;

import java.util.concurrent.TimeUnit;

public abstract class AbstractMqClient {
    protected HandlerProxy proxy;

    public abstract HandlerRequest polling(long timeout, TimeUnit unit);
    public abstract void infiniteConsume();
    public abstract String publish(StreamContext streamContext);
    public abstract void destroy();
}
