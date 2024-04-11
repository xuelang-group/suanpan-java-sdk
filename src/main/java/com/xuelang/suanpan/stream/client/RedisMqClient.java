package com.xuelang.suanpan.stream.client;

import com.alibaba.fastjson2.JSON;
import com.xuelang.suanpan.common.exception.StreamGlobalException;
import com.xuelang.suanpan.common.pool.ThreadPool;
import com.xuelang.suanpan.common.utils.SerializeUtil;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.stream.handler.HandlerProxy;
import com.xuelang.suanpan.stream.message.InflowMessage;
import com.xuelang.suanpan.stream.message.MetaInflowMessage;
import com.xuelang.suanpan.stream.message.MetaOutflowMessage;
import com.xuelang.suanpan.stream.message.MqMessage;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.NestedMultiOutput;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandKeyword;
import io.lettuce.core.protocol.CommandType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class RedisMqClient extends AbstractMqClient {
    private final String SEARCH_MSG = "BUSYGROUP Consumer Group name already exists";
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
    private volatile boolean isPolling;
    private volatile InflowMessage inflowMessage;
    private final RedisClient client;
    private final StatefulRedisConnection<String, String> consumeConnection;
    private final StatefulRedisConnection<String, String> sendConnection;
    private static final Lock lock = new ReentrantLock();
    private static final Condition pollingAvailableCondition = lock.newCondition();
    private static final Lock consumeLock = new ReentrantLock();
    private static final Condition readyConsumeCondition = consumeLock.newCondition();


    public void setGroup(String group) {
        this.group = group;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void setNoAck(boolean noAck) {
        this.noAck = noAck;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public void setConsumedMessageId(String consumedMessageId) {
        this.consumedMessageId = consumedMessageId;
    }

    public void setRestartDelay(long restartDelay) {
        this.restartDelay = restartDelay;
    }

    public RedisMqClient(HandlerProxy proxy) {
        RedisURI uri = RedisURI.Builder.redis(ConstantConfiguration.getStreamHost(), ConstantConfiguration.getStreamPort()).build();
        this.client = RedisClient.create(uri);
        this.client.setOptions(ClientOptions.builder().autoReconnect(true).pingBeforeActivateConnection(true).build());
        this.consumeConnection = this.client.connect();
        this.sendConnection = this.client.connect();
        this.proxy = proxy;
    }

    public void initConsumerGroup() {
        CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
                .add(CommandKeyword.CREATE)
                .add(queue)
                .add(group)
                .add("0")
                .add("MKSTREAM");
        RedisAsyncCommands<String, String> commands = this.consumeConnection.async();
        RedisFuture<String> future = commands.dispatch(CommandType.XGROUP, new StatusOutput<>(StringCodec.UTF8), args);
        try {
            future.get();
        } catch (ExecutionException e) {
            if (StringUtils.containsIgnoreCase(e.getMessage(), SEARCH_MSG)) {
                return;
            }

            log.error("block get mq message error", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            log.error("block get mq message error", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        if (this.consumeConnection != null) {
            this.consumeConnection.close();
        }

        if (this.sendConnection != null) {
            this.sendConnection.close();
        }
    }

    @Override
    public InflowMessage polling(long timeoutMillis) {
        lock.lock();
        inflowMessage = null;
        isPolling = true;
        try {
            pollingAvailableCondition.await(timeoutMillis, TimeUnit.MILLISECONDS);
            isPolling = false;
            log.info("polled message:{}", inflowMessage.toString());
        } catch (InterruptedException e) {
            log.error("polling mq message failed!");
        } finally {
            lock.unlock();
            return inflowMessage;
        }
    }

    @Override
    public void infiniteConsume() {
        CommandArgs<String, String> consumeArgs = new CommandArgs<>(StringCodec.UTF8)
                .add(CommandKeyword.GROUP).add(this.group)
                .add(this.name)
                //block时间设置为0，则一直保持阻塞
                .add(CommandKeyword.BLOCK).add(0)
                .add(CommandKeyword.COUNT).add(this.count)
                .add("STREAMS").add(this.queue)
                .add(this.consumedMessageId);
        if (this.noAck) {
            consumeArgs.add(CommandKeyword.NOACK);
        }

        Thread consumerThread = new Thread(new InfiniteConsumeTask(consumeArgs, proxy));
        consumerThread.setName("consumer_thread");
        consumerThread.start();
    }


    @Override
    public String publish(MetaOutflowMessage metaOutflowMessage) {
        metaOutflowMessage.updateMsgNodeOutTime();
        XAddArgs addArgs = XAddArgs.Builder.maxlen(metaOutflowMessage.getMaxLength())
                .approximateTrimming(metaOutflowMessage.isApproximateTrimming());
        if (metaOutflowMessage.isP2p()) {
            Map<String, Object[]> p2pDataMap = metaOutflowMessage.toP2PQueueData();
            if (p2pDataMap == null || p2pDataMap.isEmpty()) {
                return null;
            }

            List<String> publishResults = new ArrayList<>();
            p2pDataMap.entrySet().parallelStream().forEach(entry -> {
                RedisFuture<String> future = this.sendConnection.async().xadd(entry.getKey(), addArgs, entry.getValue());
                try {
                    publishResults.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    log.error("publish message to MQ error", e);
                } finally {
                    log.info("publish to queue={}, data={}", entry.getKey(), SerializeUtil.serialize(entry.getValue()));
                }
            });

            return publishResults.toString();
        } else {
            Object[] outData = metaOutflowMessage.toMasterQueueData();
            RedisFuture<String> future = this.sendConnection.async().xadd(metaOutflowMessage.getSendMasterQueue(), addArgs, outData);
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("publish message to MQ error", e);
                return null;
            } finally {
                log.info("publish to queue={}, data={}", metaOutflowMessage.getSendMasterQueue(), SerializeUtil.serialize(outData));
            }
        }
    }

    private void ack(String queue, String group, String messageId) {
        RedisFuture<Long> xack = this.sendConnection.async().xack(queue, group, messageId);
        try {
            xack.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("xack to MQ error", e);
        }
    }

    @Override
    public void readyConsume() {
        consumeLock.lock();
        try {
            readyConsumeCondition.signal();
        } finally {
            consumeLock.unlock();
        }
    }


    private class InfiniteConsumeTask implements Runnable {
        final HandlerProxy proxy;
        final CommandArgs<String, String> args;

        public InfiniteConsumeTask(CommandArgs<String, String> args, HandlerProxy proxy) {
            this.proxy = proxy;
            this.args = args;
        }

        @Override
        public void run() throws RuntimeException {
            consumeLock.lock();
            try {
                readyConsumeCondition.await();
                log.info("suanpan sdk start consuming mq msg ...");
            } catch (InterruptedException e) {
                log.error("wait to consume mq message error", e);
            } finally {
                consumeLock.unlock();
            }

            while (true) {
                RedisFuture<List<Object>> future = consumeConnection.async().dispatch(CommandType.XREADGROUP, new NestedMultiOutput<>(StringCodec.UTF8), args);
                MqMessage mqMessage;
                try {
                    List<Object> consumedObjects = future.get();
                    mqMessage = MqMessage.convert((List) consumedObjects.get(0));
                } catch (InterruptedException | ExecutionException e) {
                    // TODO: 2024/3/13   发送消费异常事件
                    log.error("consume message from mq error", e);
                    LockSupport.parkNanos(restartDelay * 1000000);
                    continue;
                }

                List<MetaInflowMessage> metaInflowMessages = mqMessage.getMessages();
                if (CollectionUtils.isEmpty(metaInflowMessages)) {
                    log.warn("consume message from mq is empty");
                    continue;
                }

                metaInflowMessages.stream().forEach(metaInflowMessage -> {
                    ack(queue, group, metaInflowMessage.getMetaContext().getMessageId());
                    if (metaInflowMessage.isExpired()) {
                        log.info("message is expired, no need to process, msg: {}", JSON.toJSONString(metaInflowMessage));
                        return;
                    }

                    if (metaInflowMessage.isEmpty()) {
                        log.info("message has no inPort data, no need to process, msg: {}", JSON.toJSONString(metaInflowMessage));
                        return;
                    }

                    if (isPolling) {
                        lock.lock();
                        try {
                            inflowMessage = metaInflowMessage.covert();
                            pollingAvailableCondition.signal();
                        } finally {
                            lock.unlock();
                        }
                    }

                    InvokeHandlerTask invokeHandlerTask = new InvokeHandlerTask(metaInflowMessage);
                    try {
                        ThreadPool.pool().submit(invokeHandlerTask);
                    } catch (RejectedExecutionException e) {
                        try {
                            log.warn("consume stream message too fast and invoke too late, need wait a moment to" +
                                    " consume message and submit invoke task!");
                            ThreadPool.pool().getQueue().put(invokeHandlerTask);
                        } catch (InterruptedException ex) {
                            log.error("wait a moment to consume message and submit invoke task", e);
                        }
                    }
                });
            }
        }
    }

    private class InvokeHandlerTask implements Runnable {
        private final MetaInflowMessage metaInflowMessage;

        public InvokeHandlerTask(MetaInflowMessage metaInflowMessage) {
            this.metaInflowMessage = metaInflowMessage;
        }

        @Override
        public void run() {
            try {
                MetaOutflowMessage metaOutflowMessage = proxy.invoke(metaInflowMessage);
                if (metaOutflowMessage != null && !metaOutflowMessage.isEmpty()) {
                    publish(metaOutflowMessage);
                }
            } catch (StreamGlobalException e) {
                log.error("invoke suanpan handler error", e);
            }
        }
    }
}
