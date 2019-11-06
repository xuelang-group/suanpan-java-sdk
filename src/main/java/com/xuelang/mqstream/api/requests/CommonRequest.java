package com.xuelang.mqstream.api.requests;

import com.xuelang.mqstream.config.GlobalConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/4 15:16
 * @Description:
 */
@Slf4j
public abstract class CommonRequest {

    /**
     * 数据加密
     *
     * @param key
     * @param data
     * @return
     */
    private String signatureV1(String key, String data) {
        byte[] result = null;
        try {
            String HMAC_SHA1_ALGORITHM = "HmacSHA1";
            SecretKeySpec signinKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signinKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            result = Base64.getEncoder().encode(rawHmac);
            return new String(result);
        } catch (Exception e) {
            log.error("hmac获取失败", e);
        }
        return null;
    }

    /**
     * 默认请求头
     *
     * @return
     */
    private Map<String, String> defaultHeaders() {
        if (null == GlobalConfig.userIdHeaderField) {
            throw new RuntimeException("Suanpan API call Error: userIdHeaderField not set");
        }
        Map<String, String> headers = new HashMap<>();
        headers.put(GlobalConfig.userIdHeaderField, GlobalConfig.userId);
        headers.put(GlobalConfig.userSignatureHeaderField, signatureV1(GlobalConfig.accessSecret, GlobalConfig.userId));
        headers.put(GlobalConfig.userSignVersionHeaderField, "v1");

        return headers;
    }

    public String request(Request request) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        Request.Builder builder = request.newBuilder();

        defaultHeaders().forEach((k, v) -> {
            builder.addHeader(k, v);
        });

        request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String body = response.body().string();
                return body;
            }
        } catch (Exception e) {
            log.error("请求失败", e);
        } finally {
            client.connectionPool().evictAll();
        }
        return null;
    }

    /**
     * get请求
     *
     * @param path
     * @param args
     * @return
     */
    public String get(String path, Map<String, String> args) {

        HttpUrl.Builder builder = HttpUrl.parse(getUrl(path)).newBuilder();

        if (null != args && args.size() > 0) {
            args.forEach((k, v) -> {
                builder.addQueryParameter(k, v);
            });
        }

        HttpUrl httpUrl = builder.build();

        Request request = new Request.Builder()
                .url(httpUrl)
                .get()
                .build();

        return request(request);
    }

    /**
     * post请求-form
     *
     * @param path
     * @param args
     * @return
     */
    public String post(String path, Map<String, String> args) {

        FormBody.Builder builder = new FormBody.Builder();

        if (null != args && args.size() > 0) {
            args.forEach((k, v) -> {
                builder.add(k, v);
            });
        }

        RequestBody body = builder.build();

        Request request = new Request.Builder()
                .url(getUrl(path))
                .post(body)
                .build();

        return request(request);
    }

    /**
     * post请求-json
     *
     * @param path
     * @param json
     * @return
     */
    public String post(String path, String json) {
        RequestBody body = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);

        Request request = new Request.Builder()
                .url(getUrl(path))
                .post(body)
                .build();

        return request(request);
    }

    public abstract String getUrl(String path);

}
