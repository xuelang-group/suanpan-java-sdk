package com.xuelang.suanpan.domain.handler;

import com.xuelang.suanpan.configuration.SpEnv;
import com.xuelang.suanpan.domain.io.OutPort;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandlerResponse {
    private Map<OutPort, Object> outPortDataMap = new HashMap<>();
    private List<Object> nonSpecifiedOutPortDataList = new ArrayList<>();

    public static ResponseBuilder builder(){
        return new ResponseBuilder();
    }
    public Map<OutPort, Object> getOutPortDataMap() {
        return outPortDataMap;
    }

    public List<Object> getNonSpecifiedOutPortDataList() {
        return nonSpecifiedOutPortDataList;
    }

    public static class ResponseBuilder{
        private HandlerResponse response = new HandlerResponse();
        public ResponseBuilder append(@Nullable Integer outPortIndex, Object data) throws RuntimeException {
            if (outPortIndex == null) {
                response.getNonSpecifiedOutPortDataList().add(data);
            } else {
                OutPort outPort;
                if ((outPort = SpEnv.getOutPortByIndex(outPortIndex)) == null) {
                    throw new RuntimeException("current component has no this outPort: " + "out" + outPortIndex);
                }

                // TODO: 2024/3/12 根据输出端口数据类型，将data转成对应的类型
                response.getOutPortDataMap().put(outPort, data);
            }

            return this;
        }


        public HandlerResponse build(){
            return this.response;
        }
    }
}
