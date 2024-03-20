package com.xuelang.suanpan.stream;

import com.xuelang.suanpan.common.BaseSpDomainEntity;
import com.xuelang.suanpan.common.ProxrConnectionParam;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.stream.consumer.Consumer;
import com.xuelang.suanpan.stream.handler.HandlerProxy;
import com.xuelang.suanpan.stream.handler.HandlerRequest;
import com.xuelang.suanpan.stream.message.StreamContext;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandKeyword;
import io.lettuce.core.protocol.CommandType;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Stream extends BaseSpDomainEntity implements IStream {
    private Consumer consumer;
    private HandlerProxy proxy;
    private final RedisClient client;
    private final StatefulRedisConnection<String, String> consumeConnection;
    private final StatefulRedisConnection<String, String> sendConnection;
    private static final String SEARCH_MSG = "BUSYGROUP Consumer Group name already exists";


    private Stream() {
        super();
        RedisURI uri = RedisURI.Builder.redis(ConstantConfiguration.getStreamHost(), ConstantConfiguration.getStreamPort()).withPassword("123456").build();
        this.client = RedisClient.create(uri);
        this.client.setOptions(ClientOptions.builder().autoReconnect(true).pingBeforeActivateConnection(true).build());
        this.consumeConnection = this.client.connect();
        this.sendConnection = this.client.connect();
        this.proxy = new HandlerProxy();
        consumer = createConsumerGroup(ConstantConfiguration.getReceiveQueue(), null, null, false, null);
        consumer.doTask(proxy);
    }

    private Stream(ProxrConnectionParam proxrConnectionParam) {
        super(proxrConnectionParam);
        RedisURI uri = RedisURI.Builder.redis(ConstantConfiguration.getStreamHost(), ConstantConfiguration.getStreamPort()).build();
        this.client = RedisClient.create(uri);
        this.consumeConnection = this.client.connect();
        this.sendConnection = this.client.connect();
        consumer = createConsumerGroup(ConstantConfiguration.getReceiveQueue(), null, null, false, null);
        consumer.doTask(proxy);
    }

    @Override
    public String publish(StreamContext streamContext) throws NullPointerException {
        Objects.requireNonNull(streamContext, "stream context param can not be null");
        return consumer.publish(streamContext);
    }

    @Override
    public HandlerRequest polling(long timeout, TimeUnit unit) {
        return consumer.polling(timeout, unit);
    }

    private Consumer createConsumerGroup(String queue, @Nullable String group, @Nullable String consumedMsgId, boolean isNoAck, @Nullable Long blockTimeout) {
        queue = Objects.requireNonNull(queue, "queue can not be null");
        Consumer consumer = new Consumer();
        consumer.setQueue(queue);
        consumer.setNoAck(isNoAck);
        if (StringUtils.isNotBlank(group)) {
            consumer.setGroup(group);
        }
        if (StringUtils.isNotBlank(consumedMsgId)) {
            consumer.setConsumedMessageId(consumedMsgId);
        }
        if (blockTimeout != null) {
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
}
