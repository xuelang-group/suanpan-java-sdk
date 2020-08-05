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

    private static MqClient mqClient = null;

    private MqClientFactory() {
    }

    public static MqClient getMqClient() {
        return getMqClient(GlobalConfig.mqRedisHost, GlobalConfig.mqRedisPort, GlobalConfig.mqType);
    }

    public static MqClient getMqClient(String host, Integer port, String mqType) {
        if (null == mqClient) {
            mqClient = new RedisStreamMqClient(host, port);
        }
        return mqClient;
    }

    public static MqClient getMqClient(String host, Integer port, String password, String mqType) {
        if (null == mqClient) {
            mqClient = new RedisStreamMqClient(host, port, password);
        }
        return mqClient;
    }
}
