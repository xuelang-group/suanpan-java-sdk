package com.xuelang.mqstream.handler;

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
    String input();

    /**
     * 事件
     *
     * @return
     */
    String event();

    /**
     * 出口
     *
     * @return
     */
    String[] targets();

    /**
     * 是否异步
     *
     * @return
     */
    boolean async() default true;
}


