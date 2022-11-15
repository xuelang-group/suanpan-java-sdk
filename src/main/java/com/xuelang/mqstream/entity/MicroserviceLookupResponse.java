package com.xuelang.mqstream.entity;

import lombok.Data;

@Data
public class MicroserviceLookupResponse {
    private boolean success;
    private ResponseData data;

    @Data
    public class ResponseData {
        private String address;
    }
}
