package com.xuelang.mqstream.api;

import com.xuelang.mqstream.handler.ExceptionHandler;
import com.xuelang.mqstream.handler.XReadGroupHandler;

public abstract interface Consumer {
    public abstract void subscribe(XReadGroupHandler messageRecvHandler, ExceptionHandler exceptionHandler);
}
