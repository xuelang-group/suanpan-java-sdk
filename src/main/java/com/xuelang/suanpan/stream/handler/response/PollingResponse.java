package com.xuelang.suanpan.stream.handler.response;

import com.xuelang.suanpan.common.entities.enums.NodeReceiveMsgType;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.stream.handler.InPortData;
import com.xuelang.suanpan.stream.message.Header;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class PollingResponse {
    private Header header;
    private List<InPortData> data;

    public void setHeader(Header header) {
        this.header = header;
    }

    public void setData(List<InPortData> data) {
        this.data = data;
    }

    public Header getHeader() {
        return header;
    }

    public Object getData() {
       if(NodeReceiveMsgType.async.equals(ConstantConfiguration.getReceiveMsgType())){
           if (!CollectionUtils.isEmpty(data)){
               return data.get(0);
           }
        }

        return data;
    }
}
