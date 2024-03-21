package com.xuelang.suanpan.common.exception;

public class InvocationHandlerException extends RuntimeException {
    public InvocationHandlerException(String msg, Throwable e) {
        super("invocation exception: " + msg, e);
    }
}
