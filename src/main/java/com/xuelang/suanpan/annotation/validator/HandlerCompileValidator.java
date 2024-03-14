package com.xuelang.suanpan.annotation.validator;

import com.xuelang.suanpan.annotation.AsyncHandlerMapping;
import com.xuelang.suanpan.annotation.SuanpanHandler;
import com.xuelang.suanpan.annotation.SuanpanHandlerResponseBody;
import com.xuelang.suanpan.annotation.SyncHandlerMapping;
import com.xuelang.suanpan.domain.handler.HandlerRequest;
import com.xuelang.suanpan.domain.handler.HandlerResponse;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("com.xuelang.suanpan.annotation.SuanpanHandler")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class HandlerCompileValidator extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean hasSync = false;
        boolean hasAsync = false;
        List<Integer> totalInPortIndex = new ArrayList<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(SuanpanHandler.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
                for (Element enclosedElement : enclosedElements) {
                    if (enclosedElement.getKind() == ElementKind.METHOD) {
                        // 获取方法
                        ExecutableElement method = (ExecutableElement) enclosedElement;

                        // 获取方法注解
                        AsyncHandlerMapping asyncHandlerMapping = method.getAnnotation(AsyncHandlerMapping.class);
                        SyncHandlerMapping syncHandlerMapping = method.getAnnotation(SyncHandlerMapping.class);
                        SuanpanHandlerResponseBody suanpanHandlerResponseBody = method.getAnnotation(SuanpanHandlerResponseBody.class);

                        // 校验方法注解合法性
                        if (asyncHandlerMapping == null && syncHandlerMapping == null) {
                            if (suanpanHandlerResponseBody != null) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "@SuanpanHandlerResponseBody cannot used on common Method: " + method, element);
                            }

                            continue;
                        } else if (asyncHandlerMapping != null && syncHandlerMapping != null) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                    "@AsyncHandlerMapping and @SyncHandlerMapping cannot used together on Method: " + method, element);
                        }


                        // 校验返回值合法性
                        String returnTypeClassName = method.getReturnType().toString();
                        if (!returnTypeClassName.equals(void.class.getName()) && !returnTypeClassName.equals(HandlerResponse.class.getName())) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Method: " + method
                                    + " return type must be set " + HandlerResponse.class.getName() + " or " + void.class.getName(), element);
                        } else if (returnTypeClassName.equals(HandlerResponse.class.getName()) && suanpanHandlerResponseBody == null) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Method: " + method
                                    + " return type " + HandlerResponse.class.getName() + " must use @SuanpanHandlerResponseBody", element);
                        }

                        // 校验参数合法性
                        List<?> params = method.getParameters();
                        if (CollectionUtils.isEmpty(params) || params.size() > 1) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                    "Method: " + method + " must has just one parameter ", element);
                        } else if (!((VariableElement) params.get(0)).asType().toString().equals(HandlerRequest.class.getName())) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                    "Method: " + method + " parameter must be set " + HandlerRequest.class.getName(), element);
                        }


                        if (asyncHandlerMapping != null) {
                            if (hasDuplicates(asyncHandlerMapping.default_outport_index())) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "Method: " + method + " @AsyncHandlerMapping outport_index values cannot be duplication", element);
                            }

                            if (asyncHandlerMapping.default_outport_index().length == 0 || hasNonPositiveValue(asyncHandlerMapping.default_outport_index())) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "Method: " + method + " @AsyncHandlerMapping outport_index values must be set positive integer", element);
                            }

                            if (hasNonPositiveValue(new int[]{asyncHandlerMapping.inport_index()})) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "Method: " + method + " @AsyncHandlerMapping inport_index must be set positive integer", element);
                            }


                            totalInPortIndex.add(asyncHandlerMapping.inport_index());
                            hasAsync = true;
                        }

                        if (syncHandlerMapping != null) {
                            if (hasDuplicates(syncHandlerMapping.default_outport_index())) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "Method: " + method + " @SyncHandlerMapping outport_index values cannot be duplication", element);
                            }

                            if (hasDuplicates(syncHandlerMapping.inport_index())) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "Method: " + method + " @SyncHandlerMapping inport_index values cannot be duplication", element);
                            }

                            if (syncHandlerMapping.inport_index().length == 0 || hasNonPositiveValue(syncHandlerMapping.inport_index())) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "Method: " + method + " @SyncHandlerMapping inport_index values must be set positive integer", element);
                            }

                            if (syncHandlerMapping.default_outport_index().length == 0 || hasNonPositiveValue(syncHandlerMapping.default_outport_index())) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "Method: " + method + " @SyncHandlerMapping outport_index values must be set positive integer", element);
                            }

                            totalInPortIndex.addAll(Arrays.stream(syncHandlerMapping.inport_index()).boxed().collect(Collectors.toList()));
                            hasSync = true;
                        }

                    }
                }

                // 校验是否同时存在同步和异步处理器
                if (hasAsync && hasSync) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "@AsyncHandlerMapping and @SyncHandlerMapping cannot exist together on handler Class: "
                                    + typeElement, element);
                }

                if (hasDuplicates(totalInPortIndex.stream().mapToInt(Integer::intValue).toArray())) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "handler Class: " + typeElement + " cannot has duplication inport", element);
                }
            }
        }

        return true;
    }

    private static boolean hasNonPositiveValue(int[] array) {
        for (int num : array) {
            if (num <= 0) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasDuplicates(int[] array) {
        HashSet<Integer> set = new HashSet<>();
        for (int num : array) {
            if (!set.add(num)) {
                return true;
            }
        }
        return false;
    }
}
