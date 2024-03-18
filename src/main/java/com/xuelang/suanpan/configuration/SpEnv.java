package com.xuelang.suanpan.configuration;

import com.alibaba.fastjson.JSONObject;
import com.xuelang.suanpan.domain.io.InPort;
import com.xuelang.suanpan.domain.io.OutPort;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class SpEnv {
    private static Map<String, InPort> inPortMap = new HashMap<>();
    private static Map<String, OutPort> outPortMap = new HashMap<>();
    private static Map<String, Object> spParamMap = new HashMap<>();
    private static Integer Max_InPort_Index = null;
    private static Integer Max_OutPort_Index = null;

    static {
        String spNodeInfoBase64Str = System.getenv("SP_NODE_INFO");
        if (StringUtils.isNotBlank(spNodeInfoBase64Str)) {
            JSONObject spNodeInfo = JSONObject.parseObject(new String(Base64.getDecoder().decode(spNodeInfoBase64Str)));
            JSONObject inputs = spNodeInfo.getJSONObject("inputs");
            if (inputs != null && !inputs.isEmpty()) {
                final int[] maxInportIndex = {-1};
                inputs.values().stream().forEach(v -> {
                    InPort inPort = new InPort();
                    inPort.setUuid(((JSONObject) v).getString("uuid"));
                    inPort.setName(((JSONObject) v).getString("name"));
                    inPort.setType(((JSONObject) v).getString("type"));
                    inPort.setSubType(((JSONObject) v).getString("subtype"));
                    inPort.setDescription(((JSONObject) v).getString("description"));
                    inPortMap.put(inPort.getUuid(), inPort);
                    int index = Integer.valueOf(inPort.getUuid().substring(2));
                    if (index> maxInportIndex[0]){
                        maxInportIndex[0] = index;
                    }
                });

                if (maxInportIndex[0]>0){
                    Max_InPort_Index = maxInportIndex[0];
                }
            }

            JSONObject outputs = spNodeInfo.getJSONObject("outputs");
            if (outputs != null && !outputs.isEmpty()) {
                final int[] maxOutportIndex = {-1};
                outputs.values().stream().forEach(v -> {
                    OutPort outPort = new OutPort();
                    outPort.setUuid(((JSONObject) v).getString("uuid"));
                    outPort.setName(((JSONObject) v).getString("name"));
                    outPort.setType(((JSONObject) v).getString("type"));
                    outPort.setSubType(((JSONObject) v).getString("subtype"));
                    outPort.setDescription(((JSONObject) v).getString("description"));
                    outPortMap.put(outPort.getUuid(), outPort);
                    int index = Integer.valueOf(outPort.getUuid().substring(3));
                    if (index> maxOutportIndex[0]){
                        maxOutportIndex[0] = index;
                    }
                });

                if (maxOutportIndex[0]>0){
                    Max_OutPort_Index = maxOutportIndex[0];
                }
            }
        }
    }

    static {
        String spParamBase64Str = System.getenv("SP_PARAM");
        if (StringUtils.isNotBlank(spParamBase64Str)) {
            String spParam = new String(Base64.getDecoder().decode(System.getenv("SP_PARAM")));
            String[] kvs = spParam.split("--");
            for (String kv : kvs) {
                if (StringUtils.isNotBlank(kv)) {
                    String[] formatKV = kv.split(" ");
                    String k = formatKV[0].replaceAll("-", "_").toUpperCase();
                    String v = formatKV[1].replaceAll("'", "");
                    spParamMap.put(k, v);
                }
            }
        }
    }

    public static InPort getInPortByUuid(String key) {
        return inPortMap.get(key);
    }

    public static OutPort getOutPortByIndex(Integer outPortIndex) {
        String key = "out" + outPortIndex;
        return outPortMap.get(key);
    }

    public static InPort getInPortByIndex(Integer inportIndex) {
        String key = "in" + inportIndex;
        return inPortMap.get(key);
    }

    public static List<OutPort> getOutPorts() {
        return new ArrayList<>(outPortMap.values());
    }

    public static Long getQueueMaxSendLen() {
        Object value = get(ConfigurationKeys.streamSendQueueMaxLengthKey);
        return value != null ? Long.valueOf(value.toString()) : 1000L;
    }

    public static boolean getQueueSendTrim() {
        Object value = get(ConfigurationKeys.streamSendQueueTrimImmediatelyKey);
        return value != null ? Boolean.valueOf(value.toString()) : false;
    }

    public static String getNodeId() {
        Object value = get(ConfigurationKeys.nodeIdKey);
        return value != null ? (String) value : null;
    }

    public static String getSendMasterQueue() {
        Object value = get(ConfigurationKeys.streamSendQueueKey);
        return value != null ? (String) value : null;
    }

    public static String getReceiveQueue(){
        Object value = get(ConfigurationKeys.streamRecvQueueKey);
        return value != null ? (String) value : null;
    }

    public static boolean getEnableP2pSend() {
        Object value = get(ConfigurationKeys.enableP2pSendKey);
        return value != null ? Boolean.valueOf(value.toString()) : false;
    }

    public static String getStreamHost() {
        Object value = get(ConfigurationKeys.mqRedisHostKey);
        return value != null ? (String) value : null;
    }

    public static Integer getStreamPort() {
        Object value = get(ConfigurationKeys.mqRedisPortKey);
        return value != null ? Integer.valueOf(value.toString()) : null;
    }

    private static Object get(String key) {
        Objects.requireNonNull(key, "key cannot be null");
        String value = System.getenv(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }

        if (key.startsWith("SP_")) {
            key = key.split("SP_")[1];
        }

        return spParamMap.get(key);
    }

    public static Integer getMaxInPortIndex() {
        return Max_InPort_Index;
    }

    public static Integer getMaxOutPortIndex() {
        return Max_OutPort_Index;
    }


}
