package com.xuelang.suanpan.common.entities.enums;

import org.apache.commons.lang3.StringUtils;

public enum NodeReceiveMsgType {
    sync,
    async;

    public static NodeReceiveMsgType getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (NodeReceiveMsgType type : NodeReceiveMsgType.values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }

        return null;

    }
}
