package com.xuelang.suanpan.stream.handler;

import com.xuelang.suanpan.common.entities.io.OutPort;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.stream.message.Context;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandlerResponse extends Context {
    private Long validitySeconds;
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

    public Long getValiditySeconds() {
        return validitySeconds;
    }

    public void merge(List<OutPort> specifiedDefaultOutPorts) {
        if (!CollectionUtils.isEmpty(nonSpecifiedOutPortDataList) && !CollectionUtils.isEmpty(specifiedDefaultOutPorts)) {
            if (outPortDataMap == null || outPortDataMap.isEmpty()) {
                for (Object outPortItem : specifiedDefaultOutPorts) {
                    outPortDataMap.put((OutPort) outPortItem, nonSpecifiedOutPortDataList);
                }
            } else {
                for (Object outPortItem : specifiedDefaultOutPorts) {
                    List<Object> result = new ArrayList<>();
                    Object origin = outPortDataMap.get((OutPort) outPortItem);
                    if (origin != null){
                        result.add(origin);
                    }

                    nonSpecifiedOutPortDataList.stream().forEach(item->{
                        result.add(item);
                    });
                    outPortDataMap.put((OutPort) outPortItem, result);
                }
            }
        }
    }

    public static class ResponseBuilder{
        private HandlerResponse response = new HandlerResponse();
        public ResponseBuilder append(@Nullable Integer outPortIndex, Object data) throws RuntimeException {
            if (outPortIndex == null) {
                response.getNonSpecifiedOutPortDataList().add(data);
            } else {
                OutPort outPort;
                if ((outPort = ConstantConfiguration.getOutPortByIndex(outPortIndex)) == null) {
                    throw new RuntimeException("current component has no this outPort: " + "out" + outPortIndex);
                }

                // TODO: 2024/3/12 根据输出端口数据类型，将data转成对应的类型
                response.getOutPortDataMap().put(outPort, data);
            }

            return this;
        }

        /**
         * 构建response方法
         * @param validitySeconds 消息的有效期时间，单位秒；
         * @return
         */
        public HandlerResponse build(@Nullable Long validitySeconds ){
            if (validitySeconds!=null && validitySeconds<=0){
                throw new IllegalArgumentException("validitySeconds can not be negative number!");
            }
            this.response.validitySeconds = validitySeconds;
            return this.response;
        }
    }
}
