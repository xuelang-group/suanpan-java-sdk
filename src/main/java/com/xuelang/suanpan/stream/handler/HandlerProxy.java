package com.xuelang.suanpan.stream.handler;

import com.alibaba.fastjson2.JSON;
import com.xuelang.suanpan.common.entities.io.InPort;
import com.xuelang.suanpan.common.exception.IllegalRequestException;
import com.xuelang.suanpan.common.exception.InvocationHandlerException;
import com.xuelang.suanpan.common.exception.NoSuchHandlerException;
import com.xuelang.suanpan.stream.handler.request.HandlerRequest;
import com.xuelang.suanpan.stream.handler.response.HandlerResponse;
import com.xuelang.suanpan.stream.message.InBoundMessage;
import com.xuelang.suanpan.stream.message.OutBoundMessage;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@Slf4j
public class HandlerProxy {
    private final Map<InPort, HandlerMethodEntry> registry;

    public HandlerProxy() {
        registry = HandlerScanner.scan();
    }


    /*public HandlerResponse invoke(HandlerRequest request) throws NoSuchHandlerException, IllegalRequestException, InvocationHandlerException {
        log.info("received handler request: {}", JSON.toJSONString(request));
        if (request == null) {
            throw new IllegalRequestException("received message inport data is empty, can not invoke suanpan handler");
        }
        InPort firstInPort = request.getData().get(0).getInPort();
        HandlerMethodEntry handlerMethodEntry;
        if ((handlerMethodEntry = registry.get(firstInPort)) == null) {
            // TODO: 2024/3/12 发送事件到平台
            throw new NoSuchHandlerException("there is no handler for inport: " + firstInPort.getUuid());
        }

        HandlerResponse response;
        try {
            response = (HandlerResponse) handlerMethodEntry.getMethod().invoke(handlerMethodEntry.getInstance(), request);
            response.mergeOutPortData(handlerMethodEntry.getSpecifiedDefaultOutPorts());
            copyContext(request, response);
        } catch (IllegalAccessException e) {
            throw new InvocationHandlerException("invoke suanpan handler error", e);
        } catch (InvocationTargetException e) {
            throw new InvocationHandlerException("invoke suanpan handler error", e);
        }


        if (response.getValiditySeconds() != null) {
            response.getExtra().setExpireTime(System.currentTimeMillis() + response.getValiditySeconds() * 1000);
        } else {
            response.getExtra().setExpireTime(Long.MAX_VALUE);
        }

        log.info("invoked handler response:{}", JSON.toJSONString(response));
        return response;
    }
*/

    public OutBoundMessage invoke(InBoundMessage inBoundMessage) throws NoSuchHandlerException, IllegalRequestException,
            InvocationHandlerException {
        log.info("inbound message: {}", JSON.toJSONString(inBoundMessage));
        if (inBoundMessage == null) {
            throw new IllegalRequestException("received message inport data is empty, can not invoke suanpan handler");
        }

        InPort firstInPort = inBoundMessage.getInPortDataMap().keySet().stream().findFirst().get();
        HandlerMethodEntry handlerMethodEntry;
        if ((handlerMethodEntry = registry.get(firstInPort)) == null) {
            // TODO: 2024/3/12 发送事件到平台
            throw new NoSuchHandlerException("there is no handler for inport: " + firstInPort.getUuid());
        }

        HandlerRequest request;
        HandlerResponse response;
        try {
            request = inBoundMessage.covert();
            response = (HandlerResponse) handlerMethodEntry.getMethod().invoke(handlerMethodEntry.getInstance(), request);
            if (response == null) {
                return null;
            }
            response.mergeOutPortData(handlerMethodEntry.getSpecifiedDefaultOutPorts());
        } catch (IllegalAccessException e) {
            throw new InvocationHandlerException("invoke suanpan handler error", e);
        } catch (InvocationTargetException e) {
            throw new InvocationHandlerException("invoke suanpan handler error", e);
        }

        OutBoundMessage outBoundMessage = new OutBoundMessage();
        outBoundMessage.setHeader(request.getHeader());
        outBoundMessage.setOutPortDataMap(response.getOutPortDataMap());

        Long validitySec = response.getValiditySeconds();
        if (validitySec != null && validitySec > 0){
            outBoundMessage.refreshExpireTime(validitySec*1000);
        }

        log.info("outbound message:{}", JSON.toJSONString(outBoundMessage));
        return outBoundMessage;
    }
}
