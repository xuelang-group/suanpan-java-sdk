package com.xuelang.suanpan.annotation.validator;

import com.xuelang.suanpan.annotation.AsyncHandlerMapping;
import com.xuelang.suanpan.annotation.SuanpanHandler;
import com.xuelang.suanpan.annotation.SuanpanHandlerResponseBody;
import com.xuelang.suanpan.annotation.SyncHandlerMapping;
import com.xuelang.suanpan.stream.handler.HandlerRequest;
import com.xuelang.suanpan.stream.handler.HandlerResponse;
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
        TypeElement syncHandler = null;
        TypeElement asyncHandler = null;
        Map<TypeElement, List<Integer>> inPortIndexMapHandler = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(SuanpanHandler.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement handler = (TypeElement) element;
                List<? extends Element> enclosedElements = handler.getEnclosedElements();
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

                            if (asyncHandlerMapping.default_outport_index().length == 0 ) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "Method: " + method + " @AsyncHandlerMapping outport_index values cannot be null", element);
                            }

                            List<Integer> existIndexes = inPortIndexMapHandler.get(handler);
                            if (CollectionUtils.isEmpty(existIndexes)) {
                                existIndexes = new ArrayList<>(asyncHandlerMapping.inport_index());
                            } else {
                                existIndexes.add(asyncHandlerMapping.inport_index());
                            }

                            inPortIndexMapHandler.put(handler, existIndexes);
                            asyncHandler = handler;
                        }

                        if (syncHandlerMapping != null) {
                            if (syncHandler != null){
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "Method: " + method + " @SyncHandlerMapping function cannot be duplication", element);
                            }

                            if (hasDuplicates(syncHandlerMapping.default_outport_index())) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "Method: " + method + " @SyncHandlerMapping outport_index values cannot be duplication", element);
                            }

                            if (hasDuplicates(syncHandlerMapping.inport_index())) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "Method: " + method + " @SyncHandlerMapping inport_index values cannot be duplication", element);
                            }

                            if (syncHandlerMapping.inport_index().length == 0 ) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "Method: " + method + " @SyncHandlerMapping inport_index values cannot be null", element);
                            }

                            if (syncHandlerMapping.default_outport_index().length == 0 ) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "Method: " + method + " @SyncHandlerMapping outport_index values cannot be null", element);
                            }

                            List<Integer> existIndexes = inPortIndexMapHandler.get(handler);
                            if (CollectionUtils.isEmpty(existIndexes)) {
                                existIndexes = Arrays.stream(syncHandlerMapping.inport_index()).boxed().collect(Collectors.toList());
                            } else {
                                existIndexes.addAll(Arrays.stream(syncHandlerMapping.inport_index()).boxed().collect(Collectors.toList()));
                            }

                            inPortIndexMapHandler.put(handler, existIndexes);
                            syncHandler = handler;
                        }

                    }
                }

                // 校验是否同时存在同步和异步处理器
                if (syncHandler != null && asyncHandler != null) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "@AsyncHandlerMapping handler: " + asyncHandler + " and @SyncHandlerMapping handler: "
                                    + syncHandler + " cannot exist together", element);
                }

            }
        }

        String globalDuplication;
        if ((globalDuplication = getGlobalInPortDuplication(inPortIndexMapHandler)) != null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "handler Classes: " + globalDuplication + " cannot has duplication inport");
        }

        return true;
    }

    private static String getGlobalInPortDuplication(Map<TypeElement, List<Integer>> typesMap) {
        if (typesMap == null || typesMap.isEmpty()){
            return null;
        }

        StringBuilder builder = new StringBuilder();
        List<TypeElement> keys = typesMap.keySet().stream().collect(Collectors.toList());
        for (int i = 0; i< keys.size();i++){
            TypeElement type = keys.get(i);
            List<Integer> indexes = typesMap.get(type);
            for (int n = i+1; n< keys.size();n++){
                TypeElement item = keys.get(n);
                List<Integer> itemIndexes = typesMap.get(item);
                if (CollectionUtils.containsAny(indexes,itemIndexes)){
                    builder.append(type.asType().toString());
                    builder.append(" and ");
                    builder.append(item.asType().toString());
                    builder.append(";");
                }
            }
        }

        if (builder.length()>0){
            return builder.toString();
        }

        return null;
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
