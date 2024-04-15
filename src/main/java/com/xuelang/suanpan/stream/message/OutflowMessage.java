package com.xuelang.suanpan.stream.message;

import com.alibaba.fastjson2.JSONObject;
import com.xuelang.suanpan.common.entities.io.Outport;
import com.xuelang.suanpan.common.exception.GlobalExceptionType;
import com.xuelang.suanpan.common.exception.StreamGlobalException;
import com.xuelang.suanpan.configuration.ConstantConfiguration;

import java.util.HashMap;
import java.util.Map;

public class OutflowMessage {
    private Long validitySeconds;
    private JSONObject ext;
    private Map<Outport, Object> outPortDataMap = new HashMap<>();

    private OutflowMessage() {
    }

    public static OutflowMessageBuilder builder() {
        return new OutflowMessageBuilder();
    }

    public JSONObject getExt() {
        return this.ext;
    }

    public Map<Outport, Object> getOutPortDataMap() {
        return outPortDataMap;
    }

    public Long getValiditySeconds() {
        return validitySeconds;
    }

    public static class OutflowMessageBuilder {
        private OutflowMessage outflowMessage = new OutflowMessage();

        /**
         * set outport data of outflow message
         * @param portIndex
         * @param data
         * @return
         */
        public OutflowMessageBuilder setData(Integer portIndex, Object data) {
            Outport outport;
            if ((outport = ConstantConfiguration.getByOutportIndex(portIndex)) == null) {
                throw new StreamGlobalException(GlobalExceptionType.NoSuchOutPortException);
            }

            // TODO: 2024/3/12 根据输出端口数据类型，将data转成对应的类型
            outflowMessage.outPortDataMap.put(outport, data);
            return this;
        }

        public OutflowMessageBuilder appendExtData(String key, Object value) {
            if (outflowMessage.ext == null) {
                outflowMessage.ext = new JSONObject();
            }

            outflowMessage.ext.put(key, value);
            return this;
        }

        public OutflowMessageBuilder withExpire(Long validitySeconds) {
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
