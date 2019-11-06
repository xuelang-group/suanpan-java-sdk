package com.xuelang.mqstream.message;

import com.xuelang.mqstream.config.GlobalConfig;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/5 13:59
 * @Description:
 */
public class MqSendServiceFactory {

    private MqSendServiceFactory() {
    }

    private volatile static MqSendService mqSendService;

    public static MqSendService getMqSendService() {
        if (null == mqSendService) {
            synchronized (MqSendServiceFactory.class) {
                if (null == mqSendService) {
                    if ("redis".equals(GlobalConfig.mqType)) {
                        mqSendService = new RedisMqSendService();
                    }
                }
            }
        }
        return mqSendService;
    }
}
