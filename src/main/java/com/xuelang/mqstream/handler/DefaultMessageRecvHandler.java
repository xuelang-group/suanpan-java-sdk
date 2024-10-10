package com.xuelang.mqstream.handler;

import com.google.common.collect.Maps;
import com.xuelang.mqstream.handler.annotation.BussinessListenerMapping;
import com.xuelang.mqstream.message.arguments.AppRelations;
import com.xuelang.mqstream.message.arguments.BaseType;
import com.xuelang.mqstream.response.XReadGroupResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/5 14:47
 * @Description: sdk提供默认的一种消息处理方式
 */
@Slf4j
public class DefaultMessageRecvHandler implements XReadGroupHandler {

    private static ExecutorService asyncExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 2);
    private static ExecutorService syncExecutorService = Executors.newFixedThreadPool(1);

    private Map<BussinessListenerMapping, DealMsgInvokeObj> mappingCache = Maps.newHashMap();

    private Class messageDataTypeClass;

    public DefaultMessageRecvHandler(List<Object> businessListenerInstances, Class messageDataTypeClass) {

        this.messageDataTypeClass = messageDataTypeClass;

        if (CollectionUtils.isNotEmpty(businessListenerInstances)) {
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
            List<String> inputs = new ArrayList<>();

            for (Map.Entry<String, String> entry : message.entrySet()) {
                String key = entry.getKey();
                if (AppRelations.inputs.contains(key)) {
                    inputs.add(key);
                }
            }

            for (String input : inputs) {
                Map<String, String> formatMessage = new HashMap<>();
                formatMessage.putAll(message);
                Iterator<Map.Entry<String, String>> iterator = formatMessage.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> next = iterator.next();
                    String key = next.getKey();
                    if (AppRelations.inputs.contains(key) && !Objects.equals(input, key)) {
                        iterator.remove();
                    }
                }
                BaseType baseType;

                try {
                    baseType = (BaseType) declaredConstructor.newInstance(formatMessage);
                } catch (Exception e) {
                    log.error("消息类型转换错误", e);
                    continue;
                }

                dispatchEvent(baseType);
            }
        }
    }

    private void dispatchEvent(BaseType baseType) {

        Boolean asyncDealMessage = baseType.isAsyncDealMessage(mappingCache);

        if (asyncExecutorService.isTerminated()) {
            log.error("AsyncExecutorService is terminated, cannot execute task.");
            asyncExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 2);
        }
        ExecutorService executorService = asyncExecutorService;

        //async 为 false 改用同步线程执行
        if (!asyncDealMessage) {
            if (syncExecutorService.isTerminated()) {
                log.error("SyncExecutorService is terminated, cannot execute task.");
                syncExecutorService = Executors.newFixedThreadPool(1);
            }
            executorService = syncExecutorService;
        }

        executorService.execute(() -> {
            try {
                baseType.dealMessageInvoke(mappingCache);
            } catch (Exception e) {
                log.error("消息处理异常: {} - {}", baseType.getClass().getName(), e.getMessage(), e);
            }

        });
    }

    @Data
    public static class DealMsgInvokeObj {
        private Method method;
        private Object object;
    }
}
