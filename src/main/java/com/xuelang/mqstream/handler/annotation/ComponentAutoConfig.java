package com.xuelang.mqstream.handler.annotation;

import com.xuelang.mqstream.config.ComponentAutoConfiguration;
import com.xuelang.mqstream.config.EnvUtil;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;



@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({ ComponentAutoConfiguration.class})
public @interface ComponentAutoConfig {
}
