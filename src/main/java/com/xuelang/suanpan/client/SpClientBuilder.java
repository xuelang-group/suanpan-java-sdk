package com.xuelang.suanpan.client;

import com.xuelang.suanpan.common.BaseSpDomainEntity;
import com.xuelang.suanpan.domain.proxr.ProxrConnectionParam;
import com.xuelang.suanpan.configuration.Configuration;
import com.xuelang.suanpan.event.Event;
import com.xuelang.suanpan.service.Service;
import com.xuelang.suanpan.state.State;
import com.xuelang.suanpan.stream.IStream;
import com.xuelang.suanpan.stream.Stream;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 提供静态build方法，用于构建spClient实例；
 * 对于用户组件开发者，需要通过该类提供的build方法创建spClient实例，用于和平台交互及流计算
 */
public class SpClientBuilder {

    private static volatile SpClient spClient;

    /**
     * 构建spClient实例，单例模式
     *
     * @return spClient单例对象
     */
    public static SpClient build() {
        if (null == spClient) {
            synchronized (SpClientBuilder.class) {
                if (null == spClient) {
                    spClient = new SpClient();
                }
            }
        }

        return spClient;
    }


    /**
     * 本地调试模式下，构建算盘平台的client
     *
     * @param proxrConnectionParam 算盘后面板，需要调试的组件的连接信息
     * @return spClient单例对象
     */
    public static SpClient buildDebugClient(ProxrConnectionParam proxrConnectionParam) {
        if (null == spClient) {
            synchronized (SpClientBuilder.class) {
                if (null == spClient) {
                    spClient = new SpClient(proxrConnectionParam);
                }
            }
        }

        return spClient;
    }

    private static class SpClient implements ISpClient {
        private volatile IStream stream;
        private volatile Configuration configuration;
        private volatile Event event;
        private volatile Service service;
        private volatile State state;
        private ProxrConnectionParam proxrConnectionParam;

        public SpClient() {
            if (null == configuration) {
                synchronized (this) {
                    if (null == configuration) {
                        configuration = createInstance(Configuration.class);
                    }
                }
            }
        }

        public SpClient(ProxrConnectionParam proxrConnectionParam) {
            this.proxrConnectionParam = proxrConnectionParam;
            if (null == configuration) {
                synchronized (this) {
                    if (null == configuration) {
                        configuration = createInstance(Configuration.class);
                    }
                }
            }
        }

        public IStream stream() {
            if (null == stream) {
                synchronized (this) {
                    if (null == stream) {
                        stream = createInstance(Stream.class);
                    }
                }
            }

            return stream;
        }

        public Configuration configuration() {
            return configuration;
        }

        @Override
        public Event event() {
            if (null == event) {
                synchronized (this) {
                    if (null == event) {
                        event = createInstance(Event.class);
                    }
                }
            }
            return event;
        }

        @Override
        public Service service() {
            if (null == service) {
                synchronized (this) {
                    if (null == service) {
                        service = createInstance(Service.class);
                    }
                }
            }
            return service;
        }

        @Override
        public State state() {
            if (null == state) {
                synchronized (this) {
                    if (null == state) {
                        state = createInstance(State.class);
                    }
                }
            }

            return state;
        }

        private <T extends BaseSpDomainEntity> T createInstance(Class<T> clazz) {
            try {
                Constructor<T> constructor;
                if (proxrConnectionParam == null){
                    constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    return constructor.newInstance();
                } else{
                    constructor = clazz.getDeclaredConstructor(ProxrConnectionParam.class);
                    constructor.setAccessible(true);
                    return constructor.newInstance(proxrConnectionParam);
                }


            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}


