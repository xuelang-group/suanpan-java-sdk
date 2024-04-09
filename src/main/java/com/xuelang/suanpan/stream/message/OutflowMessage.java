package com.xuelang.suanpan.stream.message;

import com.alibaba.fastjson2.JSONObject;
import com.xuelang.suanpan.common.entities.io.OutPort;
import com.xuelang.suanpan.common.exception.GlobalExceptionType;
import com.xuelang.suanpan.common.exception.StreamGlobalException;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutflowMessage {
    private Long validitySeconds;
    private JSONObject ext;
    private Map<OutPort, Object> outPortDataMap = new HashMap<>();
    private List<Object> unSpecifiedOutPortDataList = new ArrayList<>();
    private OutflowMessage(){}

    public static OutboundMessageBuilder builder() {
        return new OutboundMessageBuilder();
    }

    public JSONObject getExt(){
        return this.ext;
    }
    public Map<OutPort, Object> getOutPortDataMap() {
        return outPortDataMap;
    }

    public Long getValiditySeconds() {
        return validitySeconds;
    }

    public void mergeOutPortData(List<OutPort> specifiedDefaultOutPorts) {
        if (!CollectionUtils.isEmpty(unSpecifiedOutPortDataList) && !CollectionUtils.isEmpty(specifiedDefaultOutPorts)) {
            if (outPortDataMap.isEmpty()) {
                for (Object outPortItem : specifiedDefaultOutPorts) {
                    outPortDataMap.put((OutPort) outPortItem, unSpecifiedOutPortDataList);
                }
            } else {
                for (Object outPortItem : specifiedDefaultOutPorts) {
                    List<Object> result = new ArrayList<>();
                    Object origin = outPortDataMap.get((OutPort) outPortItem);
                    if (origin != null) {
                        result.add(origin);
                    }

                    unSpecifiedOutPortDataList.stream().forEach(item -> {
                        result.add(item);
                    });
                    outPortDataMap.put((OutPort) outPortItem, result);
                }
            }
        }
    }

    public static class OutboundMessageBuilder {
        private OutflowMessage outflowMessage = new OutflowMessage();

        public OutboundMessageBuilder append(Integer outPortNumber, Object data) {
            OutPort outPort;
            if ((outPort = ConstantConfiguration.getByOutPortNumber(outPortNumber)) == null) {
                throw new StreamGlobalException(GlobalExceptionType.NoSuchOutPortException);
            }

            // TODO: 2024/3/12 根据输出端口数据类型，将data转成对应的类型
            outflowMessage.outPortDataMap.put(outPort, data);
            return this;
        }

        public OutboundMessageBuilder append(Object data) {
            outflowMessage.unSpecifiedOutPortDataList.add(data);
            return this;
        }

        public OutboundMessageBuilder appendExt(String key, Object value){
            if (outflowMessage.ext == null){
                outflowMessage.ext = new JSONObject();
            }

            outflowMessage.ext.put(key, value);
            return this;
        }

        public OutboundMessageBuilder setExpire(Long validitySeconds){
            if (validitySeconds != null && validitySeconds > 0) {
                outflowMessage.validitySeconds = validitySeconds;
            }

            return this;
        }

        public OutflowMessage build() {
            return this.outflowMessage;
        }
    }
}
