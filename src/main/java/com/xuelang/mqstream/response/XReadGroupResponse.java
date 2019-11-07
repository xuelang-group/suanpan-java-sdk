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

    public List<Map<String, String>> messages;

    private static void parseMessage(List message, List<String> messageIds, List<Map<String, String>> messages) {
        String consumeId = (String) message.get(0);
        messageIds.add(consumeId);

        Map<String, String> item = new HashMap<>();

        List keysAndValues = (List) message.get(1);
        for (int i = 0; i < keysAndValues.size() - 1; i += 2) {
            String key = (String) keysAndValues.get(i);
            String value = (String) keysAndValues.get(i + 1);
            item.put(key, value);
        }

        messages.add(item);
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
