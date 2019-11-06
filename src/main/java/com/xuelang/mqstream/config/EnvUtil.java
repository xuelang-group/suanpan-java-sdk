package com.xuelang.mqstream.config;

import org.apache.commons.lang3.StringUtils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/4 14:13
 * @Description:
 */
public class EnvUtil {

    private static String spParam = StringUtils.isNotBlank(System.getenv("SP_PARAM")) ? new String(Base64.getDecoder().decode(System.getenv("SP_PARAM"))) : null;

    private static Map<String, String> spParamMap = new HashMap<>();

    static {
        if (StringUtils.isNotBlank(spParam)) {
            String[] kvs = spParam.split("--");
            for (String kv : kvs) {
                if (StringUtils.isNotBlank(kv)) {
                    String[] formatKV = kv.split(" ");
                    String k = formatKV[0];
                    String v = formatKV[1].replaceAll("'", "");
                    spParamMap.put(k, v);
                }
            }
        }
    }

    /**
     * 获取系统变量值
     *
     * @param key
     * @param defaultValue
     * @param required
     * @return
     */
    public static String get(String key, String defaultValue, Boolean required) {
        String env = System.getenv(key);

        if (null == env) {
            String paramKey = key.substring(key.indexOf("_") + 1).toLowerCase();
            paramKey = paramKey.replaceAll("_", "-");
            env = spParamMap.get(paramKey);
        }

        if (null != defaultValue && env == null) {
            env = defaultValue;
        }

        if (null != required && required && env == null) {
            throw new IllegalArgumentException(String.format("No such env: %s", key));
        }

        return env;
    }

    public static String get(String key) {
        return get(key, null, null);
    }

    public static String get(String key, String defaultValue) {
        return get(key, defaultValue, null);
    }
}
