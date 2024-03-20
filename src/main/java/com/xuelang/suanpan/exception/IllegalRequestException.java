package com.xuelang.suanpan.exception;

public class IllegalRequestException extends RuntimeException {
    public IllegalRequestException(String msg) {
        super("illegal request exception: " + msg);
    }
}
