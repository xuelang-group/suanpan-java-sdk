package com.xuelang.suanpan.stream.client;

import com.alibaba.fastjson2.JSON;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.xuelang.suanpan.common.exception.GlobalExceptionType;
import com.xuelang.suanpan.common.exception.StreamGlobalException;
import com.xuelang.suanpan.common.pool.ThreadPool;
import com.xuelang.suanpan.common.utils.SerializeUtil;
import com.xuelang.suanpan.common.utils.ParameterUtil;
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
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

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
    private long defaultOnceConsumeCount = 1L;
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
    private static final Lock pollingLock = new ReentrantLock();
    private final RedisClient client;
    private final StatefulRedisConnection<String, String> consumeConnection;
    private final StatefulRedisConnection<String, String> sendConnection;
    private volatile InfiniteConsumer infiniteConsumer;
    private final HandlerProxy proxy;

    public RedisMqClient(HandlerProxy proxy, String queue, String group, String consumedMsgId, boolean isNoAck, Long restartDelay) {
        this.queue = queue;
        this.noAck = isNoAck;
        if (StringUtils.isNotBlank(group)) {
            this.group = group;
        }
        if (StringUtils.isNotBlank(consumedMsgId)) {
            this.consumedMessageId = consumedMsgId;
        }
        if (restartDelay != null) {
            this.restartDelay = restartDelay;
        }

        RedisURI uri = RedisURI.Builder.redis(ParameterUtil.getStreamHost(), ParameterUtil.getStreamPort()).build();
        this.client = RedisClient.create(uri);
        this.client.setOptions(ClientOptions.builder().autoReconnect(true).pingBeforeActivateConnection(true).build());
        this.consumeConnection = this.client.connect();
        this.sendConnection = this.client.connect();
        this.proxy = proxy;
        createConsumerGroup();
    }

    @Override
    public void destroy() {
        if (this.consumeConnection != null) {
            this.consumeConnection.close();
        }

        if (this.sendConnection != null) {
            this.sendConnection.close();
        }

        this.client.shutdown();
    }

    @Override
    public List<InflowMessage> polling(int count, long timeoutMillis) throws StreamGlobalException {
        if (infiniteConsumer != null) {
            throw new StreamGlobalException(GlobalExceptionType.IllegalStreamOperation);
        }

        long start = System.currentTimeMillis();
        try {
            if (pollingLock.tryLock(timeoutMillis, TimeUnit.MILLISECONDS)) {
                long blockMillis = timeoutMillis - (System.currentTimeMillis() - start);
                List<Object> consumedObjects;
                log.info("start polling message");
                try {
                    RedisFuture<List<Object>> future = consumeConnection.async().dispatch(CommandType.XREADGROUP,
                            new NestedMultiOutput<>(StringCodec.UTF8), createCmdArgs(name, blockMillis, count));
                    consumedObjects = future.get(blockMillis, TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ExecutionException e) {
                    log.warn("polling message from mq error", e);
                    return null;
                } catch (TimeoutException e) {
                    log.warn("polling message from mq timeout", e);
                    return null;
                } finally {
                    log.info("finish polling message");
                    pollingLock.unlock();
                }

                MqMessage mqMessage = null;
                try {
                    log.debug("consumed meta mq message:{}", JSON.toJSONString(consumedObjects));
                    Object item = consumedObjects.get(0);
                    if (item instanceof List) {
                        mqMessage = MqMessage.convert((List) item);
                    } else {
                        mqMessage = MqMessage.convert(consumedObjects);
                    }
                } catch (Exception e) {
                    log.warn("convert meta mq message error", e);
                    return null;
                }

                acks(mqMessage.getMessageIds().toArray(new String[0]));
                List<MetaInflowMessage> metaInflowMessages = mqMessage.getMessages();
                if (CollectionUtils.isEmpty(metaInflowMessages)) {
                    log.warn("consume message from mq is empty");
                    return null;
                }

                return metaInflowMessages.stream().map(metaInflowMessage -> {
                    if (metaInflowMessage.isExpired()) {
                        log.info("message is expired, no need to process, msg: {}", JSON.toJSONString(metaInflowMessage));
                        return null;
                    }

                    if (metaInflowMessage.isEmpty()) {
                        log.info("message has no inPort data, no need to process, msg: {}", JSON.toJSONString(metaInflowMessage));
                        return null;
                    }

                    return metaInflowMessage.covert();
                }).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("polling message error, " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public void subscribe() {
        if (infiniteConsumer == null) {
            synchronized (this) {
                if (infiniteConsumer == null) {
                    infiniteConsumer = new InfiniteConsumer(createCmdArgs(name, 0, defaultOnceConsumeCount));
                    infiniteConsumer.start();
                }
            }
        }
    }

    @Override
    public String publish(MetaOutflowMessage metaOutflowMessage) {
        metaOutflowMessage.updateMsgNodeOutTime();
        XAddArgs addArgs = XAddArgs.Builder.maxlen(metaOutflowMessage.getMaxLength())
                .approximateTrimming(metaOutflowMessage.isApproximateTrimming());
        if (metaOutflowMessage.isP2p()) {
            Map<String, Object[]> p2pDataMap = metaOutflowMessage.toP2PStreamData();
            if (p2pDataMap == null || p2pDataMap.isEmpty()) {
                return null;
            }

            List<String> publishResults = new ArrayList<>();
            p2pDataMap.entrySet().parallelStream().forEach(entry -> {
                RedisFuture<String> future = this.sendConnection.async().xadd(entry.getKey(), addArgs, entry.getValue());
                try {
                    publishResults.add(future.get());
                    log.info("publish to queue={}, data={}", entry.getKey(), SerializeUtil.serialize(entry.getValue()));
                } catch (InterruptedException | ExecutionException e) {
                    log.warn("publish message to MQ error", e);
                }
            });

            return publishResults.toString();
        }


        Object[] outData = metaOutflowMessage.toStreamData();
        RedisFuture<String> future = this.sendConnection.async().xadd(metaOutflowMessage.getSendMasterQueue(), addArgs, outData);
        try {
            String messageId = future.get();
            log.info("publish to queue={}, data={}", metaOutflowMessage.getSendMasterQueue(), SerializeUtil.serialize(outData));
            return messageId;
        } catch (InterruptedException | ExecutionException e) {
            log.warn("publish message to MQ error", e);
            return null;
        }
    }

    private void createConsumerGroup() {
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

            log.warn("block get mq message error", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            log.warn("block get mq message error", e);
            throw new RuntimeException(e);
        }
    }

    private void ack(String messageId) {
        RedisFuture<Long> xack = this.sendConnection.async().xack(queue, group, messageId);
        try {
            xack.get();
        } catch (InterruptedException | ExecutionException e) {
            log.warn("xack to MQ error", e);
        }
    }

    private void acks(String[] messageIds) {
        RedisFuture<Long> xack = this.sendConnection.async().xack(queue, group, messageIds);
        try {
            xack.get();
        } catch (InterruptedException | ExecutionException e) {
            log.warn("xack to MQ error", e);
        }
    }

    private CommandArgs<String, String> createCmdArgs(String consumerName, long blockMillis, long count) {
        CommandArgs<String, String> consumeArgs = new CommandArgs<>(StringCodec.UTF8)
                .add(CommandKeyword.GROUP).add(group)
                .add(consumerName)
                //block时间设置为0，则一直保持阻塞
                .add(CommandKeyword.BLOCK).add(blockMillis)
                .add(CommandKeyword.COUNT).add(count)
                .add("STREAMS").add(queue)
                .add(this.consumedMessageId);
        if (this.noAck) {
            consumeArgs.add(CommandKeyword.NOACK);
        }

        return consumeArgs;
    }

    private class InvocationTask implements Runnable {
        private final MetaInflowMessage metaInflowMessage;

        public InvocationTask(MetaInflowMessage metaInflowMessage) {
            this.metaInflowMessage = metaInflowMessage;
        }

        @Override
        public void run() {
            MetaOutflowMessage metaOutflowMessage = proxy.invoke(metaInflowMessage);
            if (metaOutflowMessage != null && !metaOutflowMessage.isEmpty()) {
                publish(metaOutflowMessage);
            }
        }
    }

    private class InfiniteConsumer implements Runnable {
        private final CommandArgs<String, String> commandArgs;
        //0:停止, 1:运行, 2:暂停
        private RedisFuture<List<Object>> future;
        private final ThreadPoolExecutor singleThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1), new ThreadFactoryBuilder().setNameFormat("single-pool-%d").build(),
                new ThreadPoolExecutor.DiscardPolicy());

        public InfiniteConsumer(CommandArgs<String, String> commandArgs) {
            this.commandArgs = commandArgs;
        }

        @Override
        public void run() throws RuntimeException {
            while (true) {
                List<Object> consumedObjects;
                try {
                    future = consumeConnection.async().dispatch(CommandType.XREADGROUP, new NestedMultiOutput<>(StringCodec.UTF8), commandArgs);
                    consumedObjects = future.get();
                } catch (InterruptedException | ExecutionException e) {
                    log.warn("consume message from mq error", e);
                    LockSupport.parkNanos(restartDelay * 1000000);
                    continue;
                }

                MqMessage mqMessage = null;
                try {
                    log.debug("consumed meta mq message:{}", JSON.toJSONString(consumedObjects));
                    Object item = consumedObjects.get(0);
                    if (item instanceof List) {
                        mqMessage = MqMessage.convert((List) item);
                    } else {
                        mqMessage = MqMessage.convert(consumedObjects);
                    }
                } catch (Exception e) {
                    log.warn("convert meta mq message error", e);
                    continue;
                }

                acks(mqMessage.getMessageIds().toArray(new String[0]));
                List<MetaInflowMessage> metaInflowMessages = mqMessage.getMessages();
                if (CollectionUtils.isEmpty(metaInflowMessages)) {
                    log.warn("consume message from mq is empty");
                    continue;
                }

                metaInflowMessages.stream().forEach(metaInflowMessage -> {
                    ack(metaInflowMessage.getMetaContext().getMessageId());
                    if (metaInflowMessage.isExpired()) {
                        log.info("message is expired, no need to process, msg: {}", JSON.toJSONString(metaInflowMessage));
                        return;
                    }

                    if (metaInflowMessage.isEmpty()) {
                        log.info("message has no inPort data, no need to process, msg: {}", JSON.toJSONString(metaInflowMessage));
                        return;
                    }

                    InvocationTask invocationTask = new InvocationTask(metaInflowMessage);
                    try {
                        ThreadPool.pool().submit(invocationTask);
                    } catch (RejectedExecutionException e) {
                        try {
                            log.warn("consume faster than process, wait a moment to consume");
                            ThreadPool.pool().getQueue().put(invocationTask);
                        } catch (InterruptedException ex) {
                            //log.warn("wait a moment to consume message and submit invoke task", e);
                        }
                    }
                });
            }
        }

        public void start() {
            singleThreadPoolExecutor.submit(this);
        }
    }
}


