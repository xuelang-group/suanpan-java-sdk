package com.xuelang.suanpan.stream;

import com.xuelang.suanpan.stream.handler.HandlerRequest;
import com.xuelang.suanpan.stream.message.StreamContext;

import java.util.concurrent.TimeUnit;

public interface IStream {
    /**
     * 发送消息
     * @param outPorts
     * @param data
     * @return
     */
    String publish(StreamContext streamContext);

    /**
     * 主动轮询输入端口的消息
     * @param timeout 超时时间
     * @param unit 超时时间单位
     * @return
     */
    HandlerRequest polling(long timeout, TimeUnit unit);
}
