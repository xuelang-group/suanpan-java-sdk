package com.xuelang.mqstream;

import com.xuelang.mqstream.config.GlobalConfig;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/5 13:59
 * @Description:
 */
public class MqClientFactory {

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
                    if ("redis".equals(mqType)) {
                        mqClient = new RedisStreamMqClient(host, port);
                    }
                }
            }
        }
        return mqClient;
    }
}
