package com.xuelang.mqstream;

import com.xuelang.mqstream.options.Consumer;
import com.xuelang.mqstream.options.Message;
import com.xuelang.mqstream.options.Queue;
import example.ExampleExceptionHandler;
import example.ExampleReadGroupHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MqStreamClientTest {

    private static MqClient mqClient;

    @BeforeClass
    public static void setUp() {
        mqClient = new RedisStreamMqClient("192.168.99.100", 6379);
    }

    @Test
    public void test_create_queue() {
        Queue queue = Queue.builder()
                .name("test_xw_queue")
                .group("test_xw_group")
                .consumeId("$")
                .build();
        mqClient.createQueue(queue, true);
    }

    @Test
    public void test_send_message() {
        Message message = Message.builder()
                .queue("xw_backend")
                .keysAndValues(Message.prepareKeysAndValues("node", "hello"))
                .build();
        String messageId = mqClient.sendMessage(message);
        System.out.println(messageId);
    }

    @Test
    public void test_send_multi_message() {
        int size = 5;
        for (int i = 0; i < size; i++) {
            Message message = Message.builder()
                    .queue("js_queue")
                    .keysAndValues(
                            Message.prepareKeysAndValues("java", "cool with redis", "count", i + "")
                    )
                    .build();
            String response = mqClient.sendMessage(message);
            System.out.println(i + " " + response);
        }
    }

    @Test
    public void test_subscribe_queue() {
        Consumer consumer = Consumer.builder()
                .queue("js_queue")
                .group("js_group")
                .count(1)
                .build();
        mqClient.subscribeQueue(consumer, new ExampleReadGroupHandler(), new ExampleExceptionHandler());
    }

    @AfterClass
    public static void cleanUp() {
        mqClient.destroy();
    }

}