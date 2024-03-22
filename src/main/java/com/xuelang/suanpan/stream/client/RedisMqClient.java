package com.xuelang.suanpan.stream.client;

import com.alibaba.fastjson2.JSON;
import com.xuelang.suanpan.common.exception.IllegalRequestException;
import com.xuelang.suanpan.common.exception.InvocationHandlerException;
import com.xuelang.suanpan.common.exception.NoSuchHandlerException;
import com.xuelang.suanpan.common.pool.ThreadPool;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.stream.handler.HandlerProxy;
import com.xuelang.suanpan.stream.handler.HandlerRequest;
import com.xuelang.suanpan.stream.handler.HandlerResponse;
import com.xuelang.suanpan.stream.message.Message;
import com.xuelang.suanpan.stream.message.MqResponse;
import com.xuelang.suanpan.stream.message.StreamContext;
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
    private volatile HandlerRequest pollingResponse;
    private final RedisClient client;
    private final StatefulRedisConnection<String, String> consumeConnection;
    private final StatefulRedisConnection<String, String> sendConnection;
    private static final Lock lock = new ReentrantLock();
    private static final Condition consumedData = lock.newCondition();

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
        RedisURI uri = RedisURI.Builder.redis(ConstantConfiguration.getStreamHost(), ConstantConfiguration.getStreamPort()).withPassword("123456").build();
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
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
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
    public HandlerRequest polling(long timeout, TimeUnit unit) {
        lock.lock();
        pollingResponse = null;
        isPolling = true;
        try {
            consumedData.await(timeout, unit);
            isPolling = false;
            return pollingResponse;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
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

        ThreadPool.pool().submit(new InfiniteConsumeTask(consumeArgs, proxy));
    }

    @Override
    public String publish(StreamContext streamContext) {
        streamContext.getExtra().updateMsgNodeOutTime();
        XAddArgs addArgs = XAddArgs.Builder.maxlen(streamContext.getMaxLength())
                .approximateTrimming(streamContext.isApproximateTrimming());
        if (streamContext.isP2p()) {
            Map<String, Object[]> p2pDataMap = streamContext.toP2PQueueData();
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
                    log.info("publish queue data:{}", JSON.toJSONString(entry.getValue()));
                }
            });

            return publishResults.toString();
        } else {
            Object[] outData = streamContext.toMasterQueueData();
            RedisFuture<String> future = this.sendConnection.async().xadd(streamContext.getSendQueue(), addArgs, outData);
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("publish message to MQ error", e);
                return null;
            } finally {
                log.info("publish queue data:{}", JSON.toJSONString(outData));
            }
        }
    }

    private void ack(String queue, String group, String messageId) {
        RedisFuture<Long> xack = this.sendConnection.async().xack(queue, group, messageId);
        try {
            xack.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            log.error("xack to MQ error", e);
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
            log.info("suanpan sdk start consume queue msg ...");
            while (true) {
                RedisFuture<List<Object>> future = consumeConnection.async().dispatch(CommandType.XREADGROUP, new NestedMultiOutput<>(StringCodec.UTF8), args);
                MqResponse mqResponse = null;
                try {
                    //block get
                    List<Object> consumedObjects = future.get();
                    mqResponse = MqResponse.convert((List) consumedObjects.get(0));
                } catch (InterruptedException | ExecutionException e) {
                    // TODO: 2024/3/13   发送消费异常事件
                    log.error("consume message from MQ error", e);
                    LockSupport.parkNanos(restartDelay * 1000000);
                    continue;
                }

                List<Message> messages = mqResponse.getMessages();
                if (CollectionUtils.isEmpty(messages)) {
                    continue;
                }

                log.info("receive queue data:{}", JSON.toJSONString(messages));
                messages.stream().forEach(message -> {
                    ThreadPool.pool().submit(() -> {
                        try {
                            if (message.isExpired()) {
                                log.info("message is expired, no need to process, msg: {}", JSON.toJSONString(message));
                                return;
                            }

                            HandlerRequest request = message.covert();
                            if (isPolling) {
                                if (request != null) {
                                    pollingResponse = request;
                                    lock.lock();
                                    consumedData.signal();
                                }
                            } else {
                                HandlerResponse handlerResponse = proxy.invoke(request);
                                if (handlerResponse != null && !handlerResponse.getOutPortDataMap().isEmpty()) {
                                    StreamContext streamContext = new StreamContext();
                                    if (handlerResponse.getValiditySeconds() != null) {
                                        message.getExtra().setExpireTime(System.currentTimeMillis() + handlerResponse.getValiditySeconds() * 1000);
                                    } else {
                                        message.getExtra().setExpireTime(Long.MAX_VALUE);
                                    }
                                    streamContext.setExtra(message.getExtra());
                                    streamContext.setRequestId(message.getRequestId());
                                    streamContext.setSuccess(message.getSuccess());
                                    streamContext.setOutPortDataMap(handlerResponse.getOutPortDataMap());
                                    publish(streamContext);
                                }
                            }

                            ack(queue, group, message.getMessageId());
                        } catch (NoSuchHandlerException | IllegalRequestException e) {
                            ack(queue, group, message.getMessageId());
                            log.error("invoke suanpan handler error", e);
                        } catch (InvocationHandlerException e) {
                            // TODO: 2024/3/18  发送事件到算盘平台
                            log.error("invoke suanpan handler error", e);
                        } finally {
                            lock.unlock();
                        }
                    });
                });
            }
        }
    }
}
