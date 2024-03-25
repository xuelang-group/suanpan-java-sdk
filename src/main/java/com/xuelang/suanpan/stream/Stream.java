package com.xuelang.suanpan.stream;

import com.xuelang.suanpan.common.entities.BaseSpDomainEntity;
import com.xuelang.suanpan.common.entities.ProxrConnectionParam;
import com.xuelang.suanpan.common.entities.io.OutPort;
import com.xuelang.suanpan.configuration.ConfigurationKeys;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.stream.client.AbstractMqClient;
import com.xuelang.suanpan.stream.client.RedisMqClient;
import com.xuelang.suanpan.stream.handler.HandlerProxy;
import com.xuelang.suanpan.stream.handler.response.PollingResponse;
import com.xuelang.suanpan.stream.message.Extra;
import com.xuelang.suanpan.stream.message.Header;
import com.xuelang.suanpan.stream.message.OutBoundMessage;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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
    public String publish(Map<OutPort, Object> data, @Nullable String requestId, @Nullable Long validitySeconds, @Nullable Extra extra) throws NullPointerException {
        Objects.requireNonNull(data, "stream context param can not be null");
        if (data.isEmpty()) {
            throw new IllegalArgumentException("data can not be empty");
        }

        if (validitySeconds != null && validitySeconds <= 0) {
            throw new IllegalArgumentException("validitySeconds can not be negative number!");
        }

        Header header = new Header();
        header.setSuccess(true);
        header.setRequestId(requestId == null ? UUID.randomUUID().toString() : requestId);
        if (extra == null) {
            extra = new Extra();
            extra.append(ConstantConfiguration.getNodeId());
        }
        header.setExtra(extra);
        if (validitySeconds != null && validitySeconds > 0) {
            header.refreshExpire(validitySeconds);
        }

        OutBoundMessage outBoundMessage = new OutBoundMessage();
        outBoundMessage.setHeader(header);
        outBoundMessage.setOutPortDataMap(data);
        return mqClient.publish(outBoundMessage);
    }

    @Override
    public PollingResponse polling(long timeout, TimeUnit unit) {
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
