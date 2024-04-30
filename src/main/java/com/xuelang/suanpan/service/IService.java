package com.xuelang.suanpan.service;

import com.xuelang.suanpan.common.entities.io.Outport;

public interface IService {

    StreamResponse innerInvoke(Outport outport, StreamRequest request);

}
