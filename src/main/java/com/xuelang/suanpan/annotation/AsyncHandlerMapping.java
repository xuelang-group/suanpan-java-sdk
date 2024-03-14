package com.xuelang.suanpan.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AsyncHandlerMapping {
    /**
     * 异步订阅的输入桩端口序号
     * @return 输入桩端口序号
     */
    int inport_index() default -1;

    /**
     * 处理结果发送的输出桩端口集合
     * @return 输出桩端口集合
     */
    int[] default_outport_index();
}