package com.xuelang.suanpan.stream;

import com.xuelang.suanpan.common.entities.io.OutPort;
import com.xuelang.suanpan.stream.handler.response.PollingResponse;
import com.xuelang.suanpan.stream.message.Extra;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface IStream {
    /**
     * 主动发送流消息
     * @param data
     * @param context
     * @param validitySeconds
     * @return
     */
    String publish(Map<OutPort, Object> data, @Nullable String requestId, @Nullable Long validitySeconds, @Nullable Extra extra);

    /**
     * 主动轮询流消息
     *
     * @param timeout 超时时间
     * @param unit    超时时间单位
     * @return 轮询到的算盘handler请求
     */
    PollingResponse polling(long timeout, TimeUnit unit);
}
