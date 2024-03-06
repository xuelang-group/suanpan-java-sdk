package com.xuelang.suanpan.annotation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

public class AnnotationValidator {
    public static void validateAnnotationDuplication(Class<?> clazz) throws RuntimeException {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(AsyncSubscriber.class) && method.isAnnotationPresent(SyncSubscriber.class)) {
                throw new RuntimeException("@SyncSubscriber and @AsyncSubscriber cannot be used together " +
                        "on the same method: " + method.getName());
            }
        }
    }

    public static void validateAnnotationValues(Class<?> clazz, int inPortMaxIndex, int outPortMaxIndex) throws RuntimeException {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(AsyncSubscriber.class)) {
                AsyncSubscriber asyncSubscriber = method.getAnnotation(AsyncSubscriber.class);
                if (asyncSubscriber.inport_index() < 1 || asyncSubscriber.inport_index() > inPortMaxIndex) {
                    throw new RuntimeException("illegal scope @AsyncSubscriber inport value set, class: "
                            + clazz.getName() + " method: " + method.getName());
                }

                if (asyncSubscriber.outport_index().length == 0) {
                    throw new RuntimeException("illegal scope @AsyncSubscriber outport value set, class: "
                            + clazz.getName() + " method: " + method.getName());
                }

                if (Arrays.stream(asyncSubscriber.outport_index()).anyMatch(index -> (index < 1 || index > outPortMaxIndex))) {
                    throw new RuntimeException("illegal scope @AsyncSubscriber outport value set, class: "
                            + clazz.getName() + " method: " + method.getName());
                }

                if (hasDuplicates(asyncSubscriber.outport_index())){
                    throw new RuntimeException("duplication @AsyncSubscriber outport value set, class: "
                            + clazz.getName() + " method: " + method.getName());
                }
            }

            if (method.isAnnotationPresent(SyncSubscriber.class)) {
                SyncSubscriber syncSubscriber = method.getAnnotation(SyncSubscriber.class);
                if (syncSubscriber.inport_index().length == 0) {
                    throw new RuntimeException("illegal scope @SyncSubscriber inport value set, class: "
                            + clazz.getName() + " method: " + method.getName());
                }

                if (Arrays.stream(syncSubscriber.inport_index()).anyMatch(index -> (index < 1 || index > inPortMaxIndex))) {
                    throw new RuntimeException("illegal scope @SyncSubscriber inport value set, class: "
                            + clazz.getName() + " method: " + method.getName());
                }

                if (syncSubscriber.outport_index().length == 0) {
                    throw new RuntimeException("illegal scope @SyncSubscriber outport value set, class: "
                            + clazz.getName() + " method: " + method.getName());
                }

                if (Arrays.stream(syncSubscriber.outport_index()).anyMatch(index -> (index < 1 || index > outPortMaxIndex))) {
                    throw new RuntimeException("illegal scope @SyncSubscriber outport value set, class: "
                            + clazz.getName() + " method: " + method.getName());
                }

                if (hasDuplicates(syncSubscriber.outport_index()) || hasDuplicates(syncSubscriber.inport_index())){
                    throw new RuntimeException("duplication @SyncSubscriber port value set, class: "
                            + clazz.getName() + " method: " + method.getName());
                }
            }
        }
    }

    public static boolean hasDuplicates(int[] array) {
        HashSet<Integer> set = new HashSet<>();
        for (int num : array) {
            if (!set.add(num)) {
                return true;
            }
        }
        return false;
    }
}
