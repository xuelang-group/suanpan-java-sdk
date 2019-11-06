package com.xuelang.mqstream;

import com.xuelang.mqstream.handler.ExceptionHandler;
import com.xuelang.mqstream.handler.XReadGroupHandler;
import com.xuelang.mqstream.options.Consumer;
import com.xuelang.mqstream.options.Message;
import com.xuelang.mqstream.options.Queue;

public interface MqClient {

    void createQueue(Queue queue, boolean existedOk);

    String sendMessage(Message message);

    void subscribeQueue(Consumer consumer, XReadGroupHandler handler, ExceptionHandler exceptionHandler);

    void ackMessage(String queue, String group, String... messageIds);

    void destroy();
}
