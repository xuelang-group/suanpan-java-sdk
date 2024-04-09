package com.xuelang.suanpan.common.entities.io;

import com.xuelang.suanpan.common.exception.GlobalExceptionType;
import com.xuelang.suanpan.common.exception.StreamGlobalException;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import org.apache.commons.lang3.StringUtils;

public class OutPort extends BasePort{
    private OutPort(){}

    public static OutPort bind(Integer outPortNumber) throws StreamGlobalException {
        if (outPortNumber == null){
            return null;
        }

        OutPort outPort = ConstantConfiguration.getByOutPortNumber(outPortNumber);
        if (outPort == null){
            throw new StreamGlobalException(GlobalExceptionType.NoSuchOutPortException);
        }

        return outPort;
    }

    public static OutPort bind(String outPortUUID) throws StreamGlobalException {
        if (StringUtils.isBlank(outPortUUID)){
            return null;
        }
        OutPort outPort = ConstantConfiguration.getByOutPortUUID(outPortUUID);
        if (outPort == null){
            throw new StreamGlobalException(GlobalExceptionType.NoSuchOutPortException);
        }

        return outPort;
    }
}
