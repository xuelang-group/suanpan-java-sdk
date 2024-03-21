package com.xuelang.suanpan.annotation.validator;

import com.xuelang.suanpan.annotation.AsyncHandlerMapping;
import com.xuelang.suanpan.annotation.SyncHandlerMapping;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.entities.enums.NodeReceiveMsgType;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HandlerRuntimeValidator {

    public static void validateHandlerPortValues(Class<?> clazz, int inPortMaxIndex, int outPortMaxIndex) throws RuntimeException {
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

                if (Arrays.stream(asyncHandlerMapping.default_outport_index()).anyMatch(index -> (index > outPortMaxIndex || index < 0))) {
                    throw new RuntimeException(AsyncHandlerMapping.class.getSimpleName() + " illegal scope outport value set, cannot be negative value or bigger than outport index max value: " + outPortMaxIndex + ", Clazz: "
                            + clazz.getName() + ", Method: " + method.getName());
                }
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

                if (Arrays.stream(syncHandlerMapping.default_outport_index()).anyMatch(index -> (index > outPortMaxIndex || index <= 0))) {
                    throw new RuntimeException(SyncHandlerMapping.class.getSimpleName() + " illegal scope outport value set, cannot be negative value or bigger than outport index max value: " + outPortMaxIndex + ", Clazz: "
                            + clazz.getName() + ", Method: " + method.getName());
                }
            }
        });
    }
}
