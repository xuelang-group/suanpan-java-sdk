package com.xuelang.suanpan.stream.message;

import com.xuelang.suanpan.common.entities.enums.NodeReceiveMsgType;

/**
 * 消息上下文
 */
public class Header {
    private String messageId;
    private String requestId;
    private Boolean success = true;
    private Extra extra;
    private NodeReceiveMsgType receiveMsgType;

    public Header(){
        extra = new Extra();
    }

    public boolean isExpired(){
        if (extra == null){
            return false;
        }
        return extra.isExpired();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Extra getExtra() {
        return extra;
    }

    public void setExtra(Extra extra) {
        this.extra = extra;
    }

    public NodeReceiveMsgType getReceiveMsgType() {
        return receiveMsgType;
    }

    public void setReceiveMsgType(NodeReceiveMsgType receiveMsgType) {
        this.receiveMsgType = receiveMsgType;
    }

    public void refreshExpire(long validityMillis) {
        extra.setExpireTime(System.currentTimeMillis() + validityMillis);
    }

    public void updateMsgOutTime() {
        extra.updateMsgNodeOutTime();
    }
}
