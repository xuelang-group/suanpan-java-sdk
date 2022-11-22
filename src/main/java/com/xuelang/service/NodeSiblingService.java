package com.xuelang.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xuelang.mqstream.api.requests.AffinityRequest;
import com.xuelang.mqstream.config.GlobalConfig;
import com.xuelang.mqstream.entity.Graph;
import com.xuelang.service.util.OkHttpUtil;
import lombok.extern.flogger.Flogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Slf4j
public class NodeSiblingService {
    @Autowired
    private OkHttpUtil httpClient;
    private AffinityRequest request = new AffinityRequest();

    public String lookup(String port) throws IOException {
        String url = request.getUrl("/appcontroller/graph/" + GlobalConfig.userId + "/" + GlobalConfig.appId);
        String content = httpClient.get(url);
        if (StringUtils.isNotEmpty(content)) {
            JSONObject jsonObject = JSON.parseObject(content);
            if (jsonObject.getObject("success", Boolean.class)) {
                String graphJsonStr = jsonObject.getJSONObject("data").getJSONObject("graphJsonStr").toJSONString();
                JSONObject graphJson = JSONObject.parseObject(graphJsonStr);
                Graph graph = new Graph.Builder().processes(graphJson.getJSONObject("processes")).connections(graphJson.getJSONArray("connections")).build();
                String targetNodeId = graph.filter(GlobalConfig.nodeId, port);
                log.info("lookup service name for nodeId-{}", targetNodeId);
                return getSiblingServiceName(targetNodeId);
            }
        }
        return null;
    }

    private String getSiblingServiceName(String targetNodeId) {
        return String.format("%s-%s-%s", "app", GlobalConfig.appId, targetNodeId);
    }
}
