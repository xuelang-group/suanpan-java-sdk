package com.xuelang.mqstream.handler.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableEnv
@ComponentAutoConfig
public @interface EnableComponentConfig {
}
