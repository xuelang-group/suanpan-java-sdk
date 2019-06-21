package com.xuelang.mqstream.options;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Queue {
    private String name;

    private String group;

    @Builder.Default
    private String consumeId = "0";

    @Builder.Default
    private boolean mkStream = true;
}
