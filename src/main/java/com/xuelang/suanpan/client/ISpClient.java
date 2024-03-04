package com.xuelang.suanpan.client;

import com.xuelang.suanpan.configuration.IConfiguration;
import com.xuelang.suanpan.event.IEvent;
import com.xuelang.suanpan.service.IService;
import com.xuelang.suanpan.state.IState;
import com.xuelang.suanpan.stream.IStream;

public interface ISpClient {
    IStream stream();

    IConfiguration configuration();

    IEvent event();

    IService service();

    IState state();
}
