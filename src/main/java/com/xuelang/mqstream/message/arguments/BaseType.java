package com.xuelang.mqstream.message.arguments;

import com.alibaba.fastjson.JSON;
import com.xuelang.mqstream.handler.DefaultMessageRecvHandler;
import com.xuelang.mqstream.handler.annotation.BussinessListenerMapping;
import com.xuelang.mqstream.message.MqSendServiceFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/7 09:00
 * @Description:自定义的消息类型都要继承该类
 */
@Data
@Slf4j
public abstract class BaseType {

    private String input;

    private String extra;

    private String requestId;

    private Map<String, String> message;

    BaseType(Map<String, String> message) {
        this.message = message;

        for (Map.Entry<String, String> entry : message.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (AppRelations.inputs.contains(key)) {
                this.input = key;
            }

            if ("extra".equals(key) && StringUtils.isNotBlank(value)) {
                this.extra = value;
            }

            if ("id".equals(key)) {
                this.requestId = value;
            }
        }
    }

    /**
     * 判断该消息是否需要异步处理
     *
     * @param mappingCache
     * @return
     */
    public abstract Boolean isAsyncDealMessage(Map<BussinessListenerMapping, DefaultMessageRecvHandler.DealMsgInvokeObj> mappingCache);

    /**
     * 消息处理
     *
     * @param mappingCache
     */
    public abstract void dealMessageInvoke(Map<BussinessListenerMapping, DefaultMessageRecvHandler.DealMsgInvokeObj> mappingCache);

    /**
     * 消息处理
     *
     * @param listenerMapping
     * @param dealMsgInvokeObj
     */
    void dealMessageInvoke(BussinessListenerMapping listenerMapping, DefaultMessageRecvHandler.DealMsgInvokeObj dealMsgInvokeObj) {

        final List<String> targets = new ArrayList<>();

        String defaultTarget = this.input.replace("in", "out");
        if (listenerMapping.targets() != null && listenerMapping.targets().length > 0) {
            targets.addAll(Arrays.asList(listenerMapping.targets()));
        } else {
            targets.add(defaultTarget);
        }

        if (null == listenerMapping || null == dealMsgInvokeObj) {
            String msg = "没有找到处理方法，消息将丢失";
            log.error(msg);
            MqSendServiceFactory.getMqSendService().sendErrorMessageToTarget(
                    targets,
                    msg,
                    this.extra,
                    this.requestId
            );
            return;
        }

        try {
            Method method = dealMsgInvokeObj.getMethod();
            int count = method.getParameterCount();
            Object obj;
            if (count == 0) {
                obj = method.invoke(dealMsgInvokeObj.getObject());
            } else {
                obj = method.invoke(dealMsgInvokeObj.getObject(), this.message);
            }

            String data = "";
            if (null != obj) {
                try {
                    data = JSON.toJSONString(obj);
                } catch (Exception e) {
                    data = obj.toString();
                }
            }

            MqSendServiceFactory.getMqSendService().sendSuccessMessageToTarget(
                    targets,
                    data,
                    this.extra,
                    this.requestId
            );
        } catch (Exception e) {
            log.error("消息处理失败", e);
            MqSendServiceFactory.getMqSendService().sendErrorMessageToTarget(
                    targets,
                    "消息处理失败" + e.getMessage(),
                    this.extra,
                    this.requestId
            );
        }
    }
}
