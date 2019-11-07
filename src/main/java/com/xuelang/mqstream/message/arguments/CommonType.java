package com.xuelang.mqstream.message.arguments;

import com.xuelang.mqstream.handler.DefaultMessageRecvHandler;
import com.xuelang.mqstream.handler.annotation.BussinessListenerMapping;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/7 09:00
 * @Description:普通类型
 */
@Data
@Slf4j
public class CommonType extends BaseType {

    CommonType(Map<String, String> message) {
        super(message);
    }

    /**
     * 判断该消息是否需要异步处理
     *
     * @param mappingCache
     * @return
     */
    @Override
    public Boolean isAsyncDealMessage(Map<BussinessListenerMapping, DefaultMessageRecvHandler.DealMsgInvokeObj> mappingCache) {

        for (Map.Entry<BussinessListenerMapping, DefaultMessageRecvHandler.DealMsgInvokeObj> entry : mappingCache.entrySet()) {
            if (StringUtils.isBlank(entry.getKey().input()) || entry.getKey().input().equals(super.getInput())) {
                return entry.getKey().async();
            }
        }

        return true;
    }

    /**
     * 消息处理
     *
     * @param mappingCache
     */
    @Override
    public void dealMessageInvoke(Map<BussinessListenerMapping, DefaultMessageRecvHandler.DealMsgInvokeObj> mappingCache) {
        BussinessListenerMapping listenerMapping = null;
        DefaultMessageRecvHandler.DealMsgInvokeObj dealMsgInvokeObj = null;

        for (Map.Entry<BussinessListenerMapping, DefaultMessageRecvHandler.DealMsgInvokeObj> entry : mappingCache.entrySet()) {
            if (StringUtils.isBlank(entry.getKey().input()) || entry.getKey().input().equals(super.getInput())) {
                dealMsgInvokeObj = entry.getValue();
                listenerMapping = entry.getKey();
                break;
            }
        }

        dealMessageInvoke(listenerMapping, dealMsgInvokeObj);
    }

    @Override
    public Object[] getArgs() {
        return new Object[]{this};
    }
}
