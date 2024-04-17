package com.xuelang.suanpan.stream.handler;

import java.lang.reflect.Method;

public class HandlerMethodEntry<T> {
    private T instance;
    private Method method;

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
}
