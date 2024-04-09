package com.xuelang.suanpan.stream.message;

import com.xuelang.suanpan.common.entities.io.InPort;

import java.util.Map;

public class InflowMessage {
    private Context context;
    private Map<InPort, Object> data;


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Object getData() {
        // TODO: 2024/3/25 根据输入端口数据类型对data进行转换
        return data;
    }

    public void setData(Map<InPort, Object> data) {
        this.data = data;
    }
}
