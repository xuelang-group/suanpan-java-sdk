package com.xuelang.suanpan.client;

public class SpConnectionParam {

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

    public String getSpAddress() {
        return spAddress;
    }

    public void setSpAddress(String spAddress) {
        this.spAddress = spAddress;
    }

    public String getSpUserId() {
        return spUserId;
    }

    public void setSpUserId(String spUserId) {
        this.spUserId = spUserId;
    }

    public String getSpProjId() {
        return spProjId;
    }

    public void setSpProjId(String spProjId) {
        this.spProjId = spProjId;
    }

    public String getSpNodeId() {
        return spNodeId;
    }

    public void setSpNodeId(String spNodeId) {
        this.spNodeId = spNodeId;
    }

    public Integer getDebugPort() {
        return debugPort;
    }

    public void setDebugPort(Integer debugPort) {
        this.debugPort = debugPort;
    }
}
