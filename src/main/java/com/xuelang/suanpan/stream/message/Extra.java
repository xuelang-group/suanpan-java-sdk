package com.xuelang.suanpan.stream.message;

import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 消息扩展信息
 */
public class Extra {
    // 消息经过的节点链
    private List<MsgChainNode> msgChain = new ArrayList<>();

    // 默认是长期有效
    private Long expireTime = Long.MAX_VALUE;

    // 兼容老版本的已有的配置
    private JSONObject global;

    public Extra() {
        global = new JSONObject();
    }

    public List<MsgChainNode> getMsgChain() {
        return msgChain;
    }

    public void setMsgChain(List<MsgChainNode> msgChain) {
        this.msgChain = msgChain;
    }

    public void append(String nodeId) {
        MsgChainNode node = new MsgChainNode(nodeId, new Date());
        if (!this.msgChain.contains(node)) {
            this.msgChain.add(node);
        }
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

    public boolean isExpired() {
        return System.currentTimeMillis() > expireTime;
    }

    public void appendExtra(String key, Object value) {
        Objects.requireNonNull(key, "append extra info key cannot be null");
        Objects.requireNonNull(value, "append extra info value cannot be null");
        global.put(key, value);
    }
}
