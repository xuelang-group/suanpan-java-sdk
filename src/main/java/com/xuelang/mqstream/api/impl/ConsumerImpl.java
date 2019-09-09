/*

 * Copyright (C) 2016 - 2019
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xuelang.mqstream.api.impl;

import com.xuelang.mqstream.MqClient;
import com.xuelang.mqstream.api.Consumer;
import com.xuelang.mqstream.handler.ExceptionHandler;
import com.xuelang.mqstream.handler.XReadGroupHandler;
import com.xuelang.mqstream.options.StreamOption;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zizuo.zdh
 * @ClassName ConsumerImpl
 * @Description TODO
 * @Date 2019/9/4 22:12
 * @Version 1.0
 **/
public class ConsumerImpl implements Consumer {
    private StreamOption streamOption;
    public ConsumerImpl(StreamOption streamOption){
        this.streamOption=streamOption;
    }
    @Override
    public void subscribe(XReadGroupHandler messageRecvHandler, ExceptionHandler exceptionHandler) {
        MqClient mqClient = streamOption.buildRedisClient();
        com.xuelang.mqstream.options.Consumer consumer = com.xuelang.mqstream.options.Consumer.builder()
                .queue(streamOption.getRecvQueue())
                .group(streamOption.getNodeGroup())
                .name(streamOption.getNodeId())
                .delay(streamOption.getRecvQueueDelay())
                .build();
        mqClient.subscribeQueue(consumer,messageRecvHandler,exceptionHandler);
    }
}
