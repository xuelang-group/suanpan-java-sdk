package com.xuelang.suanpan.service;

import com.xuelang.suanpan.common.entities.enums.ProtocolType;
import com.xuelang.suanpan.common.entities.service.ServiceProvider;

import java.lang.reflect.Method;
import java.util.List;

public interface IService {


    ServiceProvider discover(Integer connectionId);

    /**
     * 向平台端注册服务
     * @param protocolType
     * @param clazz
     * @param methods
     */
    void register(ProtocolType protocolType, Class<?> clazz, List<Method> methods);

}
