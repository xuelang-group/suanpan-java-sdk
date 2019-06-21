package com.xuelang.mqstream.options;

import lombok.Builder;
import lombok.Data;

import java.util.Arrays;

@Data
@Builder(toBuilder = true)
public class Message {

    private String queue;

    private Object[] keysAndValues;

    @Builder.Default
    private long maxLength = 1000L;

    private boolean approximateTrimming;

    public static Object[] prepareKeysAndValues(Object ...keysAndValues) {
        return Arrays.copyOf(keysAndValues, keysAndValues.length);
    }
}
