package com.xuelang.service;

import com.alibaba.fastjson.JSON;
import com.xuelang.mqstream.api.requests.AffinityRequest;
import com.xuelang.mqstream.config.GlobalConfig;
import com.xuelang.mqstream.entity.RegisterPortResponse;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PortService {
    private int startPort;
    private int endPort;

    public PortService(){
        this.startPort=GlobalConfig.startPort;
        this.endPort=GlobalConfig.endPort;
    }
    public PortService(int startPort, int endPort) {
        this.startPort = startPort;
        this.endPort = endPort;
    }

    private int getFreePort() {
        for (int i = this.startPort; i <= this.endPort; i++) {
            try {
                new ServerSocket(i).close();
                return i;
            } catch (IOException e) {
                continue;
            }
        }
        return -1;
    }

    public boolean registerServicePort(int logicPort, int realPort) {
        log.debug("start register service port:{}",realPort);
        AffinityRequest request = new AffinityRequest();
        Map<String, Object> data = new HashMap<>();

        data.put("userId", StringUtil.isNullOrEmpty(GlobalConfig.streamUserId) ? GlobalConfig.userId : GlobalConfig.streamUserId);
        data.put("appId", StringUtil.isNullOrEmpty(GlobalConfig.streamAppId) ? GlobalConfig.appId : GlobalConfig.streamAppId);
        data.put("nodeId", StringUtil.isNullOrEmpty(GlobalConfig.streamNodeId) ? GlobalConfig.nodeId : GlobalConfig.streamNodeId);
        data.put("nodePort", logicPort);
        data.put("port", realPort);

        String responseStr = request.post("/app/service/register", JSON.toJSONString(data));

        RegisterPortResponse registerResult = JSON.parseObject(responseStr, RegisterPortResponse.class);
        if (!registerResult.isSuccess()) {
            log.error("registerServicePort err:", registerResult.getMsg());
            return false;
        }
        log.debug("register service port {} success!",realPort);
        return true;
    }

    public int registerServicePortUntilSuccess(int logicPort) {
        boolean registerSuccess = false;
        int realPort = -1;
        do {
            realPort = getFreePort();
            log.debug("get service port {}",realPort);
            registerSuccess = registerServicePort(logicPort, realPort);
        } while (!registerSuccess);
        return realPort;
    }
}
