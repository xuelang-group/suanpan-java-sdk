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
 * @author ellison
 * @date 2020/8/4 4:11 下午
 * @description: 自定义的消息类型都要继承该类
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
     * @description: 判断该消息是否需要异步处理
     * @Param: [mappingCache]
     * @return: java.lang.Boolean
     */
    public abstract Boolean isAsyncDealMessage(Map<BussinessListenerMapping, DefaultMessageRecvHandler.DealMsgInvokeObj> mappingCache);

    /**
     * @description: 消息处理
     * @param: [mappingCache]
     * @return: void
     */
    public abstract void dealMessageInvoke(Map<BussinessListenerMapping, DefaultMessageRecvHandler.DealMsgInvokeObj> mappingCache);

    /**
     * @description: 获取参数
     * @param: []
     * @return: java.lang.Object[]
     */
    public abstract Object[] getArgs();

    /** 消息处理
     * @description:
     * @param: [listenerMapping, dealMsgInvokeObj]
     * @return: void
     */
    void dealMessageInvoke(BussinessListenerMapping listenerMapping, DefaultMessageRecvHandler.DealMsgInvokeObj dealMsgInvokeObj) {

        final List<String> targets = new ArrayList<>();

        if (StringUtils.isNotBlank(listenerMapping.input())) {
            this.input = listenerMapping.input();
        }

        String defaultTarget = this.input.replace("in", "out");
        if (listenerMapping.targets() != null && listenerMapping.targets().length > 0) {
            targets.addAll(Arrays.asList(listenerMapping.targets()));
        } else {
            targets.add(defaultTarget);
        }

        try {
            Method method = dealMsgInvokeObj.getMethod();
            int count = method.getParameterCount();
            Object obj;
            if (count == 0) {
                obj = method.invoke(dealMsgInvokeObj.getObject());
            } else {
                obj = method.invoke(dealMsgInvokeObj.getObject(), getArgs());
            }
            if (listenerMapping.defaultSendResp()) {
                Object data = null;
                if (null != obj) {
                    try {
                        data = obj;
                    } catch (Exception e) {
                        data = obj;
                    }
                }
                MqSendServiceFactory.getMqSendService().sendSuccessMessageToTarget(
                        targets,
                        data,
                        this.extra,
                        this.requestId
                );
            }
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
