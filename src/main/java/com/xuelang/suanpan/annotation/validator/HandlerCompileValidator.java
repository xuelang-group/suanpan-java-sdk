package com.xuelang.suanpan.annotation.validator;

import com.xuelang.suanpan.annotation.InflowMapping;
import com.xuelang.suanpan.annotation.StreamHandler;
import com.xuelang.suanpan.annotation.SyncInflowMapping;
import com.xuelang.suanpan.stream.message.InflowMessage;
import com.xuelang.suanpan.stream.message.OutflowMessage;
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

@SupportedAnnotationTypes("com.xuelang.suanpan.annotation.StreamHandler")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class HandlerCompileValidator extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        TypeElement syncHandler = null;
        TypeElement asyncHandler = null;
        Map<TypeElement, List<Integer>> asyncHandlerPortIndex = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(StreamHandler.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement handler = (TypeElement) element;
                List<? extends Element> enclosedElements = handler.getEnclosedElements();
                for (Element enclosedElement : enclosedElements) {
                    if (enclosedElement.getKind() == ElementKind.METHOD) {
                        // 获取方法
                        ExecutableElement method = (ExecutableElement) enclosedElement;

                        // 获取方法注解
                        InflowMapping inflowMapping = method.getAnnotation(InflowMapping.class);
                        SyncInflowMapping syncInflowMapping = method.getAnnotation(SyncInflowMapping.class);

                        // 校验方法注解合法性
                        if (inflowMapping != null && syncInflowMapping != null) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                    "@" + InflowMapping.class.getSimpleName() + " and @" + SyncInflowMapping.class.getSimpleName() + " cannot used together on Method: " + method, element);
                        }


                        // 校验返回值合法性
                        String returnTypeClassName = method.getReturnType().toString();
                        if (!returnTypeClassName.equals(void.class.getName()) && !returnTypeClassName.equals(OutflowMessage.class.getName())) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Method: " + method
                                    + " return type must be set " + OutflowMessage.class.getName() + " or " + void.class.getName(), element);
                        }

                        // 校验参数合法性
                        List<?> params = method.getParameters();
                        if (CollectionUtils.isEmpty(params) || params.size() > 1) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                    "Method: " + method + " must has just one parameter ", element);
                        } else if (!((VariableElement) params.get(0)).asType().toString().equals(InflowMessage.class.getName())) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                    "Method: " + method + " parameter must be set " + InflowMessage.class.getName(), element);
                        } else if (!((VariableElement) params.get(0)).asType().toString().equals(InflowMessage.class.getName())) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                    "Method: " + method + " parameter must be set " + InflowMessage.class.getName(), element);
                        }

                        if (inflowMapping != null) {
                            if (inflowMapping.portIndex() <= 0 && inflowMapping.portIndex() != -1) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "Method: " + method + " @" + InflowMapping.class.getSimpleName() + " portIndex value cannot be negative", element);
                            } else if (inflowMapping.portIndex() == -1 && asyncHandler != null) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "Already has a AsyncHandler which not specified portIndex to listen all " +
                                                "inport data, so AsyncHandler cannot be duplication! Method: " + method, element);
                            }

                            if (inflowMapping.portIndex() != -1) {
                                List<Integer> existInportIndexes = asyncHandlerPortIndex.get(handler);
                                if (CollectionUtils.isEmpty(existInportIndexes)) {
                                    existInportIndexes = new ArrayList<>(inflowMapping.portIndex());
                                }
                                existInportIndexes.add(inflowMapping.portIndex());
                                asyncHandlerPortIndex.put(handler, existInportIndexes);
                            }

                            asyncHandler = handler;
                        }

                        if (syncInflowMapping != null) {
                            if (syncHandler != null) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "Method: " + method + " @" + SyncInflowMapping.class.getSimpleName() + "  function cannot be duplication", element);
                            }

                            syncHandler = handler;
                        }

                    }
                }

                // 校验是否同时存在同步和异步处理器
                if (syncHandler != null && asyncHandler != null) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "@" + InflowMapping.class.getSimpleName() + " handler: " + asyncHandler + " and @" + SyncInflowMapping.class.getSimpleName() + "  handler: "
                                    + syncHandler + " cannot exist together", element);
                }

            }
        }

        String globalDuplication;
        if ((globalDuplication = getGlobalInPortDuplication(asyncHandlerPortIndex)) != null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "handler Classes: " + globalDuplication + " cannot has duplication inport");
        }

        return true;
    }

    private static String getGlobalInPortDuplication(Map<TypeElement, List<Integer>> typesMap) {
        if (typesMap == null || typesMap.isEmpty()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        List<TypeElement> keys = typesMap.keySet().stream().collect(Collectors.toList());
        for (int i = 0; i < keys.size(); i++) {
            TypeElement type = keys.get(i);
            List<Integer> indexes = typesMap.get(type);
            for (int n = i + 1; n < keys.size(); n++) {
                TypeElement item = keys.get(n);
                List<Integer> itemIndexes = typesMap.get(item);
                if (CollectionUtils.containsAny(indexes, itemIndexes)) {
                    builder.append(type.asType().toString());
                    builder.append(" and ");
                    builder.append(item.asType().toString());
                    builder.append(";");
                }
            }
        }

        if (builder.length() > 0) {
            return builder.toString();
        }

        return null;
    }

    private static boolean hasDuplicateOrNegative(int[] array) {
        if (array == null || array.length == 0) {
            return false;
        }

        HashSet<Integer> set = new HashSet<>();
        for (int num : array) {
            if (!set.add(num)) {
                return true;
            }

            if (num <= 0) {
                return true;
            }
        }
        return false;
    }
}
