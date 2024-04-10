package com.xuelang.suanpan.stream.message;


/**
 * 消息上下文
 */
public class Context {
    private String messageId;
    private Extra extra;

    private Context() {
        extra = new Extra();
    }

    public static Builder builder() {
        return new Builder();
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

    public static class Builder {
        private Context context = new Context();

        public Builder setExt(Extra extra) {
            context.extra = extra;
            return this;
        }

        public Builder appendExt(String key, Object value) {
            context.extra.appendExtra(key, value);
            return this;
        }

        public Builder setMessageId(String messageId) {
            context.messageId = messageId;
            return this;
        }

        public Builder refreshExpire(Long validitySeconds) {
            if (validitySeconds != null && validitySeconds > 0)
                context.extra.setExpireTime(validitySeconds * 1000);
            return this;
        }

        public Context build() {
            return this.context;
        }
    }
}
