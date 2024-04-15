package com.xuelang.suanpan.stream.message;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.common.entities.connection.Connection;
import com.xuelang.suanpan.common.entities.io.Outport;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class MetaOutflowMessage {
    private MetaContext metaContext;
    private Map<Outport, Object> outPortDataMap;
    private boolean p2p = ConstantConfiguration.getEnableP2pSend();
    private String sendMasterQueue = ConstantConfiguration.getSendMasterQueue();
    private String nodeId = ConstantConfiguration.getNodeId();
    private Long maxLength = ConstantConfiguration.getQueueMaxSendLen();
    private boolean approximateTrimming = ConstantConfiguration.getQueueSendTrim();

    public MetaOutflowMessage(){
        metaContext = new MetaContext();
    }

    public void setMetaContext(MetaContext metaContext) {
        this.metaContext = metaContext;
    }

    public Map<Outport, Object> getOutPortDataMap() {
        return outPortDataMap;
    }

    public void setOutPortDataMap(Map<Outport, Object> outPortDataMap) {
        this.outPortDataMap = outPortDataMap;
    }

    public boolean isP2p() {
        return p2p;
    }

    public String getSendMasterQueue() {
        return sendMasterQueue;
    }

    public long getMaxLength() {
        return maxLength;
    }

    public boolean isApproximateTrimming() {
        return approximateTrimming;
    }

    public Object[] toStreamData() {
        List<Object> list = new ArrayList<>();
        if (StringUtils.isNotBlank(this.nodeId)) {
            list.add("node_id");
            list.add(this.nodeId);
        }
        if (StringUtils.isNotBlank(this.metaContext.getRequestId())) {
            list.add("request_id");
            list.add(this.metaContext.getRequestId());
        }
        list.add("max_length");
        list.add(this.maxLength.toString());
        list.add("approximate_trimming");
        list.add(String.valueOf(this.approximateTrimming));
        list.add("success");
        list.add("true");
        list.add("extra");
        list.add(JSON.toJSONString(this.metaContext.getExtra()));

        outPortDataMap.keySet().stream().forEach(key -> {
            list.add(key.getUuid());
            list.add(JSON.toJSONString(outPortDataMap.get(key)));
        });
        return list.toArray();
    }

    public Map<String, Object[]> toP2PQueueData() {
        Map<String, List<Object>> tmpResult = new HashMap<>();
        List<Object> commonInfo = new ArrayList<>();
        if (StringUtils.isNotBlank(this.nodeId)) {
            commonInfo.add("node_id");
            commonInfo.add(this.nodeId);
        }
        if (StringUtils.isNotBlank(this.metaContext.getRequestId())) {
            commonInfo.add("request_id");
            commonInfo.add(this.metaContext.getRequestId());
        }
        commonInfo.add("max_length");
        commonInfo.add(this.maxLength.toString());
        commonInfo.add("approximate_trimming");
        commonInfo.add(String.valueOf(this.approximateTrimming));
        commonInfo.add("success");
        commonInfo.add("true");
        commonInfo.add("extra");
        commonInfo.add(JSON.toJSONString(this.metaContext.getExtra()));
        outPortDataMap.keySet().stream().forEach(outPort -> {
            String outPortData = JSON.toJSONString(outPortDataMap.get(outPort));
            List<Connection> tgtConnections = ConstantConfiguration.getConnections(outPort);
            if (!CollectionUtils.isEmpty(tgtConnections)){
                tgtConnections.stream().forEach(connection -> {
                    String tgtSendQueue = connection.getTgtQueue();
                    tmpResult.compute(tgtSendQueue, (inPortUUID, existedList)->{
                        if (existedList == null){
                            existedList = new ArrayList<>();
                            existedList.addAll(commonInfo);
                        }

                        existedList.add(connection.getTgtInPortUUID());
                        existedList.add(outPortData);
                        return existedList;
                    });
                });
            }
        });
        if (tmpResult.isEmpty()){
            return null;
        }

        Map<String, Object[]> result = new HashMap<>();
        tmpResult.entrySet().stream().forEach(entry->{
            result.put(entry.getKey(), entry.getValue().toArray());
        });

        return result;
    }

    public boolean isEmpty() {
        return outPortDataMap == null || outPortDataMap.isEmpty();
    }

    public void refreshExpire(Long validityMillis) {
        metaContext.refreshExpire(validityMillis);
    }

    public void updateMsgNodeOutTime() {
        metaContext.updateMsgOutTime();
    }

    public void refreshExt(JSONObject ext) {
        metaContext.refreshExt(ext);
    }
}
