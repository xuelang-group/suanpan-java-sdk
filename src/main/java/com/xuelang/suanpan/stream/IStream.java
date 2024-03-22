package com.xuelang.suanpan.stream;

import com.xuelang.suanpan.stream.handler.HandlerRequest;
import com.xuelang.suanpan.stream.message.StreamContext;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

public interface IStream {
    /**
     * 主动发送流消息
     * @param streamContext 消息上下文
     * @return
     */
    String publish(StreamContext streamContext, @Nullable Long validitySeconds);

    /**
     * 主动轮询流消息
     * @param timeout 超时时间
     * @param unit 超时时间单位
     * @return
     */
    HandlerRequest polling(long timeout, TimeUnit unit);
}
