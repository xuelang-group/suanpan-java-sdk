package com.xuelang.suanpan;

import com.xuelang.suanpan.client.ISpClient;
import com.xuelang.suanpan.client.SpClientFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * 整体sdk的bootstrap类，用于初始化sdk
 */
@Slf4j
public class SpClientBootStrap {

    static {
        // todo 执行初始化逻辑
        ISpClient spClient = SpClientFactory.create();
        spClient.stream();

    }


}
