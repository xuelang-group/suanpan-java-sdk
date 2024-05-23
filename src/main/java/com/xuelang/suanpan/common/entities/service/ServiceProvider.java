package com.xuelang.suanpan.common.entities.service;

import com.xuelang.suanpan.common.entities.connection.Connection;
import com.xuelang.suanpan.common.entities.service.ServiceInfo;

public class ServiceProvider {
    private Connection connection;
    private ServiceInfo serviceInfo;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
}
