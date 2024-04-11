package com.xuelang.suanpan.stream.message;

import com.xuelang.suanpan.common.entities.io.InPort;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MetaInflowMessage {
    private MetaContext metaContext;
    private Map<InPort, Object> inPortDataMap = new HashMap<>();

    public MetaContext getMetaContext() {
        return metaContext;
    }

    public void setMetaContext(MetaContext metaContext) {
        this.metaContext = metaContext;
    }

    public Map<InPort, Object> getInPortDataMap() {
        return inPortDataMap;
    }

    public void append(InPort inPort, Object value) {
        inPortDataMap.put(inPort, value);
    }

    public boolean isExpired() {
        if (metaContext == null ) {
            return false;
        }

        return metaContext.isExpired();
    }

    public boolean isEmpty() {
        return inPortDataMap == null || inPortDataMap.isEmpty();
    }

    public InflowMessage covert() {
        if (inPortDataMap == null || inPortDataMap.isEmpty()) {
            return null;
        }

        InflowMessage inflowMessage = new InflowMessage();
        Context context = new Context();
        context.setExt(metaContext.getExtra());
        context.setMessageId(metaContext.getRequestId());
        inflowMessage.setContext(context);
        inflowMessage.setData(inPortDataMap);
        return inflowMessage;
    }
}
