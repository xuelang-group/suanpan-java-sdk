package com.xuelang.suanpan.domain.handler;

import com.xuelang.suanpan.common.exception.IllegalRequestException;
import com.xuelang.suanpan.common.exception.InvocationHandlerException;
import com.xuelang.suanpan.common.exception.NoSuchHandlerException;
import com.xuelang.suanpan.domain.io.InPort;
import com.xuelang.suanpan.domain.io.OutPort;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class HandlerProxy {
    private final Map<InPort, MethodEntry> PROXY_METHOD_ENTRY_MAP;

    public HandlerProxy() {
        PROXY_METHOD_ENTRY_MAP = HandlerScanner.scan();
    }

    /**
     * @param request 如果是同步处理器，则message中可能包含多个输入端口的数据；如果是异步处理器，则message中只会包含一个输入端口的数据；
     * @return
     * @throws RuntimeException
     */
    public HandlerResponse invoke(HandlerRequest request) throws NoSuchHandlerException, IllegalRequestException, InvocationHandlerException {
        if (request == null) {
            throw new IllegalRequestException("received message inport data is empty, can not invoke suanpan handler");
        }
        InPort firstInPort = request.getMsg().get(0).getInPort();
        MethodEntry methodEntry;
        if ((methodEntry = PROXY_METHOD_ENTRY_MAP.get(firstInPort)) == null) {
            // TODO: 2024/3/12 发送事件到平台
            throw new NoSuchHandlerException("there is no handler for inport: " + firstInPort.getUuid());
        }

        HandlerResponse response = null;
        try {
            response = (HandlerResponse) methodEntry.getMethod().invoke(methodEntry.getInstance(), request);
            merge(response.getNonSpecifiedOutPortDataList(), methodEntry.getOutPorts(), response.getOutPortDataMap());
        } catch (IllegalAccessException e) {
            throw new InvocationHandlerException("invoke suanpan handler error", e);
        } catch (InvocationTargetException e) {
            throw new InvocationHandlerException("invoke suanpan handler error", e);
        }

        return response;
    }

    private void merge(List<Object> src , List<OutPort> srcDefaultOutPorts, Map<OutPort, Object> target) {
        if (!CollectionUtils.isEmpty(src)) {
            if (target == null || target.isEmpty()) {
                for (Object outPortItem : srcDefaultOutPorts) {
                    target.put((OutPort) outPortItem, src);
                }
            } else {
                for (Object outPortItem : srcDefaultOutPorts) {
                    List<Object> result = new ArrayList<>();
                    Object origin = target.get((OutPort) outPortItem);
                    if (origin != null){
                        result.add(origin);
                    }

                    src.stream().forEach(item->{
                        result.add(item);
                    });
                    target.put((OutPort) outPortItem, result);
                }
            }
        }
    }
}
