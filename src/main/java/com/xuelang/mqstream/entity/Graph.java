package com.xuelang.mqstream.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Builder
public class Graph {
    private JSONObject processes;
    private List<GraphConnection> connections;


//    private Graph(Builder builder) {
//        this.processes = builder.processes;
//        this.connections = builder.connections;
//
//    }

    public JSONObject getProcesses() {
        return this.processes;
    }

    public List<GraphConnection> getConnections() {
        return this.connections;
    }

    public String filter(String srcNodeId, String port) {
        if (connections.size() > 0) {
            log.info("connections:" + JSON.toJSONString(this.connections).toString());
            Stream<GraphConnection> connectionStream = this.connections.stream().filter((GraphConnection c) -> {

                log.info("c:" + JSON.toJSON(c).toString());
                log.info("process:" + c.getSrc().getString("process"));
                log.info("srcNodeId:" + srcNodeId);
                log.info("port:" + c.getSrc().getString("port"));
                log.info("port1" + port);
                if (c.getSrc().getString("process").equals(srcNodeId) && c.getSrc().getString("port").equals(port)
                ) {
                    return true;
                }
                return false;
            });
            Optional<GraphConnection> conn = connectionStream.findFirst();
            if (conn == null) return null;
            return conn.get().getTgt().getString("process");
        }
        log.info("there is no connections in app graph");
        return null;
    }

//    public static class Builder {
//        JSONObject processes;
//        List<Connection> connections;
//
//        public Builder() {
//        }
//
//        public Builder processes(JSONObject processes) {
//            this.processes = processes;
//            return this;
//        }
//
//        public Builder connections(JSONArray connections) {
//            this.connections = connections.toJavaList(Connection.class);
//            return this;
//        }
//
//        public Graph build() {
//            return new Graph(this);
//        }
//    }
}

