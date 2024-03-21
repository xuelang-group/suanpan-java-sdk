package com.xuelang.suanpan.entities;

public abstract class BaseSpDomainEntity {

    // TODO: 2024/3/13   用于领域服务代理业务实现，通过单例websocket client连接算盘平台，代理领域服务数据
    //protected WebSocketClient webSocketClient;

    public BaseSpDomainEntity(){

    }

    public BaseSpDomainEntity(ProxrConnectionParam proxrConnectionParam){

    }

    /**
     * todo
     */
    // protected abstract void proxy();



}
