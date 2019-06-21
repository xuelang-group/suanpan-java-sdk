package com.xuelang.mqstream.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class XReadGroupResponse {

    private String queue;

    private List<String> messageIds;

    public List<Map<String, String>> data;


    private static void parseMessage(List message, List<String> messageIds, List<Map<String, String>> data) {
        String consumeId = (String) message.get(0);
        messageIds.add(consumeId);

        List keysAndValues = (List) message.get(1);
        for (int i = 0; i < keysAndValues.size() - 1; i++) {
            String key = (String) keysAndValues.get(i);
            String value = (String) keysAndValues.get(i+1);
            Map<String, String> item = new HashMap<>();
            item.put(key, value);
            data.add(item);
        }
    }

    public static XReadGroupResponse fromOutput(List output) {
        String queue = (String) output.get(0);
        List<String> messageIds = new ArrayList<>();
        List<Map<String, String>> data = new ArrayList<>();

        List messageList = (List) output.get(1);
        messageList.forEach(message -> {
            parseMessage((List) message, messageIds, data);
        });
        return new XReadGroupResponse(queue, messageIds, data);
    }
}
