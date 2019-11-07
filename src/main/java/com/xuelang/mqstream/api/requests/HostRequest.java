package com.xuelang.mqstream.api.requests;

import com.xuelang.mqstream.config.GlobalConfig;
import org.apache.commons.lang3.StringUtils;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/4 14:39
 * @Description:
 */
public class HostRequest extends CommonRequest {

    @Override
    public String getUrl(String path) {
        if (StringUtils.isBlank(GlobalConfig.host)) {
            throw new IllegalArgumentException("Suanpan API call Error: host not set");
        }

        String protocol = GlobalConfig.hostTls ? "https" : "http";
        return String.format("%s://%s%s", protocol, GlobalConfig.host, path);
    }
}
