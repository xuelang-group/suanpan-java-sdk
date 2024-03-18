package com.xuelang.suanpan.common.exception;

public class IllegalRequestException extends RuntimeException {
    public IllegalRequestException(String msg) {
        super("illegal request exception: " + msg);
    }
}
