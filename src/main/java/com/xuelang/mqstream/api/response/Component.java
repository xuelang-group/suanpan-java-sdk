package com.xuelang.mqstream.api.response;

import lombok.Data;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/4 16:59
 * @Description:
 */

@Data
public class Component {

    private Integer id;

    private String name;

    private String type;

    private String tags;

    private Integer dir;

    private String user_id;

    private String gmt_create;

    private String gmt_modified;
}
