package com.xuelang.suanpan.stream.handler;

import com.alibaba.fastjson2.JSON;
import com.xuelang.suanpan.common.entities.io.Inport;
import com.xuelang.suanpan.common.exception.*;
import com.xuelang.suanpan.stream.client.AbstractMqClient;
import com.xuelang.suanpan.stream.message.InflowMessage;
import com.xuelang.suanpan.stream.message.OutflowMessage;
import com.xuelang.suanpan.stream.message.MetaInflowMessage;
import com.xuelang.suanpan.stream.message.MetaOutflowMessage;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

@Slf4j
public class HandlerProxy {
    private final HandlerRegistry registry = HandlerRegistry.getInstance();

    private AbstractMqClient abstractMqClient;

    public HandlerProxy() {
        HandlerScanner.scan(registry);
    }


    public MetaOutflowMessage invoke(MetaInflowMessage metaInflowMessage) {
        log.debug("meta inflow message: {}", JSON.toJSONString(metaInflowMessage));
        if (metaInflowMessage == null) {
            log.warn("illegal stream message, inflow message is empty");
            return null;
        }

        Inport firstInport = metaInflowMessage.getInPortDataMap().keySet().stream().findFirst().get();
        HandlerMethodEntry handlerMethodEntry;
        if ((handlerMethodEntry = registry.get(firstInport)) == null) {
            // TODO: 2024/3/12 发送事件到平台
            log.warn("no such handler for {}", firstInport.getUuid());
        }

        OutflowMessage outflowMessage = null;
        try {
            InflowMessage inflowMessage = metaInflowMessage.covert();
            outflowMessage = (OutflowMessage) handlerMethodEntry.getMethod().invoke(handlerMethodEntry.getInstance(), inflowMessage);
        } catch (IllegalAccessException e) {
            log.warn("invocation handler method error", e);
        } catch (InvocationTargetException e) {
            log.warn("invocation handler method error", e);
        }

        if (outflowMessage == null) {
            return null;
        }

        MetaOutflowMessage metaOutflowMessage = new MetaOutflowMessage();
        metaOutflowMessage.setMetaContext(metaInflowMessage.getMetaContext());
        metaOutflowMessage.setOutPortDataMap(outflowMessage.getOutPortDataMap());
        metaOutflowMessage.refreshExt(outflowMessage.getExt());
        metaOutflowMessage.refreshExpire(outflowMessage.getValiditySeconds() * 1000);
        log.debug("meta outflow message:{}", JSON.toJSONString(metaOutflowMessage));
        return metaOutflowMessage;
    }
}
