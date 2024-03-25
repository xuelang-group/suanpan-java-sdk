package com.xuelang.suanpan.stream.handler.request;

import com.xuelang.suanpan.stream.handler.InPortData;
import com.xuelang.suanpan.stream.message.Header;

import java.util.List;

public class HandlerRequest {
    private Header header;
    private List<InPortData> data;


    public Header getHeader() {
        return header;
    }


    public Object getData() {
        // TODO: 2024/3/25 根据输入端口数据类型对data进行转换
        return data;
    }


    public void setHeader(Header header) {
        this.header = header;
    }


    public void setData(List<InPortData> data) {
        this.data = data;
    }
}
