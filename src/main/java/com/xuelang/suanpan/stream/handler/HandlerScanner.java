package com.xuelang.suanpan.stream.handler;

import com.xuelang.suanpan.annotation.InflowMapping;
import com.xuelang.suanpan.annotation.StreamHandler;
import com.xuelang.suanpan.annotation.SyncInflowMapping;
import com.xuelang.suanpan.annotation.validator.HandlerRuntimeValidator;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.common.entities.io.OutPort;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

@Slf4j
public class HandlerScanner {

    private static final String CLASS_FILE_EXTENSION = ".class";

    public static void scan(HandlerRegistry registry) {
        Class<?> mainClass = getStartClass();
        String basePackage = getPackageName(mainClass);
        log.info("suanpan sdk begin scan main package: {} to create suanpan handlers", basePackage);
        List<Class<?>> classes = null;
        try {
            classes = getClasses(basePackage);
        } catch (Exception e) {
            log.error("scan handler error, " + e.getMessage(), e);
            throw new RuntimeException(e);
        }

        // 找到带有指定注解的类，并实例化
        classes.stream().parallel().forEach(clazz -> {
            Annotation annotation = clazz.getAnnotation(StreamHandler.class);
            if (annotation != null) {
                HandlerRuntimeValidator.validateHandlerPortValues(clazz);
                try {
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    Method[] methods = clazz.getDeclaredMethods();
                    List<Method> filteredMethods = Arrays.stream(methods).filter(method -> (method.isAnnotationPresent(InflowMapping.class)
                            || method.isAnnotationPresent(SyncInflowMapping.class))).collect(Collectors.toList());
                    for (Method method : filteredMethods) {
                        HandlerMethodEntry handlerMethodEntry = new HandlerMethodEntry();
                        handlerMethodEntry.setInstance(instance);
                        handlerMethodEntry.setMethod(method);
                        if (method.isAnnotationPresent(InflowMapping.class)) {
                            InflowMapping inflowMapping = method.getAnnotation(InflowMapping.class);
                            List<OutPort> defaultSpecifiedOutPorts = new ArrayList<>();
                            for (int number : inflowMapping.default_outport_numbers()) {
                                defaultSpecifiedOutPorts.add(ConstantConfiguration.getByOutPortNumber(number));
                            }
                            if (CollectionUtils.isEmpty(defaultSpecifiedOutPorts)){
                                defaultSpecifiedOutPorts = ConstantConfiguration.getOutPorts();
                            }
                            handlerMethodEntry.setSpecifiedDefaultOutPorts(defaultSpecifiedOutPorts);
                            registry.regist(ConstantConfiguration.getByInPortNumber(inflowMapping.inport_number()), handlerMethodEntry);
                            log.info("create suanpan handler, Clazz: {}, Method: {}", instance.getClass().getName(), method.getName());
                        } else {
                            SyncInflowMapping syncInflowMapping = method.getAnnotation(SyncInflowMapping.class);
                            List<OutPort> defaultSpecifiedOutPorts = new ArrayList<>();
                            for (int number : syncInflowMapping.default_outport_numbers()) {
                                defaultSpecifiedOutPorts.add(ConstantConfiguration.getByOutPortNumber(number));
                            }
                            if (CollectionUtils.isEmpty(defaultSpecifiedOutPorts)){
                                defaultSpecifiedOutPorts = ConstantConfiguration.getOutPorts();
                            }
                            handlerMethodEntry.setSpecifiedDefaultOutPorts(defaultSpecifiedOutPorts);
                            registry.regist(null, handlerMethodEntry);
                            log.info("create suanpan handler, Clazz: {}, Method: {}", instance.getClass().getName(), method.getName());
                        }
                    }
                } catch (Exception e) {
                    log.error("regist handler error", e);
                    throw new RuntimeException(e);
                }
            }
        });
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

    public static Class<?> getStartClass() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            // 获取 MANIFEST.MF 文件输入流
            Enumeration<URL> urls = classLoader.getResources("META-INF/MANIFEST.MF");
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try (InputStream inputStream = url.openStream()) {
                    Manifest manifest = new Manifest(inputStream);
                    String startClassName = manifest.getMainAttributes().getValue("Start-Class");
                    if (startClassName != null) {
                        return Class.forName(startClassName);
                    }
                } catch (ClassNotFoundException e) {
                    log.error("get start-class from META-INF/MANIFEST.MF error", e);
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException ex) {
            log.error("get start-class from META-INF/MANIFEST.MF error", ex);
        }

        return getMainClass();
    }

    private static String getPackageName(Class<?> clazz) {
        Package pkg = clazz.getPackage();
        if (pkg != null) {
            return pkg.getName();
        }
        return "";
    }
}
