package com.xuelang.suanpan.event;

import com.xuelang.suanpan.common.entities.BaseSpDomainEntity;
import com.xuelang.suanpan.common.entities.enums.EventLevel;
import com.xuelang.suanpan.common.utils.HttpUtil;
import com.xuelang.suanpan.common.utils.ParameterUtil;

public class Event extends BaseSpDomainEntity implements IEvent {

    private Event() {
    }

    @Override
    public void notify(EventLevel eventLevel, String title, String message) {
        String spHost = ParameterUtil.getSpSvc();
        Integer spPort = ParameterUtil.getSpPort();
        String protocol = ParameterUtil.getSpProtocol();
        String appId = ParameterUtil.getAppId();
        // TODO: 2024/5/10 发送事件到平台端
        HttpUtil.sendPost(protocol, spHost, spPort, "xxx/xxx/" + appId, null, null);
    }
}
