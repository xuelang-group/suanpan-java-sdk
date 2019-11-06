package com.xuelang.mqstream.api.response;

import lombok.Data;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/4 18:00
 * @Description:
 */
@Data
public class Credentials {

    private String AccessKeySecret;

    private String AccessKeyId;

    private String Expiration;

    private String SecurityToken;
}
