package com.xuelang.suanpan.stream;

import com.xuelang.suanpan.common.exception.StreamGlobalException;
import com.xuelang.suanpan.stream.handler.AbstractStreamHandler;
import com.xuelang.suanpan.stream.message.Context;
import com.xuelang.suanpan.stream.message.InflowMessage;
import com.xuelang.suanpan.stream.message.OutflowMessage;

import java.util.concurrent.TimeUnit;

public interface IStream {

    /**
     *
     * @param outflowMessage
     * @param context
     * @return
     */
    String publish(OutflowMessage outflowMessage, Context context) throws StreamGlobalException;


    /**
     * 主动轮询流消息
     *
     * @param timeoutMillis 超时时间
     * @return 轮询到的算盘handler请求
     */
    InflowMessage polling(long timeoutMillis);

    /**
     * 订阅输入端口数据
     * @param inPortNum
     * @param handler
     */
    void subscribe(Integer inPortNum, AbstractStreamHandler handler) throws StreamGlobalException;

    /**
     * 通用订阅
     * @param handler
     * @throws Exception
     */
    void subscribe(AbstractStreamHandler handler) throws StreamGlobalException;
}
