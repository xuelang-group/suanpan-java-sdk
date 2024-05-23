package com.xuelang.suanpan.common.entities.connection;

import com.xuelang.suanpan.common.utils.ParameterUtil;

public class Connection {
    private Integer id;
    private String srcNodeId;
    private String tgtNodeId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSrcNodeId() {
        return srcNodeId;
    }

    public void setSrcNodeId(String srcNodeId) {
        this.srcNodeId = srcNodeId;
    }

    public String getTgtNodeId() {
        return tgtNodeId;
    }

    public void setTgtNodeId(String tgtNodeId) {
        this.tgtNodeId = tgtNodeId;
    }

    public String getTgtSvc() {
        return ParameterUtil.getSpSvc() + ":" + ParameterUtil.getSpPort() + "/app-" + ParameterUtil.getAppId() + "-" + this.tgtNodeId;
    }


    //gateway: /gateway/proxy/node/${appId}/user-${userId}/${nodeId}/proxr/${port}
    public String getTgtGatewayHost(Integer tgtServicePort) {
        return ParameterUtil.getSpSvc() + ":" + ParameterUtil.getSpPort() + "/gateway/proxy/node/" + ParameterUtil.getAppId()
                + "/user-" + ParameterUtil.getUserId() + "/" + tgtNodeId + "/proxr/" + tgtServicePort;
    }

    //proxr: /proxr/${userId}/${appId}/${nodeId}/${port}
    public String getTgtProxrHost(Integer tgtServicePort) {
        return ParameterUtil.getSpSvc() + ":" + ParameterUtil.getSpPort() + "/proxr/" + ParameterUtil.getUserId()
                + "/" + ParameterUtil.getAppId() + "/" + tgtNodeId + "/" + tgtServicePort;
    }

}
