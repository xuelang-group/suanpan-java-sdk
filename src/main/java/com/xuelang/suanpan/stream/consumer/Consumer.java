package com.xuelang.suanpan.stream.consumer;

import com.xuelang.suanpan.common.ThreadPool;
import com.xuelang.suanpan.common.exception.IllegalRequestException;
import com.xuelang.suanpan.common.exception.InvocationHandlerException;
import com.xuelang.suanpan.common.exception.NoSuchHandlerException;
import com.xuelang.suanpan.domain.handler.HandlerProxy;
import com.xuelang.suanpan.domain.handler.HandlerResponse;
import com.xuelang.suanpan.stream.dto.MQResponse;
import com.xuelang.suanpan.stream.dto.Message;
import com.xuelang.suanpan.stream.dto.StreamContext;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.XAddArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.NestedMultiOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandKeyword;
import io.lettuce.core.protocol.CommandType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.LockSupport;

@Slf4j
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
    private StatefulRedisConnection<String, String> consumeConnection;

    private StatefulRedisConnection<String, String> sendConnection;

    public void setConsumeConnection(StatefulRedisConnection<String, String> consumeConnection) {
        this.consumeConnection = consumeConnection;
    }

    public void setSendConnection(StatefulRedisConnection<String, String> sendConnection) {
        this.sendConnection = sendConnection;
    }

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

    public void destroy() {
        if (this.consumeConnection != null) {
            this.consumeConnection.close();
        }
    }

    public void doTask(HandlerProxy proxy) {
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

        ThreadPool.pool().submit(new Task(consumeArgs, proxy));
    }

    private String publish(StreamContext streamContext) {
        XAddArgs addArgs = XAddArgs.Builder.maxlen(streamContext.getMaxLength())
                .approximateTrimming(streamContext.isApproximateTrimming());
        if (streamContext.isP2p()) {
            // TODO: 2024/3/13 p2p发送
            return null;
        } else {
            RedisFuture<String> future =
                    this.sendConnection.async().xadd(streamContext.getMasterQueue(), addArgs, streamContext.toMasterQueueData());
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private void ack(String queue, String group, String messageId) {
        RedisFuture<Long> xack = this.sendConnection.async().xack(queue, group, messageId);
        try {
            xack.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    class Task implements Runnable {
        final HandlerProxy proxy;
        final CommandArgs<String, String> args;

        public Task(CommandArgs<String, String> args, HandlerProxy proxy) {
            this.proxy = proxy;
            this.args = args;
        }

        @Override
        public void run() throws RuntimeException {
            log.info("suanpan sdk start consume queue msg ...");
            while (true) {
                RedisFuture<List<Object>> future = consumeConnection.async().dispatch(CommandType.XREADGROUP, new NestedMultiOutput<>(StringCodec.UTF8), args);
                MQResponse mqResponse = null;
                try {
                    //block get
                    List<Object> consumedObjects = future.get();
                    log.info("received message: {}", consumedObjects.get(0).toString());
                    mqResponse = MQResponse.convert((List) consumedObjects.get(0));
                } catch (InterruptedException | ExecutionException e) {
                    // TODO: 2024/3/13   发送消费异常事件
                    e.printStackTrace();
                    LockSupport.parkNanos(restartDelay * 1000000);
                    continue;
                }

                List<Message> messages = mqResponse.getMessages();
                if (CollectionUtils.isEmpty(messages)) {
                    continue;
                }

                messages.stream().forEach(message -> {
                    ThreadPool.pool().submit(() -> {
                        try {
                            HandlerResponse handlerResponse = proxy.invoke(message.covert());
                            if (handlerResponse != null && !handlerResponse.getOutPortDataMap().isEmpty()) {
                                StreamContext streamContext = new StreamContext();
                                streamContext.setExtra(message.getExtra());
                                streamContext.setRequestId(message.getRequestId());
                                streamContext.setSuccess(message.getSuccess());
                                streamContext.setOutPortDataMap(handlerResponse.getOutPortDataMap());
                                publish(streamContext);
                            }

                            ack(queue, group, message.getMessageId());
                        } catch (NoSuchHandlerException | IllegalRequestException e) {
                            ack(queue, group, message.getMessageId());
                            log.error("invoke suanpan handler error", e);
                        } catch (InvocationHandlerException e) {
                            // TODO: 2024/3/18  发送事件到算盘平台
                            log.error("invoke suanpan handler error", e);
                        }
                    });
                });
            }
        }
    }
}
