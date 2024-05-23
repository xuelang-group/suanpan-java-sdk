package com.xuelang.suanpan.configuration;

import com.xuelang.suanpan.common.entities.BaseSpDomainEntity;
import com.xuelang.suanpan.common.entities.ProxrConnectionParam;

public class Configuration extends BaseSpDomainEntity {


    private Configuration() {

    }

    private Configuration(ProxrConnectionParam proxrConnectionParam) {
        super(proxrConnectionParam);
    }

    public Object get(String key) {


        return null;
    }

    public void put(String key, Object value) {
        // TODO: 2024/3/22 调用配置中心服务存储配置
    }
}
