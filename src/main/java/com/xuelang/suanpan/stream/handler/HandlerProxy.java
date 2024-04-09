package com.xuelang.suanpan.stream.handler;

import com.alibaba.fastjson2.JSON;
import com.xuelang.suanpan.common.entities.io.InPort;
import com.xuelang.suanpan.common.exception.*;
import com.xuelang.suanpan.stream.message.InflowMessage;
import com.xuelang.suanpan.stream.message.OutflowMessage;
import com.xuelang.suanpan.stream.message.MetaInflowMessage;
import com.xuelang.suanpan.stream.message.MetaOutflowMessage;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

@Slf4j
public class HandlerProxy {
    private final HandlerRegistry registry = HandlerRegistry.getInstance();

    public HandlerProxy() {
        HandlerScanner.scan(registry);
    }


    public MetaOutflowMessage invoke(MetaInflowMessage metaInflowMessage) throws StreamGlobalException{
        log.debug("meta inflow message: {}", JSON.toJSONString(metaInflowMessage));
        if (metaInflowMessage == null) {
            throw new StreamGlobalException(GlobalExceptionType.IllegalStreamMessage);
        }

        InPort firstInPort = metaInflowMessage.getInPortDataMap().keySet().stream().findFirst().get();
        HandlerMethodEntry handlerMethodEntry;
        if ((handlerMethodEntry = registry.get(firstInPort)) == null) {
            // TODO: 2024/3/12 发送事件到平台
            throw new StreamGlobalException(GlobalExceptionType.NoSuchHandlerException);
        }

        InflowMessage inflowMessage;
        OutflowMessage outflowMessage;
        try {
            inflowMessage = metaInflowMessage.covert();
            outflowMessage = (OutflowMessage) handlerMethodEntry.getMethod().invoke(handlerMethodEntry.getInstance(), inflowMessage);
            if (outflowMessage == null) {
                return null;
            }
            outflowMessage.mergeOutPortData(handlerMethodEntry.getSpecifiedDefaultOutPorts());
        } catch (IllegalAccessException e) {
            log.error("invoke suanpan handler error", e);
            throw new StreamGlobalException(GlobalExceptionType.InvocationHandlerException, e);
        } catch (InvocationTargetException e) {
            log.error("invoke suanpan handler error", e);
            throw new StreamGlobalException(GlobalExceptionType.InvocationHandlerException, e);
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
