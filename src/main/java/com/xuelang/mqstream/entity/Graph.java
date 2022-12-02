package com.xuelang.mqstream.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.rmi.UnexpectedException;
import java.util.*;

@Slf4j
public class Graph {
    private Map<String, Process> processes = new HashMap<>();
    private List<GraphConnection> connections;

    public Graph(JSONObject graphJson) {
        JSONObject processes = graphJson.getJSONObject("processes");
        String s = graphJson.getJSONArray("connections").toJSONString();
        System.out.println(s);
        List<GraphConnection> connections = graphJson.getJSONArray("connections").toJavaList(GraphConnection.class);
        setProcesses(processes);
        this.connections = connections;
    }

    public Map<String, Process> getProcesses() {
        return this.processes;
    }

    private Process getProcessByNodeId(String nodeId) {
        return processes.get(nodeId);
    }

    private void setProcesses(JSONObject jsonObject) {
        Set<String> keys = jsonObject.keySet();
        keys.forEach(key -> {
            JSONObject o = jsonObject.getJSONObject(key);
            Process process = JSONObject.parseObject(o.toJSONString(), Process.class);
            processes.put(key, process);
        });
    }

    public List<GraphConnection> getConnections() {
        return this.connections;
    }

    /**
     * 根据port名获取portUUID
     */
    public String getPortUUIDWithPortName(String nodeId, String port) throws UnexpectedException {
        Ports[] ports = getProcessByNodeId(nodeId).getMetadata().getDef().getPorts();
        for (Ports p : ports) {
            String portUUID = p.getUuid();
            String portZH = p.getDescription().getZh_CN();
            String portEN = p.getDescription().getEn_US();
            if (portUUID.equals(port) || (StringUtils.isNotBlank(portZH) && portZH.equals(port)) || (StringUtils.isNotBlank(portEN) && portEN.equals(port))) {
                return portUUID;
            }
        }
        log.error("port-{}'s UUID not found!", port);
        throw new UnexpectedException("port-" + port + "\'name not found!");
    }

    /**
     * 根据portUUID及nodeId获取端口名称参数
     */
    public String getPortNameWithPortUUID(String nodeId, String portUUID) throws UnexpectedException {
        Ports[] ports = this.processes.get(nodeId).getMetadata().getDef().getPorts();
        for (Ports p : ports) {
            String port = p.getUuid();
            String portZH = p.getDescription().getZh_CN();
            String portEN = p.getDescription().getEn_US();
            if (portUUID.equals(port) || (StringUtils.isNotBlank(portZH) && portZH.equals(port)) || (StringUtils.isNotBlank(portEN) && portEN.equals(port))) {
                return portZH;
            }
        }
        log.error("port-{}'s name not found!", portUUID);
        throw new UnexpectedException("port-" + portUUID + "\'name not found!");
    }

    public GraphConnection.Tgt filter(String srcNodeId, String port) throws UnexpectedException {
        for (GraphConnection c : connections) {
            if (c.getSrc().getProcess().equals(srcNodeId) && c.getSrc().getPort().equals(port)
            ) {
                return c.getTgt();
            }
        }
        throw new UnexpectedException("there is no connections in app graph");
    }

    @Data
    static class Process {
        private MetaData metadata;
    }

    @Data
    static class MetaData {
        private Def def;
    }

    @Data
    static class Def {
        private Ports[] ports;
    }

    @Data
    static class Ports {
        private String uuid;
        private Description description;

    }

    @Data
    static class Description {
        private String zh_CN;
        private String en_US;
    }

    @Data
    public static class GraphConnection {
        private Tgt tgt;
        private MetaData metadata;
        private Src src;

        @Data
        public static class Tgt {
            private String process;
            private String port;
        }

        @Data
        static class MetaData {
            private String path;
            private String custom;
        }

        @Data
        static class Src {
            private String process;
            private String port;
        }

    }
}

