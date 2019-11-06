package com.xuelang.mqstream.storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/9/30 15:37
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectSummary {

    private String bucketName;

    private String key;
}
