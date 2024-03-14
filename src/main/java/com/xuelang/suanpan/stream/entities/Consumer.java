package com.xuelang.suanpan.stream.entities;

public class Consumer {
    /**
     * 消费者群组
     */
    private String group = "default";

    /**
     * 消费者名称
     */
    private String name = "unknown";

    /**
     * 单次消费条数，默认消费一条
     */
    private long count = 1L;

    /**
     * 是否无ACK，默认false
     */
    private boolean noAck;

    /**
     * 消费者要消费的队列
     */
    private String queue;

    /**
     * 上次消费到的消息id，如果使用">"则代表要消费最新的消息
     */
    private String consumedMessageId = ">";

    /**
     * 阻塞消费失败后，重新消费延时时间，单位毫秒
     */
    private long restartDelay = 1000L;


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public boolean isNoAck() {
        return noAck;
    }

    public void setNoAck(boolean noAck) {
        this.noAck = noAck;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getConsumedMessageId() {
        return consumedMessageId;
    }

    public void setConsumedMessageId(String consumedMessageId) {
        this.consumedMessageId = consumedMessageId;
    }

    public long getRestartDelay() {
        return restartDelay;
    }

    public void setRestartDelay(long restartDelay) {
        this.restartDelay = restartDelay;
    }
}
