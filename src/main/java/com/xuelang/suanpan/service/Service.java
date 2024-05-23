package com.xuelang.suanpan.service;

import com.xuelang.suanpan.common.entities.BaseSpDomainEntity;
import com.xuelang.suanpan.common.entities.connection.Connection;
import com.xuelang.suanpan.common.entities.enums.ProtocolType;
import com.xuelang.suanpan.common.entities.service.ServiceApi;
import com.xuelang.suanpan.common.entities.service.ServiceInfo;
import com.xuelang.suanpan.common.entities.service.ServiceProvider;
import com.xuelang.suanpan.common.utils.ParameterUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Service extends BaseSpDomainEntity implements IService {
    private Map<Integer, ServiceProvider> serviceProviderCache = new ConcurrentHashMap<>();

    private Service() {
    }

    @Override
    public ServiceProvider discover(Integer connectionId) {
        return serviceProviderCache.compute(connectionId, (id, existedServiceProvider) -> {
            if (existedServiceProvider != null) {
                return existedServiceProvider;
            }

            Connection connection = ParameterUtil.getInvokeConnection(connectionId);
            //app-${appId}-${nodeId}
            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setConnection(connection);

            // TODO: 2024/5/7 通过配置中心获取connection关联的下游服务信息
            return serviceProvider;
        });
    }

    @Override
    public void register(ProtocolType protocolType, Class<?> clazz, List<Method> methods) {
        List<ServiceApi> apiList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(methods)) {
            methods.stream().forEach(method -> {
                ServiceApi api = new ServiceApi();
                api.setFuncName(method.getName());
                api.setParameters(method.getParameterTypes());
                apiList.add(api);
            });
        }

        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setNodeId(ParameterUtil.getCurrentNodeId());
        serviceInfo.setProtocolType(protocolType);
        serviceInfo.setClazz(clazz);
        serviceInfo.setApiList(apiList);

        // TODO: 2024/5/8 往配置中心中注册服务信息
    }
}
