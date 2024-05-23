package com.xuelang.suanpan.parameter;

import com.xuelang.suanpan.common.entities.BaseSpDomainEntity;
import com.xuelang.suanpan.common.entities.ProxrConnectionParam;
import com.xuelang.suanpan.common.entities.connection.Connection;
import com.xuelang.suanpan.common.entities.enums.NodeReceiveMsgType;
import com.xuelang.suanpan.common.entities.io.Inport;
import com.xuelang.suanpan.common.entities.io.Line;
import com.xuelang.suanpan.common.entities.io.Outport;
import com.xuelang.suanpan.common.utils.ParameterUtil;

import javax.annotation.Nullable;
import java.util.List;

public class Parameter extends BaseSpDomainEntity {

    private Parameter() {

    }

    private Parameter(ProxrConnectionParam proxrConnectionParam) {
        super(proxrConnectionParam);
    }

    public Connection getInvokeConnection(Integer connId) {
        return ParameterUtil.getInvokeConnection(connId);
    }

    public List<Connection> getCurrentNodeConnections() {
        return ParameterUtil.getCurrentNodeConnections();
    }

    public List<Line> getIoLines(Outport outPort) {
        return ParameterUtil.getIoLines(outPort);
    }

    public NodeReceiveMsgType getReceiveMsgType() {
        return ParameterUtil.getReceiveMsgType();
    }

    public String getSpSvc() {
        return ParameterUtil.getSpSvc();
    }

    public Integer getSpPort() {
        return ParameterUtil.getSpPort();
    }

    public String getSpProtocol() {
        return ParameterUtil.getSpProtocol();
    }

    public String getAppId() {
        return ParameterUtil.getAppId();
    }

    public String getSecret() {
        return ParameterUtil.getSecret();
    }

    public String getUserId() {
        return ParameterUtil.getUserId();
    }

    public Outport getByOutportIndex(Integer outportIndex) {
        return ParameterUtil.getByOutportIndex(outportIndex);
    }

    public Outport getByOutPortUUID(String outPortUUID) {
        return ParameterUtil.getByOutPortUUID(outPortUUID);
    }

    public Inport getByInPortUuid(String key) {
        return ParameterUtil.getByInPortUuid(key);
    }

    public Inport getByInportIndex(Integer inportIndex) {
        return ParameterUtil.getByInportIndex(inportIndex);
    }

    public List<Outport> getOutPorts() {
        return ParameterUtil.getOutPorts();
    }

    public Long getQueueMaxSendLen() {
        return ParameterUtil.getQueueMaxSendLen();
    }

    public boolean getQueueSendTrim() {
        return ParameterUtil.getQueueSendTrim();
    }

    public String getCurrentNodeId() {
        return ParameterUtil.getCurrentNodeId();
    }

    public String getSendMasterQueue() {
        return ParameterUtil.getSendMasterQueue();
    }

    public String getReceiveQueue() {
        return ParameterUtil.getReceiveQueue();
    }

    public boolean getEnableP2pSend() {
        return ParameterUtil.getEnableP2pSend();
    }

    public String getStreamHost() {
        return ParameterUtil.getStreamHost();
    }

    public Integer getStreamPort() {
        return ParameterUtil.getStreamPort();
    }

    public String getMqType() {
        return ParameterUtil.getMqType();
    }

    public Object get(String key, @Nullable Object defaultValue) {
        return ParameterUtil.get(key, defaultValue);
    }

    public Integer getInportMaxIndex() {
        return ParameterUtil.getInportMaxIndex();
    }
}
