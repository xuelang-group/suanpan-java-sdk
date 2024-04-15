package com.xuelang.suanpan.annotation.validator;

import com.xuelang.suanpan.annotation.InflowMapping;
import com.xuelang.suanpan.annotation.SyncInflowMapping;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.common.entities.enums.NodeReceiveMsgType;
import com.xuelang.suanpan.stream.message.InflowMessage;
import com.xuelang.suanpan.stream.message.OutflowMessage;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class HandlerRuntimeValidator {

    private static boolean existSyncHandler = false;

    private static Map<Integer, Integer> globalAsyncInPortHandler = new ConcurrentHashMap<>();

    public static void validateHandlerPortValues(Class<?> clazz) throws RuntimeException {
        int inportMaxIndex = ConstantConfiguration.getInportMaxIndex();

        if (inportMaxIndex <= 0) {
            throw new RuntimeException("the node has no inport, can not define a suanpan handler use " + InflowMapping.class.getSimpleName() + " handler, Clazz: "
                    + clazz.getName());
        }

        Method[] methods = clazz.getDeclaredMethods();
        List<Method> filteredMethods = Arrays.stream(methods).filter(method -> (method.isAnnotationPresent(InflowMapping.class)
                || method.isAnnotationPresent(SyncInflowMapping.class))).collect(Collectors.toList());

        filteredMethods.stream().forEach(method -> {
            if (method.isAnnotationPresent(InflowMapping.class)) {
                if (NodeReceiveMsgType.sync.equals(ConstantConfiguration.getReceiveMsgType())) {
                    throw new RuntimeException("Sync_Received node can not use " + InflowMapping.class.getSimpleName() + " handler, Clazz: "
                            + clazz.getName() + ", Method: " + method.getName());
                }

                validate(clazz, method);
                InflowMapping inflowMapping = method.getAnnotation(InflowMapping.class);
                if (inflowMapping.portIndex() != -1 &&
                        (inflowMapping.portIndex() > inportMaxIndex || inflowMapping.portIndex() <= 0)) {
                    throw new RuntimeException(InflowMapping.class.getSimpleName() + " illegal scope inport value set, " +
                            "cannot be negative value or bigger than port index max value: " + inportMaxIndex + ", Clazz: "
                            + clazz.getName() + ", Method: " + method.getName());
                }

                if (inflowMapping.portIndex() != -1) {
                    if (globalAsyncInPortHandler.containsKey(inflowMapping.portIndex())) {
                        throw new RuntimeException("async handler method cannot has duplication port index " + InflowMapping.class.getSimpleName() + " handler, Clazz: "
                                + clazz.getName() + ", Method: " + method.getName());
                    } else {
                        globalAsyncInPortHandler.put(inflowMapping.portIndex(), inflowMapping.portIndex());
                    }
                }
            }

            if (method.isAnnotationPresent(SyncInflowMapping.class)) {
                if (NodeReceiveMsgType.async.equals(ConstantConfiguration.getReceiveMsgType())) {
                    throw new RuntimeException("Async_Received node can not use " + SyncInflowMapping.class.getSimpleName() + " handler, Clazz: "
                            + clazz.getName() + ", Method: " + method.getName());
                }

                if (existSyncHandler) {
                    throw new RuntimeException("SyncHandler cannot be duplication " + SyncInflowMapping.class.getSimpleName() + " handler, Clazz: "
                            + clazz.getName() + ", Method: " + method.getName());
                }

                validate(clazz, method);
                existSyncHandler = true;
            }
        });
    }


    private static void validate(Class<?> clazz, Method method) {
        String returnTypeClassName = method.getReturnType().getName();
        if (!returnTypeClassName.equals(void.class.getName()) && !returnTypeClassName.equals(OutflowMessage.class.getName())) {
            throw new RuntimeException("Handler: " + clazz.getName() + " Method: " + method.getName() + " return type must be set " + OutflowMessage.class.getName() + " or " + void.class.getName());
        }

        // 校验参数合法性
        List<?> params = Arrays.asList(method.getParameterTypes());
        if (CollectionUtils.isEmpty(params) || params.size() > 1) {
            throw new RuntimeException("Handler: " + clazz.getName() + " Method: " + method.getName() + " just has only one parameter");
        } else if (!((Class) params.get(0)).getName().equals(InflowMessage.class.getName())) {
            throw new RuntimeException("Handler: " + clazz.getName() + " Method: " + method.getName() + " parameter must be set " + InflowMessage.class.getName());
        } else if (!((Class) params.get(0)).getName().equals(InflowMessage.class.getName())) {
            throw new RuntimeException("Handler: " + clazz.getName() + " Method: " + method.getName() + " parameter must be set " + InflowMessage.class.getName());
        }
    }
}
