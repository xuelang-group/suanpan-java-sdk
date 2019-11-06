package com.xuelang.mqstream.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.xuelang.mqstream.message.MqSendServiceFactory;
import com.xuelang.mqstream.response.XReadGroupResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/5 14:47
 * @Description: sdk提供默认的一种消息处理方式，可以自定义实现
 */
@Slf4j
public class DefaultMessageRecvHandler implements XReadGroupHandler {

    private static final List<String> inputs = new ArrayList<>();

    private static ExecutorService asyncExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 2);

    private static ExecutorService syncExecutorService = Executors.newFixedThreadPool(1);

    //预设20个输入
    static {
        for (int i = 1; i <= 20; i++) {
            inputs.add("in" + i);
        }
    }

    private Map<BussinessListenerMapping, DealMsg> mappingCache = Maps.newHashMap();

    public DefaultMessageRecvHandler(List<Object> instances) {

        if (null != instances && instances.size() > 0) {

            for (Object instance : instances) {
                Class c = instance.getClass();

                Method[] declaredMethods = c.getDeclaredMethods();

                for (Method declaredMethod : declaredMethods) {
                    boolean annotationPresent = declaredMethod.isAnnotationPresent(BussinessListenerMapping.class);
                    if (annotationPresent) {
                        BussinessListenerMapping methodAnno = declaredMethod.getAnnotation(BussinessListenerMapping.class);
                        DealMsg dealMsg = new DealMsg();
                        dealMsg.setMethod(declaredMethod);
                        dealMsg.setObject(instance);
                        dealMsg.setAsync(methodAnno.async());
                        mappingCache.put(methodAnno, dealMsg);
                    }
                }
            }
        }
    }

    @Override
    public void handle(XReadGroupResponse response) {
        log.info("receive: {}", response);

        MqEventDto eventDto = new MqEventDto();

        List<Map<String, String>> data = response.getData();

        for (String input : inputs) {
            for (Map<String, String> item : data) {
                if (item.containsKey(input)) {
                    String value = item.get(input);

                    JSONObject parseObject = JSON.parseObject(value);
                    String event = parseObject.getString("event");
                    JSONObject eventData = parseObject.getJSONObject("data");
                    eventDto.setEvent(event);
                    eventDto.setData(eventData);

                    eventDto.setInput(input);
                }

                if (item.containsKey("extra")) {
                    String extraValue = item.get("extra");
                    if (StringUtils.isNotBlank(extraValue)) {
                        eventDto.setExtra(extraValue);
                    }
                }

                if (item.containsKey("id")) {
                    String id = item.get("id");
                    eventDto.setRequestId(id);
                }
            }

            if (StringUtils.isNotBlank(eventDto.getInput())) {

                dispatchEvent(eventDto);

                break;
            }
        }
    }

    private void dispatchEvent(MqEventDto eventDto) {

        log.info("eventDto:{}", eventDto.toString());

        DealMsg dealMsg = null;
        BussinessListenerMapping listenerMapping = null;

        for (Map.Entry<BussinessListenerMapping, DealMsg> entry : mappingCache.entrySet()) {
            if ((StringUtils.isBlank(entry.getKey().input()) || entry.getKey().input().equals(eventDto.getInput())) && entry.getKey().event().equals(eventDto.getEvent())) {
                dealMsg = entry.getValue();
                listenerMapping = entry.getKey();
                break;
            }
        }

        if (null == dealMsg) {
            return;
        }

        final List<String> targets = new ArrayList<>();

        String defaultTarget = eventDto.getInput().replace("in", "out");
        if (listenerMapping.targets() != null && listenerMapping.targets().length > 0) {
            targets.addAll(Arrays.asList(listenerMapping.targets()));
        } else {
            targets.add(defaultTarget);
        }

        final DealMsg finalDealMsg = dealMsg;

        ExecutorService executorService = asyncExecutorService;

        //async 为 false 改用同步线程执行
        if (!dealMsg.async) {
            executorService = syncExecutorService;
        }

        executorService.execute(() -> {
                    try {
                        Method method = finalDealMsg.getMethod();
                        int count = method.getParameterCount();
                        Object obj;
                        if (count == 0) {
                            obj = method.invoke(finalDealMsg.getObject());
                        } else {
                            obj = method.invoke(finalDealMsg.getObject(), eventDto.getData());
                        }

                        MqSendServiceFactory.getMqSendService().sendSuccessMessageToTarget(
                                targets,
                                eventDto.getEvent(),
                                obj == null ? "" : JSON.toJSONString(obj),
                                eventDto.getExtra(),
                                eventDto.getRequestId()
                        );
                    } catch (Exception e) {
                        log.error("dispatchEvent {} failed", eventDto.getEvent(), e);
                        MqSendServiceFactory.getMqSendService().sendErrorMessageToTarget(
                                targets,
                                eventDto.getEvent(),
                                eventDto.getExtra(),
                                eventDto.getRequestId()
                        );
                    }
                }
        );
    }

    @Data
    class DealMsg {

        private Method method;

        private Object object;

        private Boolean async;
    }
}
