package com.xuelang.suanpan.node.io;

import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.exception.NoSuchInPortException;

import java.util.Objects;

public class OutPort extends BasePort{
    private OutPort(){}

    public static OutPort build (Integer outPortIndex) throws NoSuchInPortException {
        Objects.requireNonNull(outPortIndex, "outPort index can not be null");
        OutPort outPort = ConstantConfiguration.getOutPortByIndex(outPortIndex);
        if (outPort == null){
            throw new NoSuchInPortException("specified outPort index may illegal");
        }

        return outPort;
    }

    public static OutPort build (String outPortUUID) throws NoSuchInPortException {
        Objects.requireNonNull(outPortUUID, "outPort index can not be null");
        OutPort outPort = ConstantConfiguration.getOutPortByUUID(outPortUUID);
        if (outPort == null){
            throw new NoSuchInPortException("specified outPort index may illegal");
        }

        return outPort;
    }
}
