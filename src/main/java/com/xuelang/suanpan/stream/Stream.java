package com.xuelang.suanpan.stream;

import com.xuelang.suanpan.common.entities.BaseSpDomainEntity;
import com.xuelang.suanpan.common.entities.ProxrConnectionParam;
import com.xuelang.suanpan.common.entities.io.InPort;
import com.xuelang.suanpan.common.exception.GlobalExceptionType;
import com.xuelang.suanpan.common.exception.StreamGlobalException;
import com.xuelang.suanpan.configuration.ConfigurationKeys;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.stream.client.AbstractMqClient;
import com.xuelang.suanpan.stream.client.RedisMqClient;
import com.xuelang.suanpan.stream.handler.AbstractStreamHandler;
import com.xuelang.suanpan.stream.handler.HandlerMethodEntry;
import com.xuelang.suanpan.stream.handler.HandlerProxy;
import com.xuelang.suanpan.stream.handler.HandlerRegistry;
import com.xuelang.suanpan.stream.message.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Stream extends BaseSpDomainEntity implements IStream {
    private AbstractMqClient mqClient;
    private HandlerProxy proxy;
    private HandlerRegistry registry;

    private Stream() {
        super();
        this.proxy = new HandlerProxy();
        registry = HandlerRegistry.getInstance();
        mqClient = createMqClient(ConstantConfiguration.getReceiveQueue(), null, null, false, null);
        mqClient.infiniteConsume();
        notifySubscribeReady();
    }

    private Stream(ProxrConnectionParam proxrConnectionParam) {
        super(proxrConnectionParam);
        registry = HandlerRegistry.getInstance();
        mqClient = createMqClient(ConstantConfiguration.getReceiveQueue(), null, null, false, null);
        mqClient.infiniteConsume();
        notifySubscribeReady();
    }

    @Override
    public String publish(OutflowMessage outflowMessage, Context context) throws StreamGlobalException{
        if (StringUtils.isBlank(context.getMessageId())) {
            throw new StreamGlobalException(GlobalExceptionType.IllegalStreamMessage);
        }
        if (outflowMessage == null) {
            throw new StreamGlobalException(GlobalExceptionType.IllegalStreamMessage);
        }

        outflowMessage.mergeOutPortData(ConstantConfiguration.getOutPorts());
        context.getExt().append(ConstantConfiguration.getNodeId());

        MetaOutflowMessage metaOutflowMessage = new MetaOutflowMessage();
        MetaContext metaContext = new MetaContext();
        metaContext.setExtra(context.getExt());
        metaContext.setRequestId(context.getMessageId());
        metaOutflowMessage.setMetaContext(metaContext);
        metaOutflowMessage.setOutPortDataMap(outflowMessage.getOutPortDataMap());
        return mqClient.publish(metaOutflowMessage);
    }


    @Override
    public synchronized InflowMessage polling(long timeoutMillis) {
        return mqClient.polling(timeoutMillis);
    }

    @Override
    public void subscribe(Integer inPortNum, AbstractStreamHandler handler) throws StreamGlobalException {
        InPort inPort = InPort.bind(inPortNum);
        HandlerMethodEntry entry = new HandlerMethodEntry();
        entry.setInstance(handler);
        Method method;
        try {
            method = handler.getClass().getDeclaredMethod("handle", InflowMessage.class);
        } catch (NoSuchMethodException e) {
            log.error("get async handler declared method error", e);
            throw new StreamGlobalException(GlobalExceptionType.NoSuchMethodException, e);
        }
        method.setAccessible(true);
        entry.setMethod(method);
        registry.regist(inPort, entry);
        notifySubscribeReady();
    }

    @Override
    public void subscribe(AbstractStreamHandler handler) throws StreamGlobalException {
        HandlerMethodEntry entry = new HandlerMethodEntry();
        entry.setInstance(handler);
        Method method;
        try {
            method = handler.getClass().getDeclaredMethod("handle", InflowMessage.class);
        } catch (NoSuchMethodException e) {
            log.error("get async handler declared method error", e);
            throw new StreamGlobalException(GlobalExceptionType.NoSuchMethodException, e);
        }
        method.setAccessible(true);
        entry.setMethod(method);
        registry.regist(null, entry);
        notifySubscribeReady();
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

    private void notifySubscribeReady() {
        if (!registry.isEmpty()) {
            mqClient.readyConsume();
        }
    }
}
