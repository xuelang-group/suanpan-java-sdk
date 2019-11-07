package com.xuelang.mqstream.handler;

import com.google.common.collect.Maps;
import com.xuelang.mqstream.handler.annotation.BussinessListenerMapping;
import com.xuelang.mqstream.message.arguments.BaseType;
import com.xuelang.mqstream.response.XReadGroupResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/5 14:47
 * @Description: sdk提供默认的一种消息处理方式，可以自定义实现
 */
@Slf4j
public class DefaultMessageRecvHandler implements XReadGroupHandler {

    private static ExecutorService asyncExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 2);

    private static ExecutorService syncExecutorService = Executors.newFixedThreadPool(1);

    private Map<BussinessListenerMapping, DealMsgInvokeObj> mappingCache = Maps.newHashMap();

    private Class messageDataTypeClass;

    public DefaultMessageRecvHandler(List<Object> businessListenerInstances, Class messageDataTypeClass) {

        this.messageDataTypeClass = messageDataTypeClass;

        if (null != businessListenerInstances && businessListenerInstances.size() > 0) {

            for (Object instance : businessListenerInstances) {
                Class c = instance.getClass();

                Method[] declaredMethods = c.getDeclaredMethods();

                for (Method declaredMethod : declaredMethods) {
                    boolean annotationPresent = declaredMethod.isAnnotationPresent(BussinessListenerMapping.class);
                    if (annotationPresent) {
                        BussinessListenerMapping methodAnno = declaredMethod.getAnnotation(BussinessListenerMapping.class);
                        DealMsgInvokeObj dealMsg = new DealMsgInvokeObj();
                        dealMsg.setMethod(declaredMethod);
                        dealMsg.setObject(instance);
                        mappingCache.put(methodAnno, dealMsg);
                    }
                }
            }
        }
    }

    @Override
    public void handle(XReadGroupResponse response) {
        log.info("receive: {}", response);

        List<Map<String, String>> messages = response.getMessages();

        Constructor declaredConstructor = null;

        try {
            declaredConstructor = messageDataTypeClass.getDeclaredConstructor(Map.class);
        } catch (NoSuchMethodException e) {
            log.error("没有找到此构造参数", e);
            return;
        }

        for (Map<String, String> message : messages) {

            BaseType baseType;

            try {
                baseType = (BaseType) declaredConstructor.newInstance(message);
            } catch (Exception e) {
                log.error("消息类型转换错误", e);
                continue;
            }

            dispatchEvent(baseType);
        }
    }

    private void dispatchEvent(BaseType baseType) {

        log.info("messageDataType:{}", baseType);

        Boolean asyncDealMessage = baseType.isAsyncDealMessage(mappingCache);

        ExecutorService executorService = asyncExecutorService;

        //async 为 false 改用同步线程执行
        if (!asyncDealMessage) {
            executorService = syncExecutorService;
        }

        executorService.execute(() -> {
            baseType.dealMessageInvoke(mappingCache);
        });
    }

    @Data
    public static class DealMsgInvokeObj {

        private Method method;

        private Object object;
    }
}
