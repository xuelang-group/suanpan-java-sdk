package com.xuelang.suanpan.stream.handler;

import com.xuelang.suanpan.common.entities.io.OutPort;
import java.lang.reflect.Method;
import java.util.List;

public class HandlerMethodEntry<T> {
    private T instance;
    private Method method;
    private List<OutPort> specifiedDefaultOutPorts;

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

    public List<OutPort> getSpecifiedDefaultOutPorts() {
        return specifiedDefaultOutPorts;
    }

    public void setSpecifiedDefaultOutPorts(List<OutPort> specifiedDefaultOutPorts) {
        this.specifiedDefaultOutPorts = specifiedDefaultOutPorts;
    }
}
