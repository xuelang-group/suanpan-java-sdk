package com.xuelang.mqstream.options;

import com.xuelang.mqstream.MqClient;
import com.xuelang.mqstream.RedisStreamMqClient;
import lombok.Data;

import java.util.Map;

@Data
public class StreamOption {

    private String redisHost;

    private int redisPort;

    private String nodeGroup;

    private String nodeId;

    private int recvQueueDelay;

    private String recvQueue;

    private String sendQueue;

    public  void buildByOptions(Map<String, String> options) {
        this.redisHost = options.get("mq-redis-host") == null ? "localhost" : options.get("mq-redis-host");
        this.redisPort = options.get("mq-redis-port") == null ? 6379 : Integer.parseInt(options.get("mq-redis-port"));
        this.nodeGroup = options.get("stream-node-group") == null ? "default" : options.get("stream-node-group");
        this.nodeId = options.get("stream-node-id");
        this.recvQueueDelay = options.get("stream-recv-queue-delay") == null ? 1000 : Integer.parseInt(options.get("stream-recv-queue-delay"));
        this.recvQueue = options.get("stream-recv-queue");
        this.sendQueue = options.get("stream-send-queue");
    }

    public MqClient buildRedisClient() {
        return new RedisStreamMqClient(this.redisHost, this.redisPort);
    }

}
