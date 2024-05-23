package com.xuelang.suanpan.common.entities.service;

import com.xuelang.suanpan.common.entities.enums.ProtocolType;

import java.util.List;

public class ServiceInfo {
    private String nodeId;
    private ProtocolType protocolType;
    private Class<?> clazz;
    private List<ServiceApi> apiList;

    public String getNodeId() {
        return nodeId;
    }

    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public List<ServiceApi> getApiList() {
        return apiList;
    }

    public void setApiList(List<ServiceApi> apiList) {
        this.apiList = apiList;
    }
}
