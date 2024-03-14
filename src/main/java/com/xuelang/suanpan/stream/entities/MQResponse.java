package com.xuelang.suanpan.stream.entities;

import com.alibaba.fastjson.JSONObject;
import com.xuelang.suanpan.configuration.SpEnv;
import com.xuelang.suanpan.domain.io.InPort;
import com.xuelang.suanpan.stream.dto.MessageSchemaConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 从消息队列中消费消息的响应
 */
public class MQResponse {
    /**
     * 消费的队列名称
     */
    private String queue;

    /**
     * 消费到的消息列表
     */
    private List<Message> messages;

    public MQResponse(String queue, List<Message> messages) {
        this.queue = queue;
        this.messages = messages;
    }

    public String getQueue() {
        return queue;
    }

    public List<Message> getMessages() {
        return messages;
    }

    private static void parseMessage(List metaMessageList, List<Message> messages) {
        String messageId = (String) metaMessageList.get(0);
        Map<String, Object> tmpContentMap = new HashMap<>();
        List contentList = (List) metaMessageList.get(1);
        for (int i = 0; i < contentList.size() - 1; i += 2) {
            String key = (String) contentList.get(i);
            Object value = contentList.get(i + 1);
            tmpContentMap.put(key, value);
        }

        Message message = new Message();
        message.setMessageId(messageId);
        if(tmpContentMap.get(MessageSchemaConstants.RECV_TYPE_KEY) != null){
            message.setReceiveMsgType((String) tmpContentMap.get(MessageSchemaConstants.RECV_TYPE_KEY));
            tmpContentMap.remove(MessageSchemaConstants.RECV_TYPE_KEY);
        }
        if(tmpContentMap.get(MessageSchemaConstants.REQUEST_ID_KEY) != null){
            message.setRequestId((String) tmpContentMap.get(MessageSchemaConstants.REQUEST_ID_KEY));
            tmpContentMap.remove(MessageSchemaConstants.REQUEST_ID_KEY);
        }
        if(tmpContentMap.get(MessageSchemaConstants.EXTRA_KEY) != null){
            message.setExtra(JSONObject.parseObject((String) tmpContentMap.get(MessageSchemaConstants.EXTRA_KEY), Extra.class));
            tmpContentMap.remove(MessageSchemaConstants.EXTRA_KEY);
        }
        if(tmpContentMap.get(MessageSchemaConstants.SUCCESS_KEY) != null){
            message.setSuccess((Boolean) tmpContentMap.get(MessageSchemaConstants.SUCCESS_KEY));
            tmpContentMap.remove(MessageSchemaConstants.SUCCESS_KEY);
        }

        tmpContentMap.keySet().stream().forEach(key->{
            InPort inPort;
            if ((inPort = SpEnv.getInPortByUuid(key)) != null){
                // TODO: 2024/3/12 理论上接收的数据不应该带有数据类型，应该在发送时就根据输出端口转成对应的类型，在编排时就可以把下游组件的
                // TODO: 2024/3/12 的输入端口类型和上游组件匹配，接收到的数据类型应该在发送时对类型做保证
                /*if (tmpContentMap.get(key+MessageSchemaConstants.OUT_TYPE_KEY) != null){

                }

                if (tmpContentMap.get(key+MessageSchemaConstants.OUT_SUB_TYPE_KEY) != null){

                }

                if (tmpContentMap.get(key+MessageSchemaConstants.OUT_PORT_NODE_ID_KEY) != null){

                }*/

                message.append(inPort, tmpContentMap.get(key));
            }
        });

        messages.add(message);
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
    public static MQResponse convert(List metaMsg) {
        String queue = (String) metaMsg.get(0);
        List<Message> messages = new ArrayList<>();

        List metaMessageList = (List) metaMsg.get(1);
        metaMessageList.forEach(metaMessage -> {
            parseMessage((List) metaMessage, messages);
        });
        return new MQResponse(queue, messages);
    }
}
