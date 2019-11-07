package com.xuelang.mqstream.message;

import com.xuelang.mqstream.MqClient;
import com.xuelang.mqstream.MqClientFactory;
import com.xuelang.mqstream.config.GlobalConfig;
import com.xuelang.mqstream.handler.DefaultLogExceptionMessageHandler;
import com.xuelang.mqstream.handler.DefaultMessageRecvHandler;
import com.xuelang.mqstream.handler.XReadGroupHandler;
import com.xuelang.mqstream.options.Consumer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/5 14:20
 * @Description:
 */
@Slf4j
public class MessageRecvService {

    private DefaultMessageRecvHandler defaultMessageRecvHandler;

    public MessageRecvService(List<Object> businessListenerInstances) {
//        defaultMessageRecvHandler = new DefaultMessageRecvHandler(businessListenerInstances);
    }

    public void subscribeMsg() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future future = executorService.submit(() -> {
            subscribeQueue(defaultMessageRecvHandler);
        });
        try {
            future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        executorService.shutdown();
    }

    public void subscribeMsg(XReadGroupHandler handler) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future future = executorService.submit(() -> {
            subscribeQueue(handler);
        });
        try {
            future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        executorService.shutdown();
    }

    private void subscribeQueue(XReadGroupHandler handler) {
        MqClient mqClient = MqClientFactory.getMqClient();

        Consumer consumer = Consumer.builder()
                .queue(GlobalConfig.streamRecvQueue)
                .group(GlobalConfig.nodeGroup)
                .name(GlobalConfig.streamNodeId)
                .delay(GlobalConfig.streamRecvQueueDelay)
                .build();

        log.info("subscribe queue: {}, group: {}, consumer: {}",
                consumer.getQueue(), consumer.getGroup(), consumer.getName());

        mqClient.subscribeQueue(
                consumer,
                handler,
                new DefaultLogExceptionMessageHandler()
        );
    }
}
