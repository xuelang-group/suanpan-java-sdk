package com.xuelang.suanpan.common;

import java.util.Objects;

public class ProxrConnectionParam {

    /**
     * 算盘平台地址, host:port
     * example: 10.99.22.10:30080
     */
    private String spAddress;

    /**
     * 算盘的用户Id, 在算盘后面板右上角可以查看到
     */
    private String spUserId;

    /**
     * 要调试组件所在的项目Id
     */
    private String spProjId;

    /**
     * 要调试的组件Id
     */
    private String spNodeId;

    /**
     * 调试端口：默认值30080
     */
    private Integer debugPort = 30080;

    public ProxrConnectionParam(String spAddress, String spUserId, String spProjId, String spNodeId, Integer debugPort) {
        this.spAddress = Objects.requireNonNull(spAddress, "spAddress must not be null");
        this.spUserId = Objects.requireNonNull(spUserId, "spUserId must not be null");
        this.spProjId = Objects.requireNonNull(spProjId, "spProjId must not be null");
        this.spNodeId = Objects.requireNonNull(spNodeId, "spNodeId must not be null");
        this.debugPort = debugPort;
    }

    public String getSpAddress() {
        return spAddress;
    }

    public String getSpUserId() {
        return spUserId;
    }

    public String getSpProjId() {
        return spProjId;
    }

    public String getSpNodeId() {
        return spNodeId;
    }

    public Integer getDebugPort() {
        return debugPort;
    }

    public String getProxrConnectionUrl() {
        return spAddress + "/proxr/" + spUserId + "/" + spProjId + "/" + spNodeId + "/" + debugPort;
    }

    public String getGatewayConnectionUrl() {
        return spAddress + "/gateway/proxy/node/" + spProjId + "/" + spUserId + "/" + spNodeId + "/" + debugPort;
    }
}
