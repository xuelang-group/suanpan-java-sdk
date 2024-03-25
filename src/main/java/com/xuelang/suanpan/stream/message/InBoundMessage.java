package com.xuelang.suanpan.stream.message;

import com.xuelang.suanpan.common.entities.io.InPort;
import com.xuelang.suanpan.stream.handler.*;
import com.xuelang.suanpan.stream.handler.request.HandlerRequest;
import com.xuelang.suanpan.stream.handler.response.PollingResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class InBoundMessage {
    private Header header;
    private Map<InPort, Object> inPortDataMap = new HashMap<>();

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Map<InPort, Object> getInPortDataMap() {
        return inPortDataMap;
    }

    public void append(InPort inPort, Object value) {
        inPortDataMap.put(inPort, value);
    }

    public boolean isExpired() {
        if (header == null ) {
            return false;
        }

        return header.isExpired();
    }

    public boolean isEmpty() {
        return inPortDataMap == null || inPortDataMap.isEmpty();
    }

    public HandlerRequest covert() {
        if (inPortDataMap == null || inPortDataMap.isEmpty()) {
            return null;
        }

        HandlerRequest request = new HandlerRequest();
        request.setHeader(header);
        request.setData(this.inPortDataMap.keySet().stream().map(inPort -> {
            InPortData tmp = new InPortData();
            tmp.setInPort(inPort);
            tmp.setData(this.inPortDataMap.get(inPort));
            return tmp;
        }).collect(Collectors.toList()));
        return request;
    }


    public PollingResponse covertPollingResponse() {
        PollingResponse pollingResponse = new PollingResponse();
        pollingResponse.setHeader(header);
        pollingResponse.setData(this.inPortDataMap.keySet().stream().map(inPort -> {
            InPortData tmp = new InPortData();
            tmp.setInPort(inPort);
            tmp.setData(this.inPortDataMap.get(inPort));
            return tmp;
        }).collect(Collectors.toList()));
        return pollingResponse;
    }
}
