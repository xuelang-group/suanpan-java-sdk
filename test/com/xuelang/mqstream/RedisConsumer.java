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
package com.xuelang.mqstream;

import com.xuelang.mqstream.api.Consumer;
import com.xuelang.mqstream.api.impl.ConsumerImpl;
import com.xuelang.mqstream.handler.ExceptionHandler;
import com.xuelang.mqstream.handler.XReadGroupHandler;
import com.xuelang.mqstream.options.StreamOption;
import com.xuelang.mqstream.response.XReadGroupResponse;
import org.junit.Test;

import java.util.Map;

/**
 * @author zizuo.zdh
 * @ClassName RedisConsumer
 * @Description TODO
 * @Date 2019/9/4 23:01
 * @Version 1.0
 **/
public class RedisConsumer {
    @Test
    public void test1(){
        Map<String, String> options =null;
        StreamOption streamOption=new StreamOption();
        streamOption.setRedisHost("10.88.36.120");
        streamOption.setRedisPort(6379);
        streamOption.setNodeGroup("default");
        streamOption.setRecvQueue("recvQueue");
 //      streamOption.buildByOptions(options);
        Consumer consumer=new ConsumerImpl(streamOption);
        consumer.subscribe(new XReadGroupHandler() {
            @Override
            public void handle(XReadGroupResponse response) {
                      System.out.println(response.toString());
            }
        }, new ExceptionHandler() {
            @Override
            public void handle(Exception e) {

            }
        });
    }
}
