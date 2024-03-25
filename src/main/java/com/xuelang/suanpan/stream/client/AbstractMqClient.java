package com.xuelang.suanpan.stream.client;

import com.xuelang.suanpan.stream.handler.HandlerProxy;
import com.xuelang.suanpan.stream.handler.response.PollingResponse;
import com.xuelang.suanpan.stream.message.OutBoundMessage;

import java.util.concurrent.TimeUnit;

public abstract class AbstractMqClient {
    protected HandlerProxy proxy;

    public abstract PollingResponse polling(long timeout, TimeUnit unit);
    public abstract void infiniteConsume();
    public abstract String publish(OutBoundMessage outBoundMessage);
    public abstract void destroy();
}
