package com.xuelang.suanpan.configuration;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xuelang.suanpan.common.entities.connection.Connection;
import com.xuelang.suanpan.common.entities.enums.NodeReceiveMsgType;
import com.xuelang.suanpan.common.entities.io.Inport;
import com.xuelang.suanpan.common.entities.io.Outport;
import com.xuelang.suanpan.common.exception.StreamGlobalException;
import com.xuelang.suanpan.common.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;

@Slf4j
public class Parameter {
    public static final String appTypeKey = "SP_APP_TYPE";
    public static final String appParamsKey = "SP_PARAM";
    public static final String nodeInfoKey = "SP_NODE_INFO";
    public static final String userIdKey = "SP_USER_ID";
    public static final String appIdKey = "SP_APP_ID";
    public static final String nodeIdKey = "SP_NODE_ID";
    public static final String nodeGroupKey = "SP_NODE_GROUP";

    // Api
    public static final String hostKey = "SP_HOST";
    public static final String portKey = "SP_PORT";
    public static final String hostTlsKey = "SP_HOST_TLS";
    public static final String apiHostKey = "SP_API_HOST";
    public static final String apiHostTlsKey = "SP_API_HOST_TLS";
    public static final String accessKey = "SP_ACCESS_KEY";
    public static final String accessSecretKey = "SP_ACCESS_SECRET";
    public static final String userIdHeaderFieldKey = "SP_USER_ID_HEADER_FIELD";
    public static final String userSignatureHeaderFieldKey = "SP_USER_SIGNATURE_HEADER_FIELD";
    public static final String userSignVersionHeaderFieldKey = "SP_USER_SIGN_VERSION_HEADER_FIELD";

    // Storage
    public static final String storageTypeKey = "SP_STORAGE_TYPE";
    public static final String ossAccessIdKey = "SP_STORAGE_OSS_ACCESS_ID";
    public static final String ossAccessKeyKey = "SP_STORAGE_OSS_ACCESS_KEY";
    public static final String ossBucketNameKey = "SP_STORAGE_OSS_BUCKET_NAME";
    public static final String ossEndpointKey = "SP_STORAGE_OSS_ENDPOINT";
    public static final String ossDelimiterKey = "SP_STORAGE_OSS_DELIMITER";
    public static final String ossTempStoreKey = "SP_STORAGE_OSS_TEMP_STORE";
    public static final String ossDownloadNumThreadsKey = "SP_STORAGE_OSS_DOWNLOAD_NUM_THREADS";
    public static final String ossDownloadStoreNameKey = "SP_STORAGE_OSS_DOWNLOAD_STORE_NAME";
    public static final String ossUploadNumThreadsKey = "SP_STORAGE_OSS_UPLOAD_NUM_THREADS";
    public static final String ossUploadStoreNameKey = "SP_STORAGE_OSS_UPLOAD_STORE_NAME";
    public static final String storageLocalTempStoreKey = "SP_STORAGE_LOCAL_TEMP_STORE";
    public static final String minioAccessKey = "SP_STORAGE_MINIO_ACCESS_KEY";
    public static final String minioSecretKey = "SP_STORAGE_MINIO_SECRET_KEY";
    public static final String minioBucketNameKey = "SP_STORAGE_MINIO_BUCKET_NAME";
    public static final String minioEndpointKey = "SP_STORAGE_MINIO_ENDPOINT";
    public static final String minioSecureKey = "SP_STORAGE_MINIO_SECURE";
    public static final String minioDelimiterKey = "SP_STORAGE_MINIO_DELIMITER";
    public static final String minioTempStoreKey = "SP_STORAGE_MINIO_TEMP_STORE";

    //stream
    public static final String streamUserIdKey = "SP_STREAM_USER_ID";
    public static final String streamAppIdKey = "SP_STREAM_APP_ID";
    public static final String streamNodeIdKey = "SP_NODE_ID";
    public static final String streamSendQueueKey = "SP_STREAM_SEND_QUEUE";
    public static final String streamRecvQueueKey = "SP_STREAM_RECV_QUEUE";
    public static final String streamSendQueueMaxLengthKey = "SP_STREAM_SEND_QUEUE_MAX_LENGTH";
    public static final String streamSendQueueTrimImmediatelyKey = "SP_STREAM_SEND_QUEUE_TRIM_IMMEDIATELY";
    public static final String enableP2pSendKey = "SP_ENABLE_P2P_SEND";
    public static final String mqTypeKey = "SP_MQ_TYPE";
    public static final String mqRedisHostKey = "SP_MQ_REDIS_HOST";
    public static final String mqRedisPortKey = "SP_MQ_REDIS_PORT";
    public static final String streamRecvQueueDelayKey = "SP_STREAM_RECV_QUEUE_DELAY";
    public static final String mstorageTypeKey = "SP_MSTORAGE_TYPE";
    public static final String mstorageRedisDefaultExpireKey = "SP_MSTORAGE_REDIS_DEFAULT_EXPIRE";
    public static final String mstorageRedisHostKey = "SP_MSTORAGE_REDIS_HOST";
    public static final String spDockerRegistryHostKey = "SP_DOCKER_REGISTRY_HOST";
    public static final String spServiceDockerRegistryUrlKey = "SP_SERVICE_DOCKER_REGISTRY_URL";

    private static Map<String, Inport> inportMap = new HashMap<>();
    private static Map<String, Outport> outportMap = new HashMap<>();
    private static Map<String, Object> spParamMap = new HashMap<>();
    private static Integer Max_Inport_Index = -1;
    private static NodeReceiveMsgType receiveMsgType;
    private static Map<Outport, List<Connection>> outPortConnectionMap = new HashMap<>();

    static {
        String spNodeInfoBase64Str = System.getenv("SP_NODE_INFO");
        if (StringUtils.isNotBlank(spNodeInfoBase64Str)) {
            JSONObject spNodeInfo = JSONObject.parseObject(new String(Base64.getDecoder().decode(spNodeInfoBase64Str)));
            JSONObject inputs = spNodeInfo.getJSONObject("inputs");
            if (inputs != null && !inputs.isEmpty()) {
                final int[] maxInportIndex = {-1};
                inputs.values().stream().forEach(v -> {
                    Inport inPort = null;
                    Constructor<Inport> constructor;
                    try {
                        constructor = Inport.class.getDeclaredConstructor();
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

                    inportMap.put(inPort.getUuid(), inPort);
                    int number = Integer.valueOf(inPort.getUuid().substring(2));
                    if (number > maxInportIndex[0]) {
                        maxInportIndex[0] = number;
                    }
                });

                if (maxInportIndex[0] > 0) {
                    Max_Inport_Index = maxInportIndex[0];
                }
            }

            JSONObject outputs = spNodeInfo.getJSONObject("outputs");
            if (outputs != null && !outputs.isEmpty()) {
                outputs.values().stream().forEach(v -> {
                    Outport outPort = null;
                    Constructor<Outport> constructor;
                    try {
                        constructor = Outport.class.getDeclaredConstructor();
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

                    outportMap.put(outPort.getUuid(), outPort);
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
                    Outport outPort = null;
                    try {
                        outPort = Outport.bind(srcOutPortUUID);
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

    public static List<Connection> getConnections(Outport outPort){
        if (outPortConnectionMap.isEmpty()){
            return null;
        }

        return outPortConnectionMap.get(outPort);
    }

    public static NodeReceiveMsgType getReceiveMsgType() {
        return receiveMsgType;
    }

    private static JSONObject getGraph() {
        String spHost = (String) get(hostKey, null);
        if (StringUtils.isBlank(spHost)) {
            throw new RuntimeException("no such configuration, " + hostKey);
        }
        Integer spPort = null;
        if (spHost.contains(":")) {
            String[] tmp = spHost.split(":");
            spHost = tmp[0];
            spPort = Integer.valueOf(tmp[1]);
        }

        if (get(portKey, null) != null) {
            spPort = Integer.valueOf(get(portKey, null).toString());
        }

        Object tls = get(hostTlsKey, null);
        String protocol = tls == null ? "http" : "https";
        String appId = getAppId();
        String secret = getSecret();
        String userId = getUserId();
        String userIdHeaderField = (String) get(userIdHeaderFieldKey, "x-sp-user-id");
        String userSignatureHeaderField = (String) get(userSignatureHeaderFieldKey, "x-sp-signature");
        String userSignVersionHeaderField = (String) get(userSignVersionHeaderFieldKey, "x-sp-sign-version");
        String signature = HttpUtil.signature(secret, userId);
        Map<String, String> headers = new HashMap<>();
        headers.put(userIdHeaderField, userId);
        headers.put(userSignatureHeaderField, signature);
        headers.put(userSignVersionHeaderField, "v1");
        return HttpUtil.sendGet(protocol, spHost, spPort, "app/graph/" + appId, headers, null);
    }

    public static String getAppId() {
        Object appId = get(appIdKey, null);
        if (appId == null) {
            return null;
        }

        return appId.toString();
    }

    public static String getSecret() {
        Object secret = get(accessSecretKey, null);
        if (secret == null) {
            return null;
        }

        return secret.toString();
    }

    public static String getUserId() {
        Object userId = get(userIdKey, null);
        if (userId == null) {
            return null;
        }

        return userId.toString();
    }

    public static Outport getByOutportIndex(Integer outportIndex) {
        String key = "out" + outportIndex;
        return outportMap.get(key);
    }

    public static Outport getByOutPortUUID(String outPortUUID) {
        return outportMap.get(outPortUUID);
    }

    public static Inport getByInPortUuid(String key) {
        return inportMap.get(key);
    }

    public static Inport getByInportIndex(Integer inportIndex) {
        String key = "in" + inportIndex;
        return inportMap.get(key);
    }

    public static List<Outport> getOutPorts() {
        return new ArrayList<>(outportMap.values());
    }

    public static Long getQueueMaxSendLen() {
        Object value = get(streamSendQueueMaxLengthKey, null);
        return value != null ? Long.valueOf(value.toString()) : 1000L;
    }

    public static boolean getQueueSendTrim() {
        Object value = get(streamSendQueueTrimImmediatelyKey, null);
        return value != null ? Boolean.valueOf(value.toString()) : false;
    }

    public static String getNodeId() {
        Object value = get(nodeIdKey, null);
        return value != null ? (String) value : null;
    }

    public static String getSendMasterQueue() {
        Object value = get(streamSendQueueKey, null);
        return value != null ? (String) value : null;
    }

    public static String getReceiveQueue() {
        Object value = get(streamRecvQueueKey, null);
        return value != null ? (String) value : null;
    }

    public static boolean getEnableP2pSend() {
        Object value = get(enableP2pSendKey, null);
        return value != null ? Boolean.valueOf(value.toString()) : false;
    }

    public static String getStreamHost() {
        Object value = get(mqRedisHostKey, null);
        return value != null ? (String) value : null;
    }

    public static Integer getStreamPort() {
        Object value = get(mqRedisPortKey, null);
        return value != null ? Integer.valueOf(value.toString()) : null;
    }

    public static String getMqType(){
        Object value = get(mqTypeKey, "redis");
        return value.toString();
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

    public static Integer getInportMaxIndex() {
        return Max_Inport_Index;
    }
}
