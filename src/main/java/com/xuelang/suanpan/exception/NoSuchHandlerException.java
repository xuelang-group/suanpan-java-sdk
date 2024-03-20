package com.xuelang.suanpan.exception;

public class NoSuchHandlerException extends RuntimeException {
    public NoSuchHandlerException(String msg) {
        super("not found handler exception: " + msg);
    }
}
