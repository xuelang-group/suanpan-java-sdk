package com.xuelang.suanpan.common.entities.io;

import com.xuelang.suanpan.configuration.ConstantConfiguration;
import com.xuelang.suanpan.common.exception.NoSuchInPortException;

import java.util.Objects;

public class InPort extends BasePort {

    private InPort(){
    }
    public static InPort build (Integer inPortIndex) throws NoSuchInPortException{
        Objects.requireNonNull(inPortIndex, "inPort index can not be null");
        String key = "in" + inPortIndex;
        InPort inPort = ConstantConfiguration.getInPortByUuid(key);
        if (inPort == null){
            throw new NoSuchInPortException("specified inPort index may illegal");
        }

        return inPort;
    }

    public static InPort build (String inPortUUID) throws NoSuchInPortException{
        Objects.requireNonNull(inPortUUID, "inPort index can not be null");
        InPort inPort = ConstantConfiguration.getInPortByUuid(inPortUUID);
        if (inPort == null){
            throw new NoSuchInPortException("specified inPort index may illegal");
        }

        return inPort;
    }

}
