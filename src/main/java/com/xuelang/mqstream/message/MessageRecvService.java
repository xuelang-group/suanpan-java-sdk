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
/**
 * @author ellison
 * @date 2020/8/4 12:20 下午
 * @description: 接收消息服务
 */
@Slf4j
public class MessageRecvService {

    private ExecutorService executorService;
    private DefaultMessageRecvHandler defaultMessageRecvHandler;

    public MessageRecvService(List<Object> businessListenerInstances, Class messageDataTypeClass) {
        defaultMessageRecvHandler = new DefaultMessageRecvHandler(businessListenerInstances, messageDataTypeClass);
        executorService = Executors.newFixedThreadPool(1);
    }

    /**
     * 使用默认处理器来处理订阅到的消息
     */
    public void subscribeMsg() {
        subscribeMsg(defaultMessageRecvHandler);
    }

    /**
     * 使用自定义处理器来处理订阅到的消息
     * @param handler
     */
    public void subscribeMsg(XReadGroupHandler handler) {
        executorService.submit(() -> {
            subscribeQueue(handler);
        });
    }

    /**
     * 停止订阅消息
     */
    public void stopSubscribeMsg() {
        if (null != executorService && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
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
