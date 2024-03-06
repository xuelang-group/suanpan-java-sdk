package com.xuelang.suanpan.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SyncSubscriber {
    /**
     * 同步订阅的输入桩端口序号集合
     * @return 输入桩端口序号集合
     */
    int[] inport_index();

    /**
     * 处理结果发送的输出桩端口集合
     * @return 输出桩端口集合
     */
    int[] outport_index();
}
