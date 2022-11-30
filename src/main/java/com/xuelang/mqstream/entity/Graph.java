package com.xuelang.mqstream.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.rmi.UnexpectedException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@Slf4j
@Builder
public class Graph {
    private JSONObject processes;
    private List<GraphConnection> connections;

    public JSONObject getProcesses() {
        return this.processes;
    }

    public List<GraphConnection> getConnections() {
        return this.connections;
    }

    /**
     * 根据port名获取portUUID
     * */
    public String getPortUUIDWithPortName(String nodeId,String port) throws UnexpectedException {
        JSONArray ports=this.processes.getJSONObject(nodeId).getJSONObject("metadata").getJSONObject("def").getJSONArray("ports");
        AtomicReference<String> result=new AtomicReference<>();
        ports.forEach( item->{
            String portUUID=JSON.parseObject(item.toString()).getString("uuid");
            String portZH=JSON.parseObject(item.toString()).getJSONObject("description").getString("zh_CN");
            String portEN=JSON.parseObject(item.toString()).getJSONObject("description").getString("en_US");
            if (portUUID.equals(port) || (StringUtils.isNotBlank(portZH) && portZH.equals(port)) || (StringUtils.isNotBlank(portEN) && portEN.equals(port))) {
                result.set(portUUID);
                return;
            }
        });
        if(result.get()==null||result.get().equals("")){
            log.error("port-{}'s UUID not found!",port);
            throw new UnexpectedException("port-" + port + "\'name not found!");
        }
        return result.get();
    }

    /**
     * 根据portUUID及nodeId获取端口名称参数
     */
    public String getPortNameWithPortUUID(String nodeId, String portUUID) throws UnexpectedException {
        JSONArray ports = this.processes.getJSONObject(nodeId).getJSONObject("metadata").getJSONObject("def").getJSONArray("ports");
        AtomicReference<String> result = new AtomicReference<>();
        ports.forEach(item -> {
            String port = JSON.parseObject(item.toString()).getString("uuid");
            String portZH = JSON.parseObject(item.toString()).getJSONObject("description").getString("zh_CN");
            String portEN = JSON.parseObject(item.toString()).getJSONObject("description").getString("en_US");
            if (portUUID.equals(port) || (StringUtils.isNotBlank(portZH)&&portZH.equals(port)) || (StringUtils.isNotBlank(portEN)&&portEN.equals(port))) {
                result.set(portZH);
                return;
            }
        });
        if (result.get() == null || result.get().equals("")) {
            log.error("port-{}'s name not found!", portUUID);
            throw new UnexpectedException("port-" + portUUID + "\'name not found!");
        }
        return result.get();
    }

    public JSONObject filter(String srcNodeId, String port) throws UnexpectedException {
        if (connections.size() > 0) {
            Stream<GraphConnection> connectionStream = this.connections.stream().filter((GraphConnection c) -> {

                if (c.getSrc().getString("process").equals(srcNodeId) && c.getSrc().getString("port").equals(port)
                ) {
                    return true;
                }
                return false;
            });
            Optional<GraphConnection> conn = connectionStream.findFirst();
            if (conn == null) return null;
            return conn.get().getTgt();
        }
        throw new UnexpectedException("there is no connections in app graph");
    }
}

