package com.xuelang.suanpan.domain.handler;

import com.xuelang.suanpan.configuration.SpEnv;
import com.xuelang.suanpan.domain.io.OutPort;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandlerResponse {
    private Map<OutPort, Object> outPortDataMap = new HashMap<>();

    public void appendData(@Nullable Integer outPortIndex, Object data) throws RuntimeException {
        if (outPortIndex == null) {
            List<OutPort> outPorts = SpEnv.getOutPorts();
            if (CollectionUtils.isEmpty(outPorts)){
                throw new RuntimeException("current component has no outPort, can not append return data");
            }
            outPorts.stream().forEach(outPort -> {
                outPortDataMap.put(outPort, data);
            });
        } else {
            OutPort outPort;
            if ((outPort = SpEnv.getOutPortByIndex(outPortIndex)) == null) {
                throw new RuntimeException("current component has no this outPort: " + "out" + outPortIndex);
            }

            // TODO: 2024/3/12 根据输出端口数据类型，将data转成对应的类型
            outPortDataMap.put(outPort, data);
        }
    }

    public Map<OutPort, Object> getOutPortDataMap() {
        return outPortDataMap;
    }
}
