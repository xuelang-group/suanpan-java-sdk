package com.xuelang.suanpan.annotation.validator;

import com.xuelang.suanpan.annotation.AsyncHandlerMapping;
import com.xuelang.suanpan.annotation.SyncHandlerMapping;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.common.entities.enums.NodeReceiveMsgType;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class HandlerRuntimeValidator {

    public static void validateHandlerPortValues(Class<?> clazz) throws RuntimeException {
        int inPortMaxIndex = ConstantConfiguration.getMaxInPortIndex();
        int outPortMaxIndex = ConstantConfiguration.getMaxOutPortIndex();

        if (inPortMaxIndex <= 0) {
            throw new RuntimeException("the node has no inport, can not define a suanpan handler use " + AsyncHandlerMapping.class.getSimpleName() + " handler, Clazz: "
                    + clazz.getName());
        }

        Method[] methods = clazz.getDeclaredMethods();
        List<Method> filteredMethods = Arrays.stream(methods).filter(method -> (method.isAnnotationPresent(AsyncHandlerMapping.class)
                || method.isAnnotationPresent(SyncHandlerMapping.class))).collect(Collectors.toList());

        filteredMethods.stream().parallel().forEach(method -> {
            if (method.isAnnotationPresent(AsyncHandlerMapping.class)) {
                if (NodeReceiveMsgType.sync.equals(ConstantConfiguration.getReceiveMsgType())) {
                    throw new RuntimeException("Sync_Received node can not use " + AsyncHandlerMapping.class.getSimpleName() + " handler, Clazz: "
                            + clazz.getName() + ", Method: " + method.getName());
                }

                AsyncHandlerMapping asyncHandlerMapping = method.getAnnotation(AsyncHandlerMapping.class);
                if (asyncHandlerMapping.inport_index() > inPortMaxIndex || asyncHandlerMapping.inport_index() <= 0) {
                    throw new RuntimeException(AsyncHandlerMapping.class.getSimpleName() + " illegal scope inport value set, cannot be negative value or bigger than inport index max value: " + inPortMaxIndex + ", Clazz: "
                            + clazz.getName() + ", Method: " + method.getName());
                }

                checkOutPortValues(asyncHandlerMapping.default_outport_index(), outPortMaxIndex, clazz, method);
            }

            if (method.isAnnotationPresent(SyncHandlerMapping.class)) {
                if (NodeReceiveMsgType.async.equals(ConstantConfiguration.getReceiveMsgType())) {
                    throw new RuntimeException("Async_Received node can not use " + SyncHandlerMapping.class.getSimpleName() + " handler, Clazz: "
                            + clazz.getName() + ", Method: " + method.getName());
                }

                SyncHandlerMapping syncHandlerMapping = method.getAnnotation(SyncHandlerMapping.class);
                if (Arrays.stream(syncHandlerMapping.inport_index()).anyMatch(index -> (index > inPortMaxIndex || index <= 0))) {
                    throw new RuntimeException(SyncHandlerMapping.class.getSimpleName() + " illegal scope inport value set, cannot be negative value or bigger than inport index max value: " + inPortMaxIndex + ", Clazz: "
                            + clazz.getName() + ", Method: " + method.getName());
                }

                checkOutPortValues(syncHandlerMapping.default_outport_index(), outPortMaxIndex, clazz, method);
            }
        });
    }

    private static void checkOutPortValues(int[] defaultOutPortIndex, int outPortMaxIndex, Class<?> clazz, Method method) {
        if (outPortMaxIndex <= 0 && (defaultOutPortIndex != null && defaultOutPortIndex.length > 0)) {
            throw new RuntimeException("node has no outPort, cannot set " + AsyncHandlerMapping.class.getSimpleName() + " outport value , Clazz: "
                    + clazz.getName() + ", Method: " + method.getName());
        }

        if ((defaultOutPortIndex != null && defaultOutPortIndex.length > 0) && Arrays.stream(defaultOutPortIndex).anyMatch(index -> (index > outPortMaxIndex || index < 0))) {
            throw new RuntimeException(AsyncHandlerMapping.class.getSimpleName() + " illegal scope outport value set, cannot be negative value or bigger than outport index max value: " + outPortMaxIndex + ", Clazz: "
                    + clazz.getName() + ", Method: " + method.getName());
        }
    }
}
