package com.xuelang.mqstream.message;

import com.alibaba.fastjson.JSON;
import com.xuelang.mqstream.MqClient;
import com.xuelang.mqstream.MqClientFactory;
import com.xuelang.mqstream.config.GlobalConfig;
import com.xuelang.mqstream.options.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/5 16:27
 * @Description:
 */
@Slf4j
public class RedisMqSendService implements MqSendService {

    @Override
    public String sendSuccessMessageToTarget(String target, Object data, String extra, String requestId) {
        List<String> targets = Collections.singletonList(target);
        return sendSuccessMessageToTarget(targets, data, extra, requestId);
    }

    @Override
    public String sendSuccessMessageToTarget(List<String> targets, Object data, String extra, String requestId) {
        MqClient mqClient = MqClientFactory.getMqClient();

        Message message = Message.builder()
                .queue(GlobalConfig.streamSendQueue)
                .build();

        List<String> keysAndValues = new ArrayList<>();

        keysAndValues.addAll(Arrays.asList(
                "node_id", GlobalConfig.streamNodeId,
                "success", "true",
                "extra", extra,
                "request_id", requestId
        ));

        targets.forEach(target -> {
            keysAndValues.add(target);
            keysAndValues.add(JSON.toJSONString(data).replaceAll("^\"+|\"+$", ""));
        });

        message.setKeysAndValues(keysAndValues.toArray(new String[0]));

        String result = mqClient.sendMessage(message);

        String subMsg = message.toString();

        if (subMsg.length() > 2000) {
            subMsg = subMsg.substring(0, 2000) + "......";
        }

        log.info("send success message to {},message : {}", GlobalConfig.streamSendQueue, subMsg);

        // mqClient.destroy();

        return result;
    }

    @Override
    public String sendErrorMessageToTarget(String target, Object errorMessage, String extra, String requestId) {
        List<String> targets = Collections.singletonList(target);
        return sendErrorMessageToTarget(targets, errorMessage, extra, requestId);
    }

    @Override
    public String sendErrorMessageToTarget(List<String> targets, Object errorMessage, String extra, String requestId) {
        MqClient client = MqClientFactory.getMqClient();
        Message message = Message.builder()
                .queue(GlobalConfig.streamSendQueue)
                .keysAndValues(Message.prepareKeysAndValues(
                        "node_id",
                        GlobalConfig.streamNodeId,
                        "success", "false",
                        "msg", errorMessage,
                        "extra", extra,
                        "request_id", requestId)
                )
                .build();

        String result = client.sendMessage(message);
        log.info("send error message to {},message : {}", GlobalConfig.streamSendQueue, message);
//        client.destroy();

        return result;
    }
}
