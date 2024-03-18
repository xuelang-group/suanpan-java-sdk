package com.xuelang.suanpan.stream;

import com.xuelang.suanpan.common.BaseSpDomainEntity;
import com.xuelang.suanpan.configuration.SpEnv;
import com.xuelang.suanpan.domain.handler.HandlerProxy;
import com.xuelang.suanpan.domain.io.InPort;
import com.xuelang.suanpan.domain.io.OutPort;
import com.xuelang.suanpan.domain.proxr.ProxrConnectionParam;
import com.xuelang.suanpan.stream.consumer.Consumer;
import com.xuelang.suanpan.stream.dto.StreamContext;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandKeyword;
import io.lettuce.core.protocol.CommandType;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Stream extends BaseSpDomainEntity implements IStream {
    private HandlerProxy proxy;
    private final RedisClient client;
    private final StatefulRedisConnection<String, String> consumeConnection;
    private final StatefulRedisConnection<String, String> sendConnection;
    private static final String SEARCH_MSG = "BUSYGROUP Consumer Group name already exists";


    private Stream() {
        super();
        RedisURI uri = RedisURI.Builder.redis(SpEnv.getStreamHost(), SpEnv.getStreamPort()).withPassword("Redis@edge").build();
        this.client = RedisClient.create(uri);
        this.client.setOptions(ClientOptions.builder().autoReconnect(true).pingBeforeActivateConnection(true).build());
        this.consumeConnection = this.client.connect();
        this.sendConnection = this.client.connect();
        this.proxy = new HandlerProxy();
    }

    private Stream(ProxrConnectionParam proxrConnectionParam) {
        super(proxrConnectionParam);
        RedisURI uri = RedisURI.Builder.redis(SpEnv.getStreamHost(), SpEnv.getStreamPort()).build();
        this.client = RedisClient.create(uri);
        this.consumeConnection = this.client.connect();
        this.sendConnection = this.client.connect();
    }

    @Override
    public String publish(Set<OutPort> outPorts, Object data) {
        return null;
    }

    @Override
    public List<StreamContext> polling(InPort inPort, long timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public void subscribe() {
        Consumer consumer = createConsumerGroup(SpEnv.getReceiveQueue(),null,null,false,null);
        consumer.doTask(proxy);
    }

    private Consumer createConsumerGroup(String queue, @Nullable String group, @Nullable String consumedMsgId, boolean isNoAck, @Nullable Long blockTimeout) {
        queue = Objects.requireNonNull(queue, "queue can not be null");
        Consumer consumer = new Consumer();
        consumer.setQueue(queue);
        consumer.setNoAck(isNoAck);
        if (StringUtils.isNotBlank(group)){
            consumer.setGroup(group);
        }
        if (StringUtils.isNotBlank(consumedMsgId)){
            consumer.setConsumedMessageId(consumedMsgId);
        }
        if (blockTimeout != null){
            consumer.setRestartDelay(blockTimeout);
        }

        CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
                .add(CommandKeyword.CREATE)
                .add(consumer.getQueue())
                .add(consumer.getGroup())
                .add("0")
                .add("MKSTREAM");

        RedisAsyncCommands<String, String> commands = this.consumeConnection.async();
        consumer.setConsumeConnection(this.consumeConnection);
        consumer.setSendConnection(this.sendConnection);
        RedisFuture<String> future = commands.dispatch(CommandType.XGROUP, new StatusOutput<>(StringCodec.UTF8), args);
        try {
            future.get();
        } catch (ExecutionException e) {
            if (StringUtils.containsIgnoreCase(e.getMessage(), SEARCH_MSG)) {
                return consumer;
            }
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return consumer;
    }

    /*private void subscribe(Consumer consumer) {
        CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
                .add(CommandKeyword.GROUP).add(consumer.getGroup())
                .add(CommandKeyword.SETNAME).add(consumer.getName())
                //block时间设置为0，则一直保持阻塞
                .add(CommandKeyword.BLOCK).add(0);

        if (consumer.isNoAck()) {
            args.add(CommandKeyword.NOACK);
        }

        args.add(CommandKeyword.COUNT)
                .add(consumer.getCount())
                .add("STREAMS")
                .add(consumer.getQueue())
                .add(consumer.getConsumedMessageId());

        consume(consumer, this.recvConnection.async(), args);
    }*/

    /*private void consume(Consumer consumer, RedisAsyncCommands<String, String> commands, CommandArgs<String, String> args) {
        while (true) {
            RedisFuture<List<Object>> future = commands.dispatch(CommandType.XREADGROUP, new NestedMultiOutput<>(StringCodec.UTF8), args);
            MQResponse MQResponse = null;
            try {
                //block get
                List<Object> consumedObjects = future.get();
                MQResponse = MQResponse.convert((List) consumedObjects.get(0));
            } catch (InterruptedException | ExecutionException e) {
                // TODO: 2024/3/13   发送消费异常事件
                LockSupport.parkNanos(consumer.getRestartDelay() * 1000000);
            }

            List<Message> messages = MQResponse.getMessages();
            if (CollectionUtils.isEmpty(messages)){
                continue;
            }

            messages.stream().forEach(message -> {
                ThreadPool.pool().submit(() -> {
                    try {
                        HandlerResponse handlerResponse = proxy.invoke(message);
                        if (handlerResponse != null && !handlerResponse.getOutPortDataMap().isEmpty()) {
                            StreamContext streamContext = new StreamContext();
                            streamContext.setExtra(message.getExtra());
                            streamContext.setRequestId(message.getRequestId());
                            streamContext.setSuccess(message.getSuccess());
                            streamContext.setOutPortDataMap(handlerResponse.getOutPortDataMap());
                            publish(streamContext);
                        }

                        ack(consumer.getQueue(), consumer.getGroup(), message.getMessageId());
                    } catch (RuntimeException e){
                        // TODO: 2024/3/13 发送处理异常事件,或者将消息重新回流到组件的接收队列中做补偿
                    }
                });
            });
        }
    }*/

    /*private String publish(StreamContext streamContext) {
        RedisAsyncCommands<String, String> commands = this.sendConnection.async();
        XAddArgs addArgs = XAddArgs.Builder
                .maxlen(streamContext.getMaxLength())
                .approximateTrimming(streamContext.isApproximateTrimming());
        if (streamContext.isP2p()){
            // TODO: 2024/3/13 p2p发送
            return null;
        } else {
            RedisFuture<String> future =
                    commands.xadd(streamContext.getMasterQueue(), addArgs, streamContext.toMasterQueueData());
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }*/


    private void acks(String queue, String group, String... messageIds) {
        RedisAsyncCommands<String, String> commands = this.consumeConnection.async();
        RedisFuture<Long> xack = commands.xack(queue, group, messageIds);
        try {
            xack.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void ack(String queue, String group, String messageId) {
        RedisAsyncCommands<String, String> commands = this.consumeConnection.async();
        RedisFuture<Long> xack = commands.xack(queue, group, messageId);
        try {
            xack.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
