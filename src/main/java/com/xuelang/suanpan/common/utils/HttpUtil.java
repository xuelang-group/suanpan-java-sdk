package com.xuelang.suanpan.common.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpUtil {
    static class InstanceHolder {
        private static final OkHttpClient instance = createClient();
    }

    private static OkHttpClient getClient() {
        return InstanceHolder.instance;
    }

    private static OkHttpClient createClient() {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.callTimeout(5000, TimeUnit.MILLISECONDS).connectionPool(
                new ConnectionPool(5,3000L, TimeUnit.MILLISECONDS));
        return httpClientBuilder.build();
    }


    public static JSONObject sendGet(String protocol, String host, Integer port, String api, Map<String, String> headers, Map<String, Object> param) {
        OkHttpClient client = getClient();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(String.format(protocol + "://%s:%s/%s", host, port, api));
        JSONObject responseJSON = null;

        if (param != null && param.keySet().size() > 0) {
            stringBuffer.append("?");
            int index = 1;
            for (String key : param.keySet()) {
                if (param.get(key) != null) {
                    stringBuffer.append(key + "=" + param.get(key));
                    if (index < param.size()) {
                        stringBuffer.append("&");
                    }
                }
                index++;
            }
        }

        String url = stringBuffer.toString();
        Request.Builder builder = new Request.Builder()
                .get()
                .url(url);
        if (headers != null && !headers.isEmpty()) {
            headers.entrySet().stream().forEach(entry -> {
                builder.addHeader(entry.getKey(), entry.getValue());
            });
        }

        Request request = builder.build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String responseStr = responseBody.string();
                    responseJSON = JSON.parseObject(responseStr);
                }
            } else {
                response.close();
                Objects.requireNonNull(response.body()).close();
            }
        } catch (ConnectException e) {
            log.warn(String.format("http client connect error: {}, {}", e.getCause().getMessage(), e.getMessage()));
        } catch (IOException e) {
            log.warn(String.format("{} 请求失败: {}", url, e.getMessage()));
        }

        return responseJSON;
    }


    public static JSONObject sendPost(String protocol, String host, Integer port, String api, Map<String, String> headers, Object param) {
        OkHttpClient client = getClient();

        String url = String.format(protocol + "://%s:%s/%s", host, port, api);
        MediaType mediaType = MediaType.parse("application/json");
        String jsonBody = JSON.toJSONString(param, JSONWriter.Feature.WriteNulls);  // 将参数转换为 JSON 字符串
        RequestBody requestBody = RequestBody.create(mediaType, jsonBody);

        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/json")
                .method("POST", requestBody)
                .url(url)
                .build();

        if (headers != null && !headers.isEmpty()) {
            headers.entrySet().stream().forEach(entry -> {
                request.newBuilder().addHeader(entry.getKey(), entry.getValue());
            });
        }

        JSONObject responseJSON = null;
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String responseStr = responseBody.string();
                    responseJSON = JSON.parseObject(responseStr);
                }
            } else {
                response.close();
                Objects.requireNonNull(response.body()).close();
            }
        } catch (ConnectException e) {
            log.warn(String.format("HTTP client connect error: %s", e.getMessage()), e);
        } catch (IOException e) {
            log.warn(String.format("Failed to send POST request to %s: %s", url, e.getMessage()), e);
        }

        return responseJSON;
    }

    public static String signature(String secret, String data){
        try {
            Mac sha1Hmac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            sha1Hmac.init(secretKey);
            byte[] result = sha1Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return new String(Base64.getEncoder().encode(result));
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC", e);
        }
    }
}
