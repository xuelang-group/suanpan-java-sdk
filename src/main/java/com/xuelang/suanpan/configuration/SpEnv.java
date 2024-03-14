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

    static {
        String spNodeInfoBase64Str = System.getenv("SP_NODE_INFO");
        if (StringUtils.isNotBlank(spNodeInfoBase64Str)) {
            JSONObject spNodeInfo = JSONObject.parseObject(new String(Base64.getDecoder().decode(spNodeInfoBase64Str)));
            JSONObject inputs = spNodeInfo.getJSONObject("inputs");
            if (inputs != null && !inputs.isEmpty()) {
                inputs.values().stream().forEach(v -> {
                    InPort inPort = new InPort();
                    String uuid = ((JSONObject) v).getString("uuid");
                    inPort.setName(((JSONObject) v).getString("name"));
                    inPort.setType(((JSONObject) v).getString("type"));
                    inPort.setSubType(((JSONObject) v).getString("subtype"));
                    inPort.setDescription(((JSONObject) v).getString("description"));
                    inPortMap.put(uuid, inPort);
                });
            }

            JSONObject outputs = spNodeInfo.getJSONObject("outputs");
            if (outputs != null && !outputs.isEmpty()) {
                outputs.values().stream().forEach(v -> {
                    OutPort outPort = new OutPort();
                    String uuid = ((JSONObject) v).getString("uuid");
                    outPort.setName(((JSONObject) v).getString("name"));
                    outPort.setType(((JSONObject) v).getString("type"));
                    outPort.setSubType(((JSONObject) v).getString("subtype"));
                    outPort.setDescription(((JSONObject) v).getString("description"));
                    outPortMap.put(uuid, outPort);
                });
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

    public static List<OutPort> getOutPorts() {
        return new ArrayList<>(outPortMap.values());
    }

    public static Long getQueueMaxSendLen() {
        Object value = get(ConfigurationKeys.streamSendQueueMaxLengthKey);
        return value != null ? (Long) value : null;
    }

    public static Boolean getQueueSendTrim() {
        Object value = get(ConfigurationKeys.streamSendQueueTrimImmediatelyKey);
        return value != null ? (Boolean) value : null;
    }

    public static String getNodeId() {
        Object value = get(ConfigurationKeys.nodeIdKey);
        return value != null ? (String) value : null;
    }

    public static String getSendMasterQueue() {
        Object value = get(ConfigurationKeys.streamSendQueueKey);
        return value != null ? (String) value : null;
    }

    public static Boolean getEnableP2pSend() {
        Object value = get(ConfigurationKeys.enableP2pSendKey);
        return value != null ? (Boolean) value : null;
    }

    public static String getStreamHost() {
        Object value = get(ConfigurationKeys.mqRedisHostKey);
        return value != null ? (String) value : null;
    }

    public static Integer getStreamPort() {
        Object value = get(ConfigurationKeys.mqRedisPortKey);
        return value != null ? (Integer) value : null;
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

    public static int getMaxInPortIndex() {
        return 0;
    }

    public static int getMaxOutPortIndex() {
        return 0;
    }
}
