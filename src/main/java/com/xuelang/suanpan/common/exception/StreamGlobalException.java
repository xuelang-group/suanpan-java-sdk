package com.xuelang.suanpan.common.exception;

public class StreamGlobalException extends RuntimeException {

    private int code;

    private String msg;

    public StreamGlobalException(GlobalExceptionType globalExceptionType, Throwable cause) {
        super(globalExceptionType.getMessage(), cause);
        this.code = globalExceptionType.getCode();
        this.msg = globalExceptionType.getMessage();
    }

    public StreamGlobalException(GlobalExceptionType globalExceptionType) {
        super(globalExceptionType.getMessage());
        this.code = globalExceptionType.getCode();
        this.msg = globalExceptionType.getMessage();
    }

    public StreamGlobalException(GlobalExceptionType globalExceptionType, String msg) {
        super(globalExceptionType.getMessage() + ", " + msg);
        this.code = globalExceptionType.getCode();
        this.msg = msg;
    }
}
