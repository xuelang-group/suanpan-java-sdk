package com.xuelang.mqstream.options;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder(toBuilder = true)
public class Consumer {

    @Builder.Default
    private String group = "default";

    @Builder.Default
    private String name = "unknown";

    @Builder.Default
    private long count = 1L;

    private boolean noAck;

    @Builder.Default
    private boolean block = true;

    private String queue;

    @Builder.Default
    private String consumeId = ">";

    @Builder.Default
    private long delay = 1000L;
}
