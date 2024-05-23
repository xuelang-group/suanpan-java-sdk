package com.xuelang.suanpan.annotation.validator;

import com.xuelang.suanpan.annotation.InflowMapping;
import com.xuelang.suanpan.common.utils.ParameterUtil;
import com.xuelang.suanpan.stream.message.InflowMessage;
import com.xuelang.suanpan.stream.message.OutflowMessage;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerRuntimeValidator {

    private static volatile boolean existGlobalHandler = false;

    private static volatile Class<?> globalHandlerClass;

    private static volatile Method globalHandlerMethod;

    private static Map<Integer, Integer> InPortHandlerMap = new ConcurrentHashMap<>();

    public static void validateHandlerPortValues(Class<?> clazz, List<Method> filteredMethods) throws RuntimeException {
        int inportMaxIndex = ParameterUtil.getInportMaxIndex();
        if (inportMaxIndex <= 0) {
            throw new RuntimeException("the node has no inport, can not define a suanpan handler use " + InflowMapping.class.getSimpleName() + " handler, Clazz: "
                    + clazz.getName());
        }

        filteredMethods.stream().forEach(method -> {
            validate(clazz, method);
            InflowMapping inflowMapping = method.getAnnotation(InflowMapping.class);
            if (inflowMapping.portIndex() != -1 &&
                    (inflowMapping.portIndex() > inportMaxIndex || inflowMapping.portIndex() <= 0)) {
                throw new RuntimeException(InflowMapping.class.getSimpleName() + " illegal scope inport value set, " +
                        "cannot be negative value or bigger than port index max value: " + inportMaxIndex + ", Clazz: "
                        + clazz.getName() + ", Method: " + method.getName());
            }

            if (existGlobalHandler) {
                throw new RuntimeException(globalHandlerClass.getName() + "_" + globalHandlerMethod.getName() + " is global handler which listen all inport data, " +
                        "cannot register any other handler duplication! Caused by: " + clazz.getName() + ", Method: " + method.getName());
            }

            if (inflowMapping.portIndex() != -1) {
                if (InPortHandlerMap.containsKey(inflowMapping.portIndex())) {
                    throw new RuntimeException("handler method cannot has duplication port index " + " Caused by: "
                            + clazz.getName() + ", Method: " + method.getName());
                } else {
                    InPortHandlerMap.put(inflowMapping.portIndex(), inflowMapping.portIndex());
                }
            } else if (inflowMapping.portIndex() == -1 && !existGlobalHandler) {
                existGlobalHandler = true;
                globalHandlerClass = clazz;
                globalHandlerMethod = method;
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
