package com.xuelang.suanpan.stream.client;

import com.alibaba.fastjson2.JSON;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.xuelang.suanpan.common.exception.GlobalExceptionType;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final ConsumeWorker consumeWorker;


    public void setRestartDelay(long restartDelay) {
        this.restartDelay = restartDelay;
    }

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

        RedisURI uri = RedisURI.Builder.redis(ConstantConfiguration.getStreamHost(), ConstantConfiguration.getStreamPort()).withPassword("123456").build();
        this.client = RedisClient.create(uri);
        this.client.setOptions(ClientOptions.builder().autoReconnect(true).pingBeforeActivateConnection(true).build());
        this.consumeConnection = this.client.connect();
        this.sendConnection = this.client.connect();
        this.proxy = proxy;
        createConsumerGroup();
        this.consumeWorker = new ConsumeWorker(createCmdArgs(name, 0, defaultOnceConsumeCount), proxy);
    }

    public void createConsumerGroup() {
        CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
                .add(CommandKeyword.CREATE)
                .add(this.queue)
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
    public List<InflowMessage> polling(int count, long timeoutMillis) throws StreamGlobalException {
        if (consumeWorker.consumeStatus.get() == 1){
            throw new StreamGlobalException(GlobalExceptionType.IllegalStreamOperation);
        }

        long start = System.currentTimeMillis();
        try {
            if (pollingLock.tryLock(timeoutMillis, TimeUnit.MILLISECONDS)) {
                long blockMillis = timeoutMillis - (System.currentTimeMillis() - start);
                List<Object> consumedObjects;
                RedisFuture<List<Object>> future = null;
                log.info("start polling message operation");
                try {
                    future = consumeConnection.async().dispatch(CommandType.XREADGROUP,
                            new NestedMultiOutput<>(StringCodec.UTF8), createCmdArgs(name, blockMillis, count));
                    consumedObjects = future.get(blockMillis, TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ExecutionException e) {
                    log.error("polling message from mq error", e);
                    return null;
                } catch (TimeoutException e) {
                    log.error("polling message from mq timeout", e);
                    return null;
                } finally {
                    log.info("finish polling message operation");
                    pollingLock.unlock();
                }

                MqMessage mqMessage = MqMessage.convert((List) consumedObjects.get(0));
                acks(queue, group, mqMessage.getMessageIds().toArray(new String[0]));
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
        } catch (InterruptedException e) {
            log.error("polling message error, " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public void consume() {
        consumeWorker.start();
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
            Object[] outData = metaOutflowMessage.toStreamData();
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

    private void acks(String queue, String group, String[] messageIds) {
        RedisFuture<Long> xack = this.sendConnection.async().xack(queue, group, messageIds);
        try {
            xack.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("xack to MQ error", e);
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

    private class ConsumeWorker implements Runnable {
        private final HandlerProxy proxy;
        private final CommandArgs<String, String> commandArgs;
        //0:停止, 1:运行, 2:暂停
        private AtomicInteger consumeStatus = new AtomicInteger(0);
        private RedisFuture<List<Object>> future;
        private final ThreadPoolExecutor singleThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1), new ThreadFactoryBuilder().setNameFormat("single-pool-%d").build(),
                new ThreadPoolExecutor.DiscardPolicy());

        public ConsumeWorker(CommandArgs<String, String> commandArgs, HandlerProxy proxy) {
            this.proxy = proxy;
            this.commandArgs = commandArgs;
        }

        @Override
        public void run() throws RuntimeException {
            while (consumeStatus.get() == 1) {
                log.info("do fixed consume task");
                List<Object> consumedObjects;
                try {
                    future = consumeConnection.async().dispatch(CommandType.XREADGROUP, new NestedMultiOutput<>(StringCodec.UTF8), commandArgs);
                    consumedObjects = future.get();
                    log.info("fixed consume task consumed message:{}", JSON.toJSONString(consumedObjects.get(0)));
                } catch (InterruptedException | ExecutionException e) {
                    log.error("consume message from mq error", e);
                    LockSupport.parkNanos(restartDelay * 1000000);
                    continue;
                }

                MqMessage mqMessage = MqMessage.convert((List) consumedObjects.get(0));
                acks(queue, group, mqMessage.getMessageIds().toArray(new String[0]));

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

                    RedisMqClient.InvokeHandlerTask invokeHandlerTask = new RedisMqClient.InvokeHandlerTask(metaInflowMessage);
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

        public synchronized void pause() {
            if (consumeStatus.compareAndSet(1, 2)) {
                if (future != null) {
                    future.cancel(true);
                    log.info("pause fixed consume task");
                }


                try {
                    singleThreadPoolExecutor.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }

        public void start() {
            if (consumeStatus.compareAndSet(0, 1)) {
                log.info("start fixed consume task");
                singleThreadPoolExecutor.submit(this);
            }
        }

        public synchronized void resume() {
            if (consumeStatus.compareAndSet(2, 1)) {
                log.info("resume fixed consume task");
                singleThreadPoolExecutor.notify();
            }
        }
    }
}


