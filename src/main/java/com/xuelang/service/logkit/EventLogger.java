package com.xuelang.service.logkit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xuelang.mqstream.api.requests.CommonRequest;
import com.xuelang.mqstream.config.GlobalConfig;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.helpers.BasicMarker;

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
        log.trace(callMarker() + msg, params);
        this.emitEventLog(buildEventLog(TRACE, msg, params));
    }

    public void debug(String msg, Object... params) {
        log.debug(callMarker() + msg, params);
        this.emitEventLog(buildEventLog(DEBUG, msg, params));
    }

    public void info(String msg, Object... params) {
        log.info(callMarker() + msg, params);
        this.emitEventLog(buildEventLog(INFO, msg, params));
    }

    public void warn(String msg, Object... params) {
        log.warn(callMarker() + msg, params);
        this.emitEventLog(buildEventLog(WARN, msg, params));
    }

    public void error(String msg, Object... params) {
        log.error(callMarker() + msg, params);
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
        if (null == params || JSONArray.parseArray(JSONObject.toJSONString(params)).size() == 0) {
            eventLog.put("title", msg);
        } else {
            eventLog.put("title", String.format(msg.replaceAll("\\{\\}", "%s"), params));

        }
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

    private String callMarker() {
        String className = Thread.currentThread().getStackTrace()[3].getClassName();//调用的类名
        String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();//调用的方法名
        int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();//调用的行数
        return className + ":" + methodName + "[" + lineNumber + "]: ";
//        return MarkerFactory.getMarker(className+":"+methodName+"["+lineNumber+"] ");
    }
}