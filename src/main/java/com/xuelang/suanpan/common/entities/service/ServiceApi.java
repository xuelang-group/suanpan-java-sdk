package com.xuelang.suanpan.common.entities.service;

public class ServiceApi {
    private String funcName;
    private Class<?>[] parameters;

    public String getFuncName() {
        return funcName;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    public Class<?>[] getParameters() {
        return parameters;
    }

    public void setParameters(Class<?>[] parameters) {
        this.parameters = parameters;
    }
}
