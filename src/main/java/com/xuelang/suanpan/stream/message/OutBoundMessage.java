package com.xuelang.suanpan.stream.message;

import com.alibaba.fastjson2.JSON;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.common.entities.connection.Connection;
import com.xuelang.suanpan.common.entities.io.OutPort;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class OutBoundMessage extends Context {
    private Map<OutPort, Object> outPortDataMap;
    private boolean p2p = ConstantConfiguration.getEnableP2pSend();
    private String sendMasterQueue = ConstantConfiguration.getSendMasterQueue();
    private String nodeId = ConstantConfiguration.getNodeId();
    private Long maxLength = ConstantConfiguration.getQueueMaxSendLen();
    private boolean approximateTrimming = ConstantConfiguration.getQueueSendTrim();

    public Map<OutPort, Object> getOutPortDataMap() {
        return outPortDataMap;
    }

    public void setOutPortDataMap(Map<OutPort, Object> outPortDataMap) {
        this.outPortDataMap = outPortDataMap;
    }

    public boolean isP2p() {
        return p2p;
    }

    public String getSendMasterQueue() {
        return sendMasterQueue;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        Objects.requireNonNull(requestId, "requestId can not be null");
        this.requestId = requestId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Extra getExtra() {
        return extra;
    }

    public void setExtra(Extra extra) {
        this.extra = extra;
    }

    public long getMaxLength() {
        return maxLength;
    }

    public boolean isApproximateTrimming() {
        return approximateTrimming;
    }

    public Object[] toMasterQueueData() {
        List<Object> list = new ArrayList<>();
        if (StringUtils.isNotBlank(this.nodeId)) {
            list.add("node_id");
            list.add(this.nodeId);
        }
        if (StringUtils.isNotBlank(this.requestId)) {
            list.add("request_id");
            list.add(this.requestId);
        }
        list.add("max_length");
        list.add(this.maxLength.toString());
        list.add("approximate_trimming");
        list.add(String.valueOf(this.approximateTrimming));
        list.add("success");
        list.add(String.valueOf(this.success));
        if (this.extra != null) {
            list.add("extra");
            list.add(JSON.toJSONString(this.extra));
        }
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
        if (StringUtils.isNotBlank(this.requestId)) {
            commonInfo.add("request_id");
            commonInfo.add(this.requestId);
        }
        commonInfo.add("max_length");
        commonInfo.add(this.maxLength.toString());
        commonInfo.add("approximate_trimming");
        commonInfo.add(String.valueOf(this.approximateTrimming));
        commonInfo.add("success");
        commonInfo.add(String.valueOf(this.success));
        if (this.extra != null) {
            commonInfo.add("extra");
            commonInfo.add(JSON.toJSONString(this.extra));
        }

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
}
