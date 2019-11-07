package com.xuelang.mqstream;

import com.xuelang.mqstream.message.MqSendService;
import com.xuelang.mqstream.message.MqSendServiceFactory;
import com.xuelang.mqstream.options.Consumer;
import com.xuelang.mqstream.options.Message;
import example.ExampleExceptionHandler;
import example.ExampleReadGroupHandler;
import org.junit.Test;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/6 22:27
 * @Description:
 */
public class RedisStreamMqClientTest {

    private MqClient mqClient = MqClientFactory.getMqClient("10.88.36.127", 6379, "redis");

    @Test
    public void test_subscribe_queue() {
        Consumer consumer = Consumer.builder()
                .queue("stm")
                .group("default")
                .count(2)
                .build();
        mqClient.subscribeQueue(consumer, new ExampleReadGroupHandler(), new ExampleExceptionHandler());
    }

    @Test
    public void test_send_msg(){
//        MqSendService mqSendService = MqSendServiceFactory.getMqSendService();
//
//        mqSendService = MqSendServiceFactory.getMqSendService();
//
//        mqSendService.sendSuccessMessageToTarget(
//                "out1",
//                "{\"evnet\":null}",
//                "extra",
//                "123456"
//        );

        MqClient mqClient = MqClientFactory.getMqClient("10.88.36.127", 6379, "redis");

        Message message = Message.builder()
                .queue("mq-master")
                .requestId("")
                .keysAndValues(new Object[]{"requestId","dfa"})
                .maxLength(100)
                .approximateTrimming(false)
                .build();

        String s = mqClient.sendMessage(message);

        System.out.println(s);
    }
}
