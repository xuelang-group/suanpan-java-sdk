package com.xuelang.suanpan.common.entities.io;

import com.xuelang.suanpan.common.exception.GlobalExceptionType;
import com.xuelang.suanpan.common.exception.StreamGlobalException;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import org.apache.commons.lang3.StringUtils;

public class InPort extends BasePort {

    private InPort(){
    }
    public static InPort bind(Integer inPortNumber) throws StreamGlobalException {
        String key = "in" + inPortNumber;
        InPort inPort = ConstantConfiguration.getByInPortUuid(key);
        if (inPort == null){
            throw new StreamGlobalException(GlobalExceptionType.NoSuchInPortException);
        }

        return inPort;
    }

    public static InPort bind(String inPortUUID) throws StreamGlobalException {
        if (StringUtils.isBlank(inPortUUID)){
            return null;
        }
        InPort inPort = ConstantConfiguration.getByInPortUuid(inPortUUID);
        if (inPort == null){
            throw new StreamGlobalException(GlobalExceptionType.NoSuchInPortException);
        }

        return inPort;
    }

}
