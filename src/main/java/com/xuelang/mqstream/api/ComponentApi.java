package com.xuelang.mqstream.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xuelang.mqstream.api.requests.AffinityRequest;
import com.xuelang.mqstream.api.response.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/4 16:38
 * @Description:
 */
public class ComponentApi {

    private AffinityRequest request = new AffinityRequest();

    public List<Component> listComponents(Integer limit) {
        limit = limit == null ? 9999 : limit;

        Map<String, Object> param = new HashMap<>();
        param.put("limit", limit);

        String content = request.post("/component/list", JSON.toJSONString(param));

        List<Component> components = new ArrayList<>();

        if (StringUtils.isNotBlank(content)) {
            JSONObject jsonObject = JSON.parseObject(content);

            JSONArray list = jsonObject.getJSONArray("list");

            components = list.toJavaList(Component.class);
        }

        return components;
    }

    public String shareComponent(Integer componentId, String userId, String name) {

        Map<String, Object> param = new HashMap<>();
        param.put("id", componentId);
        param.put("targetUserId", userId);
        param.put("name", name);

        String content = request.post("/component/share", JSON.toJSONString(param));

        if (StringUtils.isNotBlank(content)) {
            JSONObject jsonObject = JSON.parseObject(content);
            return jsonObject.getString("id");
        }

        return null;
    }
}
