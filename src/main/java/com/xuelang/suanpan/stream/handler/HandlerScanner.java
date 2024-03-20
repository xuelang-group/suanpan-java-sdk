package com.xuelang.suanpan.stream.handler;

import com.xuelang.suanpan.annotation.AsyncHandlerMapping;
import com.xuelang.suanpan.annotation.SuanpanHandler;
import com.xuelang.suanpan.annotation.SyncHandlerMapping;
import com.xuelang.suanpan.annotation.validator.HandlerRuntimeValidator;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.node.io.InPort;
import com.xuelang.suanpan.node.io.OutPort;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class HandlerScanner {

    private static final String CLASS_FILE_EXTENSION = ".class";

    public static Map<InPort, MethodEntry> scan() {
        Map<InPort, MethodEntry> instances = new HashMap<>();
        Class<?> mainClass = getMainClass();
        String mainPackage = getPackageName(mainClass);
        log.info("suanpan sdk begin scan main package: {} to create suanpan handlers", mainPackage);
        List<Class<?>> classes = null;
        try {
            classes = getClasses(mainPackage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 找到带有指定注解的类，并实例化
        for (Class<?> clazz : classes) {
            Annotation annotation = clazz.getAnnotation(SuanpanHandler.class);
            if (annotation != null) {
                HandlerRuntimeValidator.validateHandlerPortValues(clazz, ConstantConfiguration.getMaxInPortIndex(), ConstantConfiguration.getMaxOutPortIndex());
                try {
                    Object instance = clazz.getDeclaredConstructor().newInstance();

                    Method[] methods = clazz.getDeclaredMethods();
                    List<Method> filteredMethods = Arrays.stream(methods).filter(method -> (method.isAnnotationPresent(AsyncHandlerMapping.class)
                            || method.isAnnotationPresent(SyncHandlerMapping.class))).collect(Collectors.toList());

                    for (Method method : filteredMethods) {
                        MethodEntry methodEntry = new MethodEntry();
                        methodEntry.setInstance(instance);
                        methodEntry.setMethod(method);
                        if (method.isAnnotationPresent(AsyncHandlerMapping.class)) {
                            AsyncHandlerMapping asyncHandlerMapping = method.getAnnotation(AsyncHandlerMapping.class);
                            methodEntry.setSync(false);
                            List<OutPort> outPorts = new ArrayList<>();
                            for (int index : asyncHandlerMapping.default_outport_index()) {
                                outPorts.add(ConstantConfiguration.getOutPortByIndex(index));
                            }
                            methodEntry.setOutPorts(outPorts);
                            instances.put(ConstantConfiguration.getInPortByIndex(asyncHandlerMapping.inport_index()), methodEntry);
                            log.info("create suanpan handler, Clazz: {}, Method: {}", instance.getClass().getName(), method.getName());
                        } else {
                            SyncHandlerMapping syncHandlerMapping = method.getAnnotation(SyncHandlerMapping.class);
                            methodEntry.setSync(true);
                            List<OutPort> outPorts = new ArrayList<>();
                            for (int index : syncHandlerMapping.default_outport_index()) {
                                outPorts.add(ConstantConfiguration.getOutPortByIndex(index));
                            }
                            methodEntry.setOutPorts(outPorts);
                            for (int index : syncHandlerMapping.inport_index()) {
                                instances.put(ConstantConfiguration.getInPortByIndex(index), methodEntry);
                            }

                            log.info("create suanpan handler, Clazz: {}, Method: {}", instance.getClass().getName(), method.getName());
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return instances;
    }

    private static List<Class<?>> getClasses(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        URL resource = classLoader.getResource(path);
        if (resource != null) {
            File directory = new File(resource.getFile());
            if (directory.exists()) {
                scanDirectory(directory, packageName, classes);
            }
        }
        return classes;
    }

    private static void scanDirectory(File directory, String packageName, List<Class<?>> classes) throws ClassNotFoundException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, packageName + "." + file.getName(), classes);
                } else if (file.getName().endsWith(CLASS_FILE_EXTENSION)) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - CLASS_FILE_EXTENSION.length());
                    classes.add(Class.forName(className));
                    log.info("suanpan sdk scanned clazz: {}", className);
                }
            }
        }
    }

    private static Class<?> getMainClass() {
        try {
            // 获取当前线程的栈轨迹
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String mainClassName = stackTrace[stackTrace.length - 1].getClassName();
            return Class.forName(mainClassName);
        } catch (ClassNotFoundException e) {
            log.error("get main class error", e);
        }
        throw new RuntimeException("Main class not found");
    }

    private static String getPackageName(Class<?> clazz) {
        Package pkg = clazz.getPackage();
        if (pkg != null) {
            return pkg.getName();
        }
        return "";
    }
}
