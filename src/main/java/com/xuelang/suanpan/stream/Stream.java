package com.xuelang.suanpan.stream;

import com.xuelang.suanpan.entities.BaseSpDomainEntity;
import com.xuelang.suanpan.entities.ProxrConnectionParam;
import com.xuelang.suanpan.configuration.ConfigurationKeys;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.stream.client.AbstractMqClient;
import com.xuelang.suanpan.stream.client.RedisMqClient;
import com.xuelang.suanpan.stream.handler.HandlerProxy;
import com.xuelang.suanpan.stream.handler.HandlerRequest;
import com.xuelang.suanpan.stream.message.StreamContext;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Stream extends BaseSpDomainEntity implements IStream {
    private AbstractMqClient mqClient;
    private HandlerProxy proxy;

    private Stream() {
        super();
        this.proxy = new HandlerProxy();
        mqClient = createMqClient(ConstantConfiguration.getReceiveQueue(), null, null, false, null);
        mqClient.infiniteConsume();
    }

    private Stream(ProxrConnectionParam proxrConnectionParam) {
        super(proxrConnectionParam);
        mqClient = createMqClient(ConstantConfiguration.getReceiveQueue(), null, null, false, null);
        mqClient.infiniteConsume();
    }

    @Override
    public String publish(StreamContext streamContext) throws NullPointerException {
        Objects.requireNonNull(streamContext, "stream context param can not be null");
        return mqClient.publish(streamContext);
    }

    @Override
    public HandlerRequest polling(long timeout, TimeUnit unit) {
        return mqClient.polling(timeout, unit);
    }

    private AbstractMqClient createMqClient(String queue, @Nullable String group, @Nullable String consumedMsgId,
                                            boolean isNoAck, @Nullable Long blockTimeout) {
        queue = Objects.requireNonNull(queue, "queue can not be null");
        AbstractMqClient abstractMqClient = null;
        if ("redis".equals(ConstantConfiguration.get(ConfigurationKeys.mqTypeKey, "redis"))) {
            abstractMqClient = new RedisMqClient(proxy);
            ((RedisMqClient) abstractMqClient).setQueue(queue);
            ((RedisMqClient) abstractMqClient).setNoAck(isNoAck);
            if (StringUtils.isNotBlank(group)) {
                ((RedisMqClient) abstractMqClient).setGroup(group);
            }
            if (StringUtils.isNotBlank(consumedMsgId)) {
                ((RedisMqClient) abstractMqClient).setConsumedMessageId(consumedMsgId);
            }
            if (blockTimeout != null) {
                ((RedisMqClient) abstractMqClient).setRestartDelay(blockTimeout);
            }

            ((RedisMqClient) abstractMqClient).initConsumerGroup();
            return abstractMqClient;
        }
        throw new RuntimeException("current not supported mq type");
    }

}
