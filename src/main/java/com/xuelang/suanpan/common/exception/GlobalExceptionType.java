package com.xuelang.suanpan.common.exception;

public enum GlobalExceptionType {
    IllegalStreamMessage(100, "illegal stream message exception"),
    InvocationHandlerException(101, "invocation handler method exception"),
    NoSuchHandlerException(102, "no such handler exception"),
    NoSuchInPortException(103, "no such inPort exception"),
    NoSuchOutPortException(104, "no such outPort exception"),
    NoSuchMethodException(105, "no such method exception"),
    DuplicationHandlerException(106,"duplication handler exception"),
    IllegalStreamOperation(107, "subscriber existed, cannot use polling together!"),
    GetGraphError(108, "get sp graph error"),
    IllegalParameter(109, "illegal parameter");

    private int code;
    private String message;

    GlobalExceptionType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
