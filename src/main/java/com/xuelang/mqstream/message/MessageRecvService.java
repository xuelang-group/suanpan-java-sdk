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
 * @Auther: zigui.zdf
 * @Date: 2019/11/5 14:20
 * @Description:
 */
@Slf4j
public class MessageRecvService {

    private DefaultMessageRecvHandler defaultMessageRecvHandler;

    private ExecutorService executorService;

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
     *
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
