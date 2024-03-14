package com.xuelang.suanpan.stream.entities;

import com.xuelang.suanpan.domain.handler.HandlerRequest;
import com.xuelang.suanpan.domain.handler.InPortData;
import com.xuelang.suanpan.domain.io.InPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Message {
    /**
     * 在stream队列中的id
     */
    private String messageId;
    /**
     * 请求id，
     */
    private String requestId;
    /**
     * 同步、异步接收消息类型
     */
    private String receiveMsgType = "async";
    private Boolean success;
    /**
     * 扩展信息
     */
    private Extra extra;
    /**
     * 输入端口对应的数据
     */
    private Map<InPort, Object> portDataMap = new HashMap<>();


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public Extra getExtra() {
        return extra;
    }

    public String getReceiveMsgType() {
        return receiveMsgType;
    }
    public void setReceiveMsgType(String receiveMsgType) {
        this.receiveMsgType = receiveMsgType;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public void setExtra(Extra extra) {
        this.extra = extra;
    }

    public Map<InPort, Object> getPortDataMap() {
        return portDataMap;
    }

    public void append(InPort inPort, Object value) {
        portDataMap.put(inPort, value);
    }

    public HandlerRequest covert(){
        if (this.portDataMap == null || this.portDataMap.isEmpty()){
            return null;
        }

        HandlerRequest request = new HandlerRequest();
        List<InPortData> msg = new ArrayList<>();
        this.portDataMap.keySet().stream().forEach(inPort -> {
            InPortData tmp = new InPortData();
            tmp.setInPort(inPort);
            tmp.setData(this.portDataMap.get(inPort));
            msg.add(tmp);
        });
        request.setMsg(msg);
        return request;
    }

}