package com.xuelang.mqstream.handler.annotation;

import java.lang.annotation.*;
/**
 * @author ellison
 * @date 2020/8/4 1:45 下午
 * @description: TODO
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BussinessListenerMapping {
    /**
     * 入口
     */
    String input() default "in1";

    /**
     * 出口
     */
    String[] targets() default {"out1"};

    /**
     * 是否异步
     */
    boolean async() default true;

    /**
     * 是否发送响应消息
     */
    boolean defaultSendResp() default true;

    /**
     * 基于事件类型的消息
     */
    String event() default "";
}


