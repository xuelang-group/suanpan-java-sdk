package com.xuelang.suanpan.stream.message;


/**
 * 消息上下文
 */
public class Context {
    private String messageId;
    private Extra extra;

    public Context() {
        extra = new Extra();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Extra getExt() {
        return extra;
    }

    public void setExt(Extra extra) {
        this.extra = extra;
    }
}
