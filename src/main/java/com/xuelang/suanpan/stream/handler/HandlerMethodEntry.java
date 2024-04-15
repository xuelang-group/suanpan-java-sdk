package com.xuelang.suanpan.stream.handler;

import com.xuelang.suanpan.common.entities.io.Outport;
import com.xuelang.suanpan.configuration.ConstantConfiguration;

import java.lang.reflect.Method;

public class HandlerMethodEntry<T> {
    private T instance;
    private Method method;
    private final Outport defaultOutport = ConstantConfiguration.getByOutportIndex(1);

    public T getInstance() {
        return instance;
    }

    public void setInstance(T instance) {
        this.instance = instance;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Outport getDefaultOutPort() {
        return defaultOutport;
    }
}
