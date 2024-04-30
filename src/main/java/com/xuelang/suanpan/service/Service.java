package com.xuelang.suanpan.service;

import com.xuelang.suanpan.common.entities.BaseSpDomainEntity;
import com.xuelang.suanpan.common.entities.io.Outport;

public class Service extends BaseSpDomainEntity implements IService{

    private Service() {
    }


    @Override
    public StreamResponse innerInvoke(Outport outport, StreamRequest request){
        return null;
    }
}
