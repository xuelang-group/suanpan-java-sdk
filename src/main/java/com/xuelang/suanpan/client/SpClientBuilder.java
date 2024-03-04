package com.xuelang.suanpan.client;

import com.xuelang.suanpan.common.BaseSpDomainEntity;
import com.xuelang.suanpan.common.ProxrConnectionParam;
import com.xuelang.suanpan.configuration.ConfigurationImpl;
import com.xuelang.suanpan.configuration.IConfiguration;
import com.xuelang.suanpan.event.EventImpl;
import com.xuelang.suanpan.event.IEvent;
import com.xuelang.suanpan.service.IService;
import com.xuelang.suanpan.service.ServiceImpl;
import com.xuelang.suanpan.state.IState;
import com.xuelang.suanpan.state.StateImpl;
import com.xuelang.suanpan.stream.IStream;
import com.xuelang.suanpan.stream.StreamImpl;

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
        private volatile IConfiguration configuration;
        private volatile IEvent event;
        private volatile IService service;
        private volatile IState state;
        private ProxrConnectionParam proxrConnectionParam;

        public SpClient() {
        }

        public SpClient(ProxrConnectionParam proxrConnectionParam) {
            this.proxrConnectionParam = proxrConnectionParam;
        }

        public IStream stream() {
            if (null == stream) {
                synchronized (this) {
                    if (null == stream) {
                        stream = createInstance(StreamImpl.class);
                    }
                }
            }

            return stream;
        }

        public IConfiguration configuration() {
            if (null == configuration) {
                synchronized (this) {
                    if (null == configuration) {
                        configuration = createInstance(ConfigurationImpl.class);
                    }
                }
            }

            return configuration;
        }

        public IEvent event() {
            if (null == event) {
                synchronized (this) {
                    if (null == event) {
                        event = createInstance(EventImpl.class);
                    }
                }
            }
            return event;
        }

        public IService service() {
            if (null == service) {
                synchronized (this) {
                    if (null == service) {
                        service = createInstance(ServiceImpl.class);
                    }
                }
            }
            return service;
        }

        public IState state() {
            if (null == state) {
                synchronized (this) {
                    if (null == state) {
                        state = createInstance(StateImpl.class);
                    }
                }
            }
            return state;
        }

        private <T extends BaseSpDomainEntity> T createInstance(Class<T> clazz) {
            try {
                Constructor<T> constructor = clazz.getDeclaredConstructor(proxrConnectionParam == null ? null : ProxrConnectionParam.class);
                constructor.setAccessible(true);
                return constructor.newInstance(proxrConnectionParam);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}


