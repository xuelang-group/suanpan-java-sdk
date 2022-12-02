package com.xuelang.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xuelang.mqstream.api.requests.AffinityRequest;
import com.xuelang.mqstream.config.GlobalConfig;
import com.xuelang.mqstream.entity.Graph;
import com.xuelang.service.util.OkHttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.rmi.UnexpectedException;

@Slf4j
public class NodeSiblingService {
    @Autowired
    private OkHttpUtil httpClient;
    private AffinityRequest request = new AffinityRequest();
    private Graph graph;

    public String lookup(String port) throws Exception {
        log.info("start to lookup {} service name", port);
        try {
            String url = request.getUrl("/appcontroller/graph/" + GlobalConfig.userId + "/" + GlobalConfig.appId);
            String content = httpClient.get(url);
            if (!StringUtils.isNotEmpty(content)) {
                throw new UnexpectedException("get service graph error: null content!");
            }
            JSONObject jsonObject = JSON.parseObject(content);
            if (!jsonObject.getObject("success", Boolean.class)) {
                throw new UnexpectedException("get service graph error!");
            }
            String graphJsonStr = jsonObject.getJSONObject("data").getJSONObject("graphJsonStr").toJSONString();
            JSONObject graphJson = JSONObject.parseObject(graphJsonStr);
            graph = new Graph(graphJson);

            String portUUID = graph.getPortUUIDWithPortName(GlobalConfig.nodeId, port);
            Graph.GraphConnection.Tgt tgt = graph.filter(GlobalConfig.nodeId, portUUID);
            String targetNodeId = tgt.getProcess();
            String targetNodeInPort = tgt.getPort();

            String inPortName = graph.getPortNameWithPortUUID(targetNodeId, targetNodeInPort);
            String ip = this.getServiceIp(targetNodeId);

            return String.format("%s:%s", ip, inPortName);
        } catch (Exception e) {
            log.error("get service invocation err:{}", e.getMessage());
            throw new Exception("get service invocation err:{}", e);
        }
    }

    private String getSiblingServiceName(String targetNodeId) {
        return String.format("%s-%s-%s", "app", GlobalConfig.appId, targetNodeId);
    }

    private String getServiceIp(String targetNodeId) throws Exception {
        if (targetNodeId == null || targetNodeId.equals("")) {
            throw new Exception("there is no output node");
        }
        log.info("get service name for nodeId-{}", targetNodeId);
        return getSiblingServiceName(targetNodeId);
    }
}
