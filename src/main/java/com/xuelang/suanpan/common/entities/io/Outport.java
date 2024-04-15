package com.xuelang.suanpan.common.entities.io;

import com.xuelang.suanpan.common.exception.GlobalExceptionType;
import com.xuelang.suanpan.common.exception.StreamGlobalException;
import com.xuelang.suanpan.configuration.ConstantConfiguration;
import org.apache.commons.lang3.StringUtils;

public class Outport extends BasePort{
    private Outport(){}

    public static Outport bind(Integer outPortIndex) throws StreamGlobalException {
        if (outPortIndex == null){
            return null;
        }

        Outport outport = ConstantConfiguration.getByOutportIndex(outPortIndex);
        if (outport == null){
            throw new StreamGlobalException(GlobalExceptionType.NoSuchOutPortException);
        }

        return outport;
    }

    public static Outport bind(String outPortUUID) throws StreamGlobalException {
        if (StringUtils.isBlank(outPortUUID)){
            return null;
        }
        Outport outPort = ConstantConfiguration.getByOutPortUUID(outPortUUID);
        if (outPort == null){
            throw new StreamGlobalException(GlobalExceptionType.NoSuchOutPortException);
        }

        return outPort;
    }
}
