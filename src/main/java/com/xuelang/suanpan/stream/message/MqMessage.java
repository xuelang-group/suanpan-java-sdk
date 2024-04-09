package com.xuelang.suanpan.stream.message;

import com.alibaba.fastjson2.JSONObject;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.common.entities.io.InPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 从消息队列中消费消息的响应
 */
public class MqMessage {
    private static final String REQUEST_ID_KEY = "request_id";
    private static final String REQUEST_ID_ALIAS_KEY = "id";
    private static final String EXTRA_KEY = "extra";

    /**
     * 消费的队列名称
     */
    private String queue;

    /**
     * 消费到的消息列表
     */
    private List<MetaInflowMessage> metaInflowMessages;

    public MqMessage(String queue, List<MetaInflowMessage> metaInflowMessages) {
        this.queue = queue;
        this.metaInflowMessages = metaInflowMessages;
    }

    public String getQueue() {
        return queue;
    }

    public List<MetaInflowMessage> getMessages() {
        return metaInflowMessages;
    }

    private static void parseMessage(List metaMessageList, List<MetaInflowMessage> metaInflowMessages) {
        String messageId = (String) metaMessageList.get(0);
        Map<String, Object> tmpContentMap = new HashMap<>();
        List contentList = (List) metaMessageList.get(1);
        for (int i = 0; i < contentList.size() - 1; i += 2) {
            String key = (String) contentList.get(i);
            Object value = contentList.get(i + 1);
            tmpContentMap.put(key, value);
        }


        MetaContext metaContext = new MetaContext();
        metaContext.setMessageId(messageId);
        if (tmpContentMap.get(REQUEST_ID_KEY) != null) {
            metaContext.setRequestId((String) tmpContentMap.get(REQUEST_ID_KEY));
            tmpContentMap.remove(REQUEST_ID_KEY);
        }
        if (tmpContentMap.get(REQUEST_ID_ALIAS_KEY) != null) {
            metaContext.setRequestId((String) tmpContentMap.get(REQUEST_ID_ALIAS_KEY));
            tmpContentMap.remove(REQUEST_ID_ALIAS_KEY);
        }

        if (tmpContentMap.get(EXTRA_KEY) != null) {
            Extra extra = JSONObject.parseObject((String) tmpContentMap.get(EXTRA_KEY), Extra.class);
            if (extra == null) {
                extra = new Extra();
            }
            extra.append(ConstantConfiguration.getNodeId());
            metaContext.setExtra(extra);
            tmpContentMap.remove(EXTRA_KEY);
        } else {
            Extra extra = new Extra();
            extra.append(ConstantConfiguration.getNodeId());
            metaContext.setExtra(extra);
        }


        MetaInflowMessage metaInflowMessage = new MetaInflowMessage();
        metaInflowMessage.setMetaContext(metaContext);

        tmpContentMap.keySet().stream().forEach(key -> {
            InPort inPort;
            if ((inPort = ConstantConfiguration.getByInPortUuid(key)) != null) {
                metaInflowMessage.append(inPort, tmpContentMap.get(key));
            }
        });

        metaInflowMessages.add(metaInflowMessage);
    }

    /**
     * 把从redis stream中消费到的原始消息转成streamMessage
     * "xreadgroup group default fd70cbd0d54d11ee91d66b2b05acfbf8 count 2 block 0 streams mq-master-1000184-55158-fd70cbd0d54d11ee91d66b2b05acfbf8 >"
     *
     * @param metaMsg redis stream中的原始消息
     *                1) 1) "mq-master-1000184-55158-fd70cbd0d54d11ee91d66b2b05acfbf8"
     *                2) 1) 1) "1709881691087-0"
     *                2)  1) "id"
     *                2) "6ad6e766998b469b8b7fe331d38e9ecf"
     *                3) "in1"
     *                4) "{\"count\": 36, \"time\": \"2024-03-08 15:08:12\"}"
     *                5) "in1OutType"
     *                6) "data"
     *                7) "in1OutSubType"
     *                8) "all"
     *                9) "extra"
     *                10) "{\"global\":{}}"
     *                2) 1) "1709881691087-1"
     *                2)  1) "id"
     *                2) "6ad6e766998b469b8b7fe331d38e9ecf"
     *                3) "in2"
     *                4) "{\"count\": 36, \"time\": \"2024-03-08 15:08:12\"}"
     *                5) "in2OutType"
     *                6) "data"
     *                7) "in2OutSubType"
     *                8) "all"
     *                9) "extra"
     *                10) "{\"global\":{}}"
     * @return StreamMessage
     */
    public static MqMessage convert(List metaMsg) {
        String queue = (String) metaMsg.get(0);
        List<MetaInflowMessage> metaInflowMessages = new ArrayList<>();

        List metaMessageList = (List) metaMsg.get(1);
        metaMessageList.forEach(metaMessage -> {
            parseMessage((List) metaMessage, metaInflowMessages);
        });
        return new MqMessage(queue, metaInflowMessages);
    }
}
