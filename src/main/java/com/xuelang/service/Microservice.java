package com.xuelang.service;

import com.alibaba.fastjson.JSON;
import com.xuelang.mqstream.config.GlobalConfig;
import com.xuelang.mqstream.entity.MicroserviceLookupResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

@Slf4j
public class Microservice {

    public static String lookup(String portKey) throws Exception {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl
                .parse("http://app-".concat(GlobalConfig.appId).concat(":8001/internal/microservice/lookup"))
                .newBuilder();
        urlBuilder.addQueryParameter("portKey", portKey);
        urlBuilder.addQueryParameter("nodeId", GlobalConfig.nodeId);
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("查找微服务节点失败");
        }
        String body = response.body().string();
        MicroserviceLookupResponse lookupResponse = JSON.parseObject(body, MicroserviceLookupResponse.class);
        return lookupResponse.getData().getAddress();
    }
}