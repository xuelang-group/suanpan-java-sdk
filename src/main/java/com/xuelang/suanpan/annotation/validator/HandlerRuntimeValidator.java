package com.xuelang.suanpan.annotation.validator;

import com.xuelang.suanpan.annotation.AsyncHandlerMapping;
import com.xuelang.suanpan.annotation.SuanpanHandlerResponseBody;
import com.xuelang.suanpan.annotation.SyncHandlerMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class HandlerRuntimeValidator {

    public static void validateHandlerPortValues(Class<?> clazz, int inPortMaxIndex, int outPortMaxIndex) throws RuntimeException {
        Method[] methods = clazz.getDeclaredMethods();
        List<Method> filteredMethods = Arrays.stream(methods).filter(method -> (method.isAnnotationPresent(AsyncHandlerMapping.class)
                        || method.isAnnotationPresent(SyncHandlerMapping.class))).collect(Collectors.toList());

        for (Method method : filteredMethods) {
            if (method.isAnnotationPresent(AsyncHandlerMapping.class)) {
                AsyncHandlerMapping asyncHandlerMapping = method.getAnnotation(AsyncHandlerMapping.class);
                if (asyncHandlerMapping.inport_index() > inPortMaxIndex) {
                    throw new RuntimeException("illegal scope @AsyncSubscriber inport value set, class: "
                            + clazz.getName() + " method: " + method.getName());
                }

                if (Arrays.stream(asyncHandlerMapping.default_outport_index()).anyMatch(index -> (index > outPortMaxIndex))) {
                    throw new RuntimeException("illegal scope @AsyncSubscriber outport value set, class: "
                            + clazz.getName() + " method: " + method.getName());
                }
            }

            if (method.isAnnotationPresent(SyncHandlerMapping.class)) {
                SyncHandlerMapping syncHandlerMapping = method.getAnnotation(SyncHandlerMapping.class);
                if (Arrays.stream(syncHandlerMapping.inport_index()).anyMatch(index -> (index > inPortMaxIndex))) {
                    throw new RuntimeException("illegal scope @SyncSubscriber inport value set, class: "
                            + clazz.getName() + " method: " + method.getName());
                }

                if (Arrays.stream(syncHandlerMapping.default_outport_index()).anyMatch(index -> (index > outPortMaxIndex))) {
                    throw new RuntimeException("illegal scope @SyncSubscriber outport value set, class: "
                            + clazz.getName() + " method: " + method.getName());
                }
            }
        }
    }
}
