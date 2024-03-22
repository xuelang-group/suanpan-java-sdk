package com.xuelang.suanpan.stream.message;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Extra {
    //消息经过的节点链
    private List<MsgChainNode> msgChain = new ArrayList<>();

    // 默认是长期有效
    private Long expireTime = Long.MAX_VALUE;

    //兼容老版本的已有的配置
    private JSONObject global;

    public List<MsgChainNode> getMsgChain() {
        return msgChain;
    }

    public void append(String nodeId) {
        MsgChainNode node = new MsgChainNode(nodeId, new Date());
        this.msgChain.add(node);
    }

    public void updateMsgNodeOutTime() {
        if (!msgChain.isEmpty()) {
            msgChain.get(msgChain.size() - 1).setOutTime(new Date());
        }
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public JSONObject getGlobal() {
        return global;
    }

    public void setGlobal(JSONObject global) {
        this.global = global;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("global", global)
                .toString();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expireTime;
    }


    class MsgChainNode {
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

}
