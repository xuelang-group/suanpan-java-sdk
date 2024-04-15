package com.xuelang.suanpan.stream;

import com.xuelang.suanpan.common.entities.BaseSpDomainEntity;
import com.xuelang.suanpan.common.entities.ProxrConnectionParam;
import com.xuelang.suanpan.common.entities.io.Inport;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

@Slf4j
public class Stream extends BaseSpDomainEntity implements IStream {
    /**
     * 消费者群组
     */
    private String group = "default";
    /**
     * 消费者名称
     */
    private String name = "unknown";
    /**
     * 是否无ACK，默认false
     */
    private boolean noAck = false;

    /**
     * 上次消费到的消息id，如果使用">"则代表要消费最新的消息
     */
    private String consumedMessageId = ">";
    /**
     * 阻塞消费失败后，重新消费延时时间，单位毫秒
     */
    private long restartDelay = 1000L;


    private AbstractMqClient mqClient;
    private HandlerProxy proxy;
    private HandlerRegistry registry;

    private Stream() {
        super();
        this.proxy = new HandlerProxy();
        registry = HandlerRegistry.getInstance();
        mqClient = createMqClient(ConstantConfiguration.getReceiveQueue(), group, consumedMessageId, noAck, restartDelay);
        if (!registry.isEmpty()) {
            mqClient.consume();
        }
    }

    private Stream(ProxrConnectionParam proxrConnectionParam) {
        super(proxrConnectionParam);
        registry = HandlerRegistry.getInstance();
        mqClient = createMqClient(ConstantConfiguration.getReceiveQueue(), group, consumedMessageId, noAck, restartDelay);
        if (!registry.isEmpty()) {
            mqClient.consume();
        }
    }

    @Override
    public String publish(OutflowMessage outflowMessage, Context context) throws StreamGlobalException{
        if (StringUtils.isBlank(context.getMessageId())) {
            throw new StreamGlobalException(GlobalExceptionType.IllegalStreamMessage);
        }
        if (outflowMessage == null) {
            throw new StreamGlobalException(GlobalExceptionType.IllegalStreamMessage);
        }


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
    public InflowMessage polling(long timeoutMillis) {
        List<InflowMessage> result =  mqClient.polling(1, timeoutMillis);
        if (CollectionUtils.isEmpty(result)){
            return null;
        }

        return result.get(0);
    }

    @Override
    public List<InflowMessage> polling(int count, long timeoutMillis) {
        return mqClient.polling(count, timeoutMillis);
    }

    @Override
    public void subscribe(Integer inPortNum, AbstractStreamHandler handler) throws StreamGlobalException {
        Inport inPort = Inport.bind(inPortNum);
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
        mqClient.consume();
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
        mqClient.consume();
    }

    private AbstractMqClient createMqClient(String queue, @Nullable String group, @Nullable String consumedMsgId,
                                            boolean isNoAck, @Nullable Long restartDelay) {
        queue = Objects.requireNonNull(queue, "queue can not be null");
        AbstractMqClient abstractMqClient = null;
        if ("redis".equals(ConstantConfiguration.get(ConfigurationKeys.mqTypeKey, "redis"))) {
            abstractMqClient = new RedisMqClient(proxy, queue, group, consumedMsgId, isNoAck, restartDelay);
            return abstractMqClient;
        }
        throw new RuntimeException("current not supported mq type");
    }
}
