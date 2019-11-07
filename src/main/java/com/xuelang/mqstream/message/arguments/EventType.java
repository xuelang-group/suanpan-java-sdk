package com.xuelang.mqstream.message.arguments;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xuelang.mqstream.handler.DefaultMessageRecvHandler;
import com.xuelang.mqstream.handler.annotation.BussinessListenerMapping;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/6 22:03
 * @Description:
 */
@Data
public class EventType extends BaseType {

    private String event = "empty_event";

    private JSONObject data;

    public EventType(Map<String, String> message) {
        super(message);

        String value = message.get(super.getInput());

        if (StringUtils.isNotBlank(value)) {
            JSONObject parseObject = JSON.parseObject(value);

            this.event = parseObject.getString("event");
            this.data = parseObject.getJSONObject("data");
        }
    }

    @Override
    public Boolean isAsyncDealMessage(Map<BussinessListenerMapping, DefaultMessageRecvHandler.DealMsgInvokeObj> mappingCache) {
        for (Map.Entry<BussinessListenerMapping, DefaultMessageRecvHandler.DealMsgInvokeObj> entry : mappingCache.entrySet()) {
            if ((StringUtils.isBlank(entry.getKey().input()) || entry.getKey().input().equals(super.getInput()))
                    && entry.getKey().event().equals(this.event)) {
                return entry.getKey().async();
            }
        }

        return true;
    }

    @Override
    public void dealMessageInvoke(Map<BussinessListenerMapping, DefaultMessageRecvHandler.DealMsgInvokeObj> mappingCache) {
        BussinessListenerMapping listenerMapping = null;
        DefaultMessageRecvHandler.DealMsgInvokeObj dealMsgInvokeObj = null;

        for (Map.Entry<BussinessListenerMapping, DefaultMessageRecvHandler.DealMsgInvokeObj> entry : mappingCache.entrySet()) {
            if ((StringUtils.isBlank(entry.getKey().input()) || entry.getKey().input().equals(super.getInput()))
                    && entry.getKey().event().equals(this.event)) {
                dealMsgInvokeObj = entry.getValue();
                listenerMapping = entry.getKey();
                break;
            }
        }
        super.dealMessageInvoke(listenerMapping, dealMsgInvokeObj);
    }
}
