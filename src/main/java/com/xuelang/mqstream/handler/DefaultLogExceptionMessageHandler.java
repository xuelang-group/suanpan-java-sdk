package com.xuelang.mqstream.handler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultLogExceptionMessageHandler implements ExceptionHandler {
    @Override
    public void handle(Exception e) {
      log.error(e.getMessage());
    }
}
