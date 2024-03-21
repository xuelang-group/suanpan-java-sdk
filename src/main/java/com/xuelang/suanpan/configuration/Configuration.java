package com.xuelang.suanpan.configuration;

import com.xuelang.suanpan.entities.BaseSpDomainEntity;
import com.xuelang.suanpan.entities.ProxrConnectionParam;

public class Configuration extends BaseSpDomainEntity {


    private Configuration() {

    }

    private Configuration(ProxrConnectionParam proxrConnectionParam) {
        super(proxrConnectionParam);
    }

}
