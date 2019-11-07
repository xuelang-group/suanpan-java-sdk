package com.xuelang.mqstream;

import com.xuelang.mqstream.config.GlobalConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/5 13:59
 * @Description:
 */
@Slf4j
public class MqClientFactory {

    private MqClientFactory() {
    }

    public static MqClient getMqClient() {
        return getMqClient(GlobalConfig.mqRedisHost, GlobalConfig.mqRedisPort, GlobalConfig.mqType);
    }

    public static MqClient getMqClient(String host, Integer port, String mqType) {
        MqClient mqClient = new RedisStreamMqClient(host, port);
        if ("redis".equals(mqType)) {
            mqClient = new RedisStreamMqClient(host, port);
        }
        return mqClient;
    }
}
