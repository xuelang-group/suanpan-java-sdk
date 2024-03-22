package com.xuelang.suanpan.common.entities.connection;

public class Connection {
    private String srcNodeId;
    private String srcOutPortUUID;
    private String tgtNodeId;
    private String tgtInPortUUID;
    private String tgtQueue;

    public String getSrcNodeId() {
        return srcNodeId;
    }

    public void setSrcNodeId(String srcNodeId) {
        this.srcNodeId = srcNodeId;
    }

    public String getSrcOutPortUUID() {
        return srcOutPortUUID;
    }

    public void setSrcOutPortUUID(String srcOutPortUUID) {
        this.srcOutPortUUID = srcOutPortUUID;
    }

    public String getTgtNodeId() {
        return tgtNodeId;
    }

    public void setTgtNodeId(String tgtNodeId) {
        this.tgtNodeId = tgtNodeId;
    }

    public String getTgtInPortUUID() {
        return tgtInPortUUID;
    }

    public void setTgtInPortUUID(String tgtInPortUUID) {
        this.tgtInPortUUID = tgtInPortUUID;
    }

    public String getTgtQueue() {
        return tgtQueue;
    }

    public void setTgtQueue(String tgtQueue) {
        this.tgtQueue = tgtQueue;
    }
}
