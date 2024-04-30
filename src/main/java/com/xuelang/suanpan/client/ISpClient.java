package com.xuelang.suanpan.client;

import com.xuelang.suanpan.configuration.Configuration;
import com.xuelang.suanpan.event.Event;
import com.xuelang.suanpan.service.IService;
import com.xuelang.suanpan.state.State;
import com.xuelang.suanpan.stream.IStream;

public interface ISpClient {
    IStream stream();

    Configuration configuration();

    Event event();

    IService service();

    State state();
}
