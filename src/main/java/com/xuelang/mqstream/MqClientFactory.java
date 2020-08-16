package com.xuelang.mqstream;

import com.xuelang.mqstream.config.GlobalConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ellison
 * @date 2020/8/6 12:43 上午
 */
@Slf4j
public class MqClientFactory {

    public static final String MQ_TYPE = "redis";

    private MqClientFactory() {
    }

    private volatile static MqClient mqClient;

    public static MqClient getMqClient() {
        return getMqClient(GlobalConfig.mqRedisHost, GlobalConfig.mqRedisPort, GlobalConfig.mqType);
    }

    public static MqClient getMqClient(String host, Integer port, String mqType) {
        if (null == mqClient) {
            synchronized (MqClientFactory.class) {
                if (null == mqClient) {
                    if (MQ_TYPE.equals(mqType)) {
                        mqClient = new RedisStreamMqClient(host, port);
                    }
                }
            }
        }
        return mqClient;
    }
}
