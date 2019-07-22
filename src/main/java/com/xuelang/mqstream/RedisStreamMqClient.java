package com.xuelang.mqstream;

import com.xuelang.mqstream.handler.ExceptionHandler;
import com.xuelang.mqstream.handler.XReadGroupHandler;
import com.xuelang.mqstream.options.Consumer;
import com.xuelang.mqstream.options.Message;
import com.xuelang.mqstream.options.Queue;
import com.xuelang.mqstream.response.XReadGroupResponse;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.XAddArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.*;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandKeyword;
import io.lettuce.core.protocol.CommandType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RedisStreamMqClient implements MqClient {

    private RedisClient client;
    private StatefulRedisConnection<String, String> connection;

    public RedisStreamMqClient(String url) {
        this.client = RedisClient.create(url);
        this.connection = this.client.connect();
    }

    public RedisStreamMqClient(String host, int port) {
        RedisURI uri = RedisURI.Builder.redis(host, port).build();
        this.client = RedisClient.create(uri);
        this.connection = this.client.connect();
    }

    public RedisStreamMqClient(String host, int port, String password) {
        RedisURI uri = RedisURI.Builder.redis(host, port).withPassword(password).build();
        this.client = RedisClient.create(uri);
        this.connection = this.client.connect();
    }

    public void createQueue(Queue queue, boolean existedOk) {
        RedisAsyncCommands<String, String> commands = this.connection.async();
        StringCodec codec = StringCodec.UTF8;
        CommandArgs<String, String> args = new CommandArgs<>(codec)
                .add(CommandKeyword.CREATE)
                .add(queue.getName())
                .add(queue.getGroup())
                .add(queue.getConsumeId());

        if (queue.isMkStream()) {
            args.add("MKSTREAM");
        }
        RedisFuture<String> future = commands.dispatch(CommandType.XGROUP, new StatusOutput<>(codec), args);
        try {
            future.get();
        } catch (ExecutionException e) {
            if (
                    existedOk &&
                    StringUtils.containsIgnoreCase(e.getMessage(), "BUSYGROUP Consumer Group name already exists")
            ) {
                return;
            }
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String sendMessage(Message message) {
        RedisAsyncCommands<String, String> commands = this.connection.async();
        XAddArgs addArgs = XAddArgs.Builder
                .maxlen(message.getMaxLength())
                .approximateTrimming(message.isApproximateTrimming());
        List<Object> keysAndValues = new ArrayList<>(Arrays.asList(message.getKeysAndValues()));
        keysAndValues.add("request_id");
        keysAndValues.add(message.getRequestId());
        Object[] data = keysAndValues.toArray(new Object[0]);
        RedisFuture<String> future =
                commands.xadd(message.getQueue(), addArgs, data);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribeQueue(
            Consumer consumer,
            XReadGroupHandler handler,
            ExceptionHandler exceptionHandler
    ) {
        Queue queue = Queue.builder()
                .name(consumer.getQueue())
                .group(consumer.getGroup())
                .build();
        createQueue(queue, true);

        RedisAsyncCommands<String, String> commands = this.connection.async();
        StringCodec codec = StringCodec.UTF8;
        CommandArgs<String, String> args = new CommandArgs<>(codec)
                .add(CommandKeyword.GROUP)
                .add(consumer.getGroup())
                .add(consumer.getName());

        if (consumer.isNoAck())
            args.add(CommandKeyword.NOACK);
        if (consumer.isBlock()) {
            args.add(CommandKeyword.BLOCK).add(0);
        }
        args.add(CommandKeyword.COUNT)
                .add(consumer.getCount())
                .add("STREAMS")
                .add(consumer.getQueue())
                .add(consumer.getConsumeId());

        // runs infinity
        while (true) {
            RedisFuture<List<Object>> future = commands.dispatch(CommandType.XREADGROUP, new NestedMultiOutput<>(codec), args);
            try {
                List<Object> consumedObjects = future.get();
                XReadGroupResponse response = XReadGroupResponse.fromOutput((List) consumedObjects.get(0));
                handler.handle(response);
                String[] messageIds = response.getMessageIds().toArray(new String[0]);
                ackMessage(consumer.getQueue(), consumer.getGroup(), messageIds);
            } catch (InterruptedException | ExecutionException e) {
                exceptionHandler.handle(e);
                try {
                    Thread.sleep(consumer.getDelay());
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                createQueue(queue, true);
            }
        }
    }

    @Override
    public void ackMessage(String queue, String group, String... messageIds) {
        RedisAsyncCommands<String, String> commands = this.connection.async();
        RedisFuture<Long> xack = commands.xack(queue, group, messageIds);
        try {
            xack.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void destroy() {
        if (this.connection != null)
            this.connection.close();
        if (this.client != null)
            this.client.shutdown();
    }
}
