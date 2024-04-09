package com.xuelang.suanpan.configuration;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xuelang.suanpan.common.entities.connection.Connection;
import com.xuelang.suanpan.common.entities.enums.NodeReceiveMsgType;
import com.xuelang.suanpan.common.entities.io.InPort;
import com.xuelang.suanpan.common.entities.io.OutPort;
import com.xuelang.suanpan.common.exception.StreamGlobalException;
import com.xuelang.suanpan.common.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;

@Slf4j
public class ConstantConfiguration {
    private static Map<String, InPort> inPortMap = new HashMap<>();
    private static Map<String, OutPort> outPortMap = new HashMap<>();
    private static Map<String, Object> spParamMap = new HashMap<>();
    private static Integer Max_InPort_Number = -1;
    private static Integer Max_OutPort_Number = -1;
    private static NodeReceiveMsgType receiveMsgType;

    private static Map<OutPort, List<Connection>> outPortConnectionMap = new HashMap<>();

    static {
        String spNodeInfoBase64Str = System.getenv("SP_NODE_INFO");
        if (StringUtils.isNotBlank(spNodeInfoBase64Str)) {
            JSONObject spNodeInfo = JSONObject.parseObject(new String(Base64.getDecoder().decode(spNodeInfoBase64Str)));
            JSONObject inputs = spNodeInfo.getJSONObject("inputs");
            if (inputs != null && !inputs.isEmpty()) {
                final int[] maxInportNumber = {-1};
                inputs.values().stream().forEach(v -> {
                    InPort inPort = null;
                    Constructor<InPort> constructor;
                    try {
                        constructor = InPort.class.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        inPort = constructor.newInstance();
                        inPort.setUuid(((JSONObject) v).getString("uuid"));
                        inPort.setName(((JSONObject) v).getString("name"));
                        inPort.setType(((JSONObject) v).getString("type"));
                        inPort.setSubType(((JSONObject) v).getString("subtype"));
                        inPort.setDescription(((JSONObject) v).getString("description"));
                    } catch (Exception e) {
                        log.error("create inPort instant from spParam error", e);
                    }

                    inPortMap.put(inPort.getUuid(), inPort);
                    int number = Integer.valueOf(inPort.getUuid().substring(2));
                    if (number > maxInportNumber[0]) {
                        maxInportNumber[0] = number;
                    }
                });

                if (maxInportNumber[0] > 0) {
                    Max_InPort_Number = maxInportNumber[0];
                }
            }

            JSONObject outputs = spNodeInfo.getJSONObject("outputs");
            if (outputs != null && !outputs.isEmpty()) {
                final int[] maxOutPortNumber = {-1};
                outputs.values().stream().forEach(v -> {
                    OutPort outPort = null;
                    Constructor<OutPort> constructor;
                    try {
                        constructor = OutPort.class.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        outPort = constructor.newInstance();
                        outPort.setUuid(((JSONObject) v).getString("uuid"));
                        outPort.setName(((JSONObject) v).getString("name"));
                        outPort.setType(((JSONObject) v).getString("type"));
                        outPort.setSubType(((JSONObject) v).getString("subtype"));
                        outPort.setDescription(((JSONObject) v).getString("description"));
                    } catch (Exception e) {
                        log.error("create outPort instant from spParam error", e);
                    }

                    outPortMap.put(outPort.getUuid(), outPort);
                    int number = Integer.valueOf(outPort.getUuid().substring(3));
                    if (number > maxOutPortNumber[0]) {
                        maxOutPortNumber[0] = number;
                    }
                });

                if (maxOutPortNumber[0] > 0) {
                    Max_OutPort_Number = maxOutPortNumber[0];
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

    static {
        JSONObject graph = getGraph();
        if (graph != null) {
            JSONObject graphData = graph.getJSONObject("data");
            JSONObject processData = graphData.getJSONObject("processes");
            if (processData != null && !processData.isEmpty()) {
                JSONObject nodeDefinition = processData.getJSONObject(getNodeId());
                receiveMsgType = NodeReceiveMsgType.getByCode(nodeDefinition.getJSONObject("metadata").getString("receieveMsgType"));
            }

            JSONArray connData = graphData.getJSONArray("connections");
            if (connData != null && !connData.isEmpty()) {
                JSONObject[] connArrays = connData.toArray(JSONObject.class);
                for (JSONObject item : connArrays) {
                    String srcNodeId = item.getJSONObject("src").getString("process");
                    if (!srcNodeId.equals(getNodeId())) {
                        continue;
                    }

                    String srcOutPortUUID = item.getJSONObject("src").getString("port");
                    OutPort outPort = null;
                    try {
                        outPort = OutPort.bind(srcOutPortUUID);
                    } catch (StreamGlobalException e) {
                        log.error("bind outPort error", e);
                        throw e;
                    }

                    JSONObject tgt = item.getJSONObject("tgt");
                    String tgtNodeId = tgt.getString("process");
                    String tgtInPortUUID = tgt.getString("port");

                    Connection connection = new Connection();
                    connection.setSrcNodeId(srcNodeId);
                    connection.setSrcOutPortUUID(srcOutPortUUID);
                    connection.setTgtNodeId(tgtNodeId);
                    connection.setTgtInPortUUID(tgtInPortUUID);
                    connection.setTgtQueue("mq-master-" + getUserId() + "-" + getAppId() + "-" + tgtNodeId);
                    outPortConnectionMap.compute(outPort, (key, existedList)->{
                        if (existedList == null){
                            existedList = new ArrayList<>();
                        }

                        existedList.add(connection);
                        return existedList;
                    });
                }
            }
        }
    }

    public static List<Connection> getConnections(OutPort outPort){
        if (outPortConnectionMap.isEmpty()){
            return null;
        }

        return outPortConnectionMap.get(outPort);
    }

    public static NodeReceiveMsgType getReceiveMsgType() {
        return receiveMsgType;
    }

    private static JSONObject getGraph() {
        String spHost = (String) get(ConfigurationKeys.hostKey, null);
        if (StringUtils.isBlank(spHost)) {
            throw new RuntimeException("no such configuration, " + ConfigurationKeys.hostKey);
        }
        Integer spPort = null;
        if (spHost.contains(":")) {
            String[] tmp = spHost.split(":");
            spHost = tmp[0];
            spPort = Integer.valueOf(tmp[1]);
        }

        if (get(ConfigurationKeys.portKey, null) != null) {
            spPort = Integer.valueOf(get(ConfigurationKeys.portKey, null).toString());
        }

        Object tls = get(ConfigurationKeys.hostTlsKey, null);
        String protocol = tls == null ? "http" : "https";
        String appId = getAppId();
        String secret = getSecret();
        String userId = getUserId();
        String userIdHeaderField = (String) get(ConfigurationKeys.userIdHeaderFieldKey, "x-sp-user-id");
        String userSignatureHeaderField = (String) get(ConfigurationKeys.userSignatureHeaderFieldKey, "x-sp-signature");
        String userSignVersionHeaderField = (String) get(ConfigurationKeys.userSignVersionHeaderFieldKey, "x-sp-sign-version");
        String signature = HttpUtil.signature(secret, userId);
        Map<String, String> headers = new HashMap<>();
        headers.put(userIdHeaderField, userId);
        headers.put(userSignatureHeaderField, signature);
        headers.put(userSignVersionHeaderField, "v1");
        return HttpUtil.sendGet(protocol, spHost, spPort, "app/graph/" + appId, headers, null);
    }

    public static String getAppId() {
        Object appId = get(ConfigurationKeys.appIdKey, null);
        if (appId == null) {
            return null;
        }

        return appId.toString();
    }

    public static String getSecret() {
        Object secret = get(ConfigurationKeys.accessSecretKey, null);
        if (secret == null) {
            return null;
        }

        return secret.toString();
    }

    public static String getUserId() {
        Object userId = get(ConfigurationKeys.userIdKey, null);
        if (userId == null) {
            return null;
        }

        return userId.toString();
    }

    public static OutPort getByOutPortNumber(Integer outPortNumber) {
        String key = "out" + outPortNumber;
        return outPortMap.get(key);
    }

    public static OutPort getByOutPortUUID(String outPortUUID) {
        return outPortMap.get(outPortUUID);
    }

    public static InPort getByInPortUuid(String key) {
        return inPortMap.get(key);
    }

    public static InPort getByInPortNumber(Integer inportNumber) {
        String key = "in" + inportNumber;
        return inPortMap.get(key);
    }

    public static List<OutPort> getOutPorts() {
        return new ArrayList<>(outPortMap.values());
    }

    public static Long getQueueMaxSendLen() {
        Object value = get(ConfigurationKeys.streamSendQueueMaxLengthKey, null);
        return value != null ? Long.valueOf(value.toString()) : 1000L;
    }

    public static boolean getQueueSendTrim() {
        Object value = get(ConfigurationKeys.streamSendQueueTrimImmediatelyKey, null);
        return value != null ? Boolean.valueOf(value.toString()) : false;
    }

    public static String getNodeId() {
        Object value = get(ConfigurationKeys.nodeIdKey, null);
        return value != null ? (String) value : null;
    }

    public static String getSendMasterQueue() {
        Object value = get(ConfigurationKeys.streamSendQueueKey, null);
        return value != null ? (String) value : null;
    }

    public static String getReceiveQueue() {
        Object value = get(ConfigurationKeys.streamRecvQueueKey, null);
        return value != null ? (String) value : null;
    }

    public static boolean getEnableP2pSend() {
        Object value = get(ConfigurationKeys.enableP2pSendKey, null);
        return value != null ? Boolean.valueOf(value.toString()) : false;
    }

    public static String getStreamHost() {
        Object value = get(ConfigurationKeys.mqRedisHostKey, null);
        return value != null ? (String) value : null;
    }

    public static Integer getStreamPort() {
        Object value = get(ConfigurationKeys.mqRedisPortKey, null);
        return value != null ? Integer.valueOf(value.toString()) : null;
    }

    public static Object get(String key, @Nullable Object defaultValue) {
        Objects.requireNonNull(key, "key cannot be null");
        String value = System.getenv(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }

        if (key.startsWith("SP_")) {
            key = key.split("SP_")[1];
        }

        Object tmp = spParamMap.get(key);
        if (tmp == null) {
            return defaultValue;
        }

        return tmp;
    }

    public static Integer getMaxInPortNumber() {
        return Max_InPort_Number;
    }

    public static Integer getMaxOutPortNumber() {
        return Max_OutPort_Number;
    }


}
