package com.xuelang.mqstream.handler.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BussinessListenerMapping {

    /**
     * 入口
     *
     * @return
     */
    String input() default "";

    /**
     * 出口
     *
     * @return
     */
    String[] targets() default {};

    /**
     * 是否异步
     *
     * @return
     */
    boolean async() default true;

    /**
     * 基于事件类型的消息
     * @return
     */
    String event() default "";
}


