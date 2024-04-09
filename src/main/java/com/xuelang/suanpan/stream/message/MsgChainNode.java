package com.xuelang.suanpan.stream.message;

import java.util.Date;

public class MsgChainNode {
    private String nodeId;
    private Date inTime;
    private Date outTime;

    public MsgChainNode(String nodeId, Date inTime) {
        this.nodeId = nodeId;
        this.inTime = inTime;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Date getInTime() {
        return inTime;
    }

    public void setInTime(Date inTime) {
        this.inTime = inTime;
    }

    public Date getOutTime() {
        return outTime;
    }

    public void setOutTime(Date outTime) {
        this.outTime = outTime;
    }
}
