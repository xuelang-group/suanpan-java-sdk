package com.xuelang.mqstream.message;

import java.util.List;

public interface MqSendService {

    String sendSuccessMessageToTarget(String target, String data, String extra, String requestId);

    String sendSuccessMessageToTarget(List<String> targets, String data, String extra, String requestId);

    String sendErrorMessageToTarget(String target, String errorMessage, String extra, String requestId);

    String sendErrorMessageToTarget(List<String> targets, String errorMessage, String extra, String requestId);
}
