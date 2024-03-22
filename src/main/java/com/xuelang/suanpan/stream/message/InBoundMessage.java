package com.xuelang.suanpan.stream.message;

import com.xuelang.suanpan.stream.handler.HandlerRequest;
import com.xuelang.suanpan.common.entities.io.InPort;
import com.xuelang.suanpan.stream.handler.PollingResponse;
import com.xuelang.suanpan.stream.handler.InPortData;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class InBoundMessage extends Context {
    private Map<InPort, Object> inPortDataMap = new HashMap<>();

    public Map<InPort, Object> getInPortDataMap() {
        return inPortDataMap;
    }

    public void append(InPort inPort, Object value) {
        inPortDataMap.put(inPort, value);
    }

    public HandlerRequest covert() {
        HandlerRequest request = new HandlerRequest();
        request.setRequestId(requestId);
        request.setMessageId(messageId);
        request.setSuccess(success);

        List<InPortData> msg = new ArrayList<>();
        this.inPortDataMap.keySet().stream().forEach(inPort -> {
            InPortData tmp = new InPortData();
            tmp.setInPort(inPort);
            tmp.setData(this.inPortDataMap.get(inPort));
            msg.add(tmp);
        });
        request.setMsg(msg);
        if (extra != null) {
            request.setExtra(extra);
        }


        return request;
    }


    public PollingResponse covertPollingResponse() {
        PollingResponse pollingResponse = new PollingResponse();
        pollingResponse.setRequestId(requestId);
        pollingResponse.setMessageId(messageId);
        pollingResponse.setSuccess(success);

        List<InPortData> msg = new ArrayList<>();
        this.inPortDataMap.keySet().stream().forEach(inPort -> {
            InPortData tmp = new InPortData();
            tmp.setInPort(inPort);
            tmp.setData(this.inPortDataMap.get(inPort));
            msg.add(tmp);
        });
        pollingResponse.setMsg(msg);
        if (extra != null) {
            pollingResponse.setExtra(extra);
        }


        return pollingResponse;
    }

    public boolean isExpired() {
        if (extra == null) {
            return false;
        }

        return extra.isExpired();
    }
}
