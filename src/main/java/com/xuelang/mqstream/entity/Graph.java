package com.xuelang.mqstream.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;

import java.util.List;
import java.util.Optional;


public final class Graph {
    final JSONObject processes;
    final List<Connection> connections;

    class Processes {
        private JSONObject metadata;
    }

    @Getter
    static class Connection {
        private JSONObject tgt;
        private JSONObject metadata;
        private JSONObject src;
    }

    public Graph(Builder builder) {
        this.processes = builder.processes;
        this.connections = builder.connections;

    }

    public JSONObject getProcesses() {
        return this.processes;
    }

    public List<Connection> getConnections() {
        return this.connections;
    }

    public String filter(String srcNodeId, String port) {
        System.out.println(JSONObject.toJSON(this.connections).toString());
        Optional<Connection> conn = this.connections.stream().filter(c -> c.getSrc().getString("process").equals(srcNodeId) && c.getSrc().getString("port").equals(port)).findFirst();
        return conn.get().getTgt().getString("process");
    }

    public static final class Builder {
        JSONObject processes;
        List<Connection> connections;

        public Builder() {
        }

        public Builder processes(JSONObject processes) {
            this.processes = processes;
            return this;
        }

        public Builder connections(JSONArray connections) {
            this.connections = connections.toJavaList(Connection.class);

            return this;
        }

        public Graph build() {
            return new Graph(this);
        }
    }


}
