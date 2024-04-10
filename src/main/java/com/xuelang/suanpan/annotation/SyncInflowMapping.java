package com.xuelang.suanpan.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SyncInflowMapping {
    /**
     * 处理结果发送的输出桩端口集合
     * @return 输出桩端口集合
     */
    int[] default_outport_numbers() default {};
}