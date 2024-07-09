package com.xuelang.suanpan;

import com.xuelang.suanpan.client.ISpClient;
import com.xuelang.suanpan.client.SpClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

/**
 * 整体sdk的bootstrap类，用于初始化sdk
 */
@Slf4j
public class SpClientSpringBootStarter implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ISpClient spClient = SpClientFactory.create();
        spClient.stream();
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
        BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(ISpClient.class, () -> spClient).getBeanDefinition();
        beanFactory.registerBeanDefinition("spClient", beanDefinition);
    }

    @Override
    public int getOrder() {
        // 返回最高优先级
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
