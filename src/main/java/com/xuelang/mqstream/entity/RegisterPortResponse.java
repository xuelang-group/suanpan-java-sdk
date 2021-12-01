package com.xuelang.mqstream.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterPortResponse implements Serializable {
    private boolean success;
    private String msg;
}
