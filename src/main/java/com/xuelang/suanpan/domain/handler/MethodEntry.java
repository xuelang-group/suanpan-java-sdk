package com.xuelang.suanpan.domain.handler;

import com.xuelang.suanpan.domain.io.OutPort;
import java.lang.reflect.Method;
import java.util.List;

public class MethodEntry<T> {
    private T instance;
    private Method method;
    private boolean sync;
    private List<OutPort> outPorts;

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

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public List<OutPort> getOutPorts() {
        return outPorts;
    }

    public void setOutPorts(List<OutPort> outPorts) {
        this.outPorts = outPorts;
    }
}
