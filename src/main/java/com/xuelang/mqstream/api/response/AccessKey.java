package com.xuelang.mqstream.api.response;

import lombok.Data;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/4 17:55
 * @Description:
 */
@Data
public class AccessKey {

    private String Status;

    private String AccessKeySecret;

    private String AccessKeyId;

    private String CreateDate;
}
