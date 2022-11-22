package com.xuelang.mqstream.config;

import com.xuelang.mqstream.common.CommonUtil;
import com.xuelang.service.NodeSiblingService;
import com.xuelang.service.PortService;
import com.xuelang.service.logkit.EventLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ComponentAutoConfiguration {

    /**
     * 获取注册可用端口
     */
    @Bean
    public WebServerFactoryCustomizer webServerFactoryCustomizer() {
        if(CommonUtil.isWindows()){
            return new WebServerFactoryCustomizer<ConfigurableWebServerFactory>() {
                @Override
                public void customize(ConfigurableWebServerFactory factory) {
                    PortService registerPort = new PortService();

                    int realPort = registerPort.registerServicePortUntilSuccess(GlobalConfig.logicPort);
                    log.info("set Server Port {}", realPort);
                    factory.setPort(realPort);
                }
            };
        }
        return null;
    }

    /**
     * 实例化logKit
     */
    @Bean
    public EventLogger eventLogger() {
        return new EventLogger();
    }

    /***
     *服务发现service
     */
    @Bean
    public NodeSiblingService nodeSiblingService() {
        if (!CommonUtil.isWindows()) {
            return new NodeSiblingService();
        }
        return null;
    }
}
