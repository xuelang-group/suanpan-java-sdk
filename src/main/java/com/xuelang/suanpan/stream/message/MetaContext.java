package com.xuelang.suanpan.stream.message;

import com.alibaba.fastjson2.JSONObject;

/**
 * 消息上下文
 */
public class MetaContext {
    private String messageId;
    private String requestId;
    private Extra extra;

    public MetaContext(){
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

    public Extra getExtra() {
        return extra;
    }

    public void setExtra(Extra extra) {
        this.extra = extra;
    }

    public void refreshExpire(Long validityMillis) {
        if (validityMillis != null && validityMillis > 0) {
            extra.setExpireTime(System.currentTimeMillis() + validityMillis);
        } else{
            extra.setExpireTime(Long.MAX_VALUE);
        }
    }

    public void refreshExt(JSONObject ext){
        extra.setGlobal(ext);
    }

    public void updateMsgOutTime() {
        extra.updateMsgNodeOutTime();
    }
}
