package com.xuelang.suanpan.domain.handler;

import com.xuelang.suanpan.domain.io.InPort;
import com.xuelang.suanpan.stream.entities.Message;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class HandlerProxy {
    private final Map<InPort, HandlerEntry> HANDLER_MAP;

    public HandlerProxy() {
        HANDLER_MAP = scanHandlers();
    }

    private Map<InPort, HandlerEntry> scanHandlers() {

        return null;
    }

    /**
     * @param message 如果是同步处理器，则message中可能包含多个输入端口的数据；如果是异步处理器，则message中只会包含一个输入端口的数据；
     * @return
     * @throws RuntimeException
     */
    public HandlerResponse invoke(Message message) throws RuntimeException {
        HandlerRequest request = message.covert();
        if (request == null) {
            // TODO: 2024/3/12 发送事件到平台
            throw new RuntimeException("received inport message is empty");
        }

        InPort firstInPort = request.getMsg().get(0).getInPort();
        HandlerEntry handler;
        if ((handler = HANDLER_MAP.get(firstInPort)) == null) {
            // TODO: 2024/3/12 发送事件到平台
            throw new RuntimeException("there is no handler for inport: " + firstInPort.getUuid());
        }

        try {
            return (HandlerResponse) handler.getMethod().invoke(handler.getInstance(), request);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

       /* if (handler.isSync()) {
            try {

                handlerResponse = (HandlerResponse) handler.getMethod().invoke(handler.getInstance(), request);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                handlerResponse = (HandlerResponse) handler.getMethod().invoke(handler.getInstance(), inPortMsgMap.get(firstInPort));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        return handlerResponse;
        */
    }
}
