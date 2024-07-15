package com.xuelang.suanpan.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InflowMapping {
    /**
     * 异步订阅的输入桩端口序号
     * @return 输入桩端口序号
     */
    int portIndex() default -Integer.MAX_VALUE;
}
