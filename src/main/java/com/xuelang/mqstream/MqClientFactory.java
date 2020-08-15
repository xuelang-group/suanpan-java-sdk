package com.xuelang.mqstream;

import com.xuelang.mqstream.config.GlobalConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ellison
 * @date 2020/8/6 12:43 上午
 */
@Slf4j
public class MqClientFactory {

    private static final Map<String, MqClient> mqMap = new HashMap();

    public static synchronized MqClient getMqClient(String type) {
        if (mqMap.containsKey(type)) {
            return mqMap.get(type);
        }
        MqClient mqClient = new RedisStreamMqClient(GlobalConfig.mqRedisHost, GlobalConfig.mqRedisPort);
        mqMap.put(type, mqClient);
        return mqClient;
    }
}
