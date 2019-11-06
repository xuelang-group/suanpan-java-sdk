package com.xuelang.mqstream.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xuelang.mqstream.api.requests.AffinityRequest;
import com.xuelang.mqstream.api.response.AccessKey;
import com.xuelang.mqstream.api.response.Credentials;
import org.apache.commons.lang3.StringUtils;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/4 17:53
 * @Description:
 */
public class OSSApi {

    private AffinityRequest request = new AffinityRequest();

    public AccessKey getAccessKey() {
        String content = request.get("/oss/ak", null);
        if (StringUtils.isNotBlank(content)) {
            JSONObject jsonObject = JSON.parseObject(content);
            return jsonObject.getObject("AccessKey", AccessKey.class);
        }
        return null;
    }

    public Credentials getToken() {
        String content = request.get("/oss/token", null);
        if (StringUtils.isNotBlank(content)) {
            JSONObject jsonObject = JSON.parseObject(content);
            return jsonObject.getObject("Credentials", Credentials.class);
        }
        return null;
    }
}
