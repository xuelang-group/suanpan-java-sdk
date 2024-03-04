package com.xuelang.suanpan.stream;

import com.xuelang.suanpan.common.BaseSpDomainEntity;
import com.xuelang.suanpan.common.ProxrConnectionParam;

public class StreamImpl extends BaseSpDomainEntity implements IStream {

    private StreamImpl() {
        super();
    }

    private StreamImpl(ProxrConnectionParam proxrConnectionParam) {
        super(proxrConnectionParam);

    }


    @Override
    public void pub(){
        System.out.println("invoke stream pub func");
    }


}
