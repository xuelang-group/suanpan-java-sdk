package com.xuelang.suanpan.stream.message;

import com.alibaba.fastjson2.JSON;
import com.xuelang.suanpan.common.entities.io.Inport;

import java.util.Map;

public class InflowMessage {
    private Context context;
    private Map<Inport, Object> data;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Object getData(Integer inPortIndex) {
        // TODO: 2024/3/25 根据输入端口数据类型对data进行转换
        if (data == null || data.isEmpty()){
            return null;
        }

        Inport inport = Inport.bind(inPortIndex);
        return data.get(inport);
    }

    public void setData(Map<Inport, Object> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("context=").append(context!=null? JSON.toJSONString(context):"").append(",")
                .append("data={");
        if (data == null || data.isEmpty()){
            sb.append("}");
        } else{
            data.entrySet().stream().forEach(entry->{
                sb.append(entry.getKey().getUuid()).append("=").append(entry.getValue().toString()).append(",");
            });

            sb.deleteCharAt(sb.length()-1);
            sb.append("}");
        }


        return sb.toString();
    }
}
