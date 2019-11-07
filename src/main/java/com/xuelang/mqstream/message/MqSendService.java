package com.xuelang.mqstream.message;

import java.util.List;

public interface MqSendService {

    void sendSuccessMessageToTarget(String target, String data, String extra, String requestId);

    void sendSuccessMessageToTarget(List<String> targets, String data, String extra, String requestId);

    void sendErrorMessageToTarget(String target, String errorMessage, String extra, String requestId);

    void sendErrorMessageToTarget(List<String> targets, String errorMessage, String extra, String requestId);
}
