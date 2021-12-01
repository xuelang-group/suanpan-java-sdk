package com.xuelang.service;

import com.xuelang.mqstream.config.GlobalConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

@Slf4j
public class ComponentRunCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        PortService registerPort=new PortService();

        int realPort=registerPort.registerServicePortUntilSuccess(GlobalConfig.logicPort);
        log.info("set Server Port {}",realPort);
        factory.setPort(realPort);
    }
}