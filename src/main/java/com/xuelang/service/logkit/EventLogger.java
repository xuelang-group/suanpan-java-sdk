package com.xuelang.service.logkit;

import com.alibaba.fastjson.JSONObject;
import com.xuelang.mqstream.api.requests.CommonRequest;
import com.xuelang.mqstream.config.GlobalConfig;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.*;

@Slf4j
public class EventLogger {

    private Socket socket;

    private static final String TRACE = "TRACE";
    private static final String DEBUG = "DEBUG";
    private static final String INFO = "INFO";
    private static final String WARN = "WARN";
    private static final String ERROR = "ERROR";
    private static final String QUERY = "query";

    public EventLogger() {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(GlobalConfig.userIdHeaderField, Arrays.asList(GlobalConfig.userId));
        headers.put(GlobalConfig.userSignatureHeaderField, Arrays.asList(CommonRequest.signatureV1(GlobalConfig.accessSecret, GlobalConfig.userId)));
        headers.put(GlobalConfig.userSignVersionHeaderField, Arrays.asList("v1"));
        IO.Options options = IO.Options.builder().setExtraHeaders(headers).setPath(GlobalConfig.logKitPath).setTransports(new String[]{"websocket"}).build();
        String url = getRequesUrl(GlobalConfig.logKitUrl, GlobalConfig.logKitNamespace);
        Socket socket = IO.socket(URI.create(url), options);

        socket.io().on(Socket.EVENT_CONNECT, args -> {
            log.info("connect to logKit");
        });

        socket.io().on(Socket.EVENT_CONNECT_ERROR, args -> {
            log.error("connect to logKt error", args);
        });

        socket.io().on(Socket.EVENT_DISCONNECT, args -> {
            log.warn("logKit disconnected ");
        });
        this.socket = socket.connect();
    }

    public void trace(String msg, Object... params) {
        log.trace(msg, params);
        this.emitEventLog(buildEventLog(TRACE, msg, params));
    }

    public void debug(String msg, Object... params) {
        log.debug(msg, params);
        this.emitEventLog(buildEventLog(DEBUG, msg, params));
    }

    public void info(String msg, Object... params) {
        log.info(msg, params);
        this.emitEventLog(buildEventLog(INFO, msg));
    }

    public void warn(String msg, Object... params) {
        log.warn(msg, params);
        this.emitEventLog(buildEventLog(WARN, msg, params));
    }

    public void error(String msg, Object... params) {
        log.error(msg, params);
        this.emitEventLog(buildEventLog(ERROR, msg, params));
    }

    public Object query() {
        return socket.emit(QUERY, new JSONObject().put("app", GlobalConfig.streamAppId), (Ack) args -> {
            JSONObject response = (JSONObject) args[0];
            if (Boolean.valueOf(response.get("success").toString())) {
                response.get("data");
            } else {
                response.get("error");
            }
        });
    }

    private void emitEventLog(JSONObject msg) {
        socket.emit(GlobalConfig.logKitEvent, GlobalConfig.streamAppId, msg);
    }

    public static JSONObject buildEventLog(String logLevel, String msg, Object... params) {
        JSONObject eventLog = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("node", GlobalConfig.nodeId);

        eventLog.put("title", String.format(msg, params));
        eventLog.put("level", logLevel);
        eventLog.put("time", new Date().toString());
        eventLog.put("data", data);
        return eventLog;
    }

    /**
     * 获取ws Url
     */
    private String getRequesUrl(String endPoint, String nsp) {
        endPoint = endPoint.endsWith("/") ? endPoint : String.format("%s/", endPoint);
        nsp = nsp.startsWith("/") ? nsp.substring(1) : nsp;
        return endPoint + nsp;
    }
}