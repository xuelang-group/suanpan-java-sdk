package com.xuelang.suanpan.stream.handler;

import com.alibaba.fastjson2.JSON;
import com.xuelang.suanpan.common.entities.enums.NodeReceiveMsgType;
import com.xuelang.suanpan.common.entities.io.Inport;
import com.xuelang.suanpan.common.exception.GlobalExceptionType;
import com.xuelang.suanpan.common.exception.StreamGlobalException;
import com.xuelang.suanpan.common.utils.ParameterUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HandlerRegistry {
    private static volatile HandlerRegistry registry;

    /**
     * 指定输入端口的handler
     */
    private Map<Inport, HandlerMethodEntry> methodEntryMap;

    /**
     * 优先级最好的handler
     */
    private volatile HandlerMethodEntry globalMethodEntry;

    public static HandlerRegistry getInstance() {
        if (null == registry) {
            synchronized (HandlerRegistry.class) {
                if (null == registry) {
                    registry = new HandlerRegistry();
                }
            }
        }

        return registry;
    }

    private HandlerRegistry() {
        methodEntryMap = new HashMap<>();
    }

    public synchronized void regist(@Nullable Inport inport, HandlerMethodEntry entry) throws StreamGlobalException {
        if (globalMethodEntry != null) {
            throw new StreamGlobalException(GlobalExceptionType.DuplicationHandlerException,
                    "There is already a global handler, cannot register any other handlers duplication");
        }

        if (inport == null) {
            log.info("register global handler method entry:{}", JSON.toJSONString(entry));
            globalMethodEntry = entry;
        } else {
            if (methodEntryMap.containsKey(inport)) {
                throw new StreamGlobalException(GlobalExceptionType.DuplicationHandlerException,
                        "There is already a global handler for this inport, cannot register any other handlers duplication");
            }

            if (NodeReceiveMsgType.sync.equals(ParameterUtil.getReceiveMsgType())) {
                globalMethodEntry = entry;
                log.info("node received type is sync, register global handler method entry:{}", JSON.toJSONString(entry));
            } else {
                methodEntryMap.put(inport, entry);
                log.info("inport:{}, register handler:{}, method:{}", inport.getUuid(),
                        entry.getInstance().getClass().getName(), entry.getMethod().getName());
            }
        }
    }

    public HandlerMethodEntry get(Inport inPort) {
        if (globalMethodEntry != null) {
            return globalMethodEntry;
        }

        return methodEntryMap.get(inPort);
    }

    public boolean isEmpty() {
        return globalMethodEntry == null && methodEntryMap.isEmpty();
    }
}
