package com.xuelang.suanpan.annotation.validator;

import com.xuelang.suanpan.annotation.AsyncHandlerMapping;
import com.xuelang.suanpan.annotation.SyncHandlerMapping;

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
                AsyncHandlerMapping asyncHandlerMapping = method.getAnnotation(AsyncHandlerMapping.class);
                if (asyncHandlerMapping.inport_index() > inPortMaxIndex) {
                    throw new RuntimeException(AsyncHandlerMapping.class.getSimpleName()+" illegal scope inport value set, component max inport index is " + inPortMaxIndex + ", Clazz: "
                            + clazz.getName() + ", Method: " + method.getName());
                }

                if (Arrays.stream(asyncHandlerMapping.default_outport_index()).anyMatch(index -> (index > outPortMaxIndex))) {
                    throw new RuntimeException(AsyncHandlerMapping.class.getSimpleName()+" illegal scope outport value set, component max outport index is " + outPortMaxIndex + ", Clazz: "
                            + clazz.getName() + ", Method: " + method.getName());
                }
            }

            if (method.isAnnotationPresent(SyncHandlerMapping.class)) {
                SyncHandlerMapping syncHandlerMapping = method.getAnnotation(SyncHandlerMapping.class);
                if (Arrays.stream(syncHandlerMapping.inport_index()).anyMatch(index -> (index > inPortMaxIndex))) {
                    throw new RuntimeException(SyncHandlerMapping.class.getSimpleName()+" illegal scope inport value set, component max inport index is " + inPortMaxIndex + ", Clazz: "
                            + clazz.getName() + ", Method: " + method.getName());
                }

                if (Arrays.stream(syncHandlerMapping.default_outport_index()).anyMatch(index -> (index > outPortMaxIndex))) {
                    throw new RuntimeException(SyncHandlerMapping.class.getSimpleName()+" illegal scope outport value set, component max outport index is " + outPortMaxIndex + ", Clazz: "
                            + clazz.getName() + ", Method: " + method.getName());
                }
            }
        });
    }
}
