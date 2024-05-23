package com.xuelang.suanpan.common.entities.io;

import com.xuelang.suanpan.common.exception.GlobalExceptionType;
import com.xuelang.suanpan.common.exception.StreamGlobalException;
import com.xuelang.suanpan.common.utils.ParameterUtil;
import org.apache.commons.lang3.StringUtils;

public class Inport extends BasePort {

    private Inport(){
    }
    public static Inport bind(Integer inportIndex) throws StreamGlobalException {
        String key = "in" + inportIndex;
        Inport inport = ParameterUtil.getByInPortUuid(key);
        if (inport == null){
            throw new StreamGlobalException(GlobalExceptionType.NoSuchInPortException);
        }

        return inport;
    }

    public static Inport bind(String inPortUUID) throws StreamGlobalException {
        if (StringUtils.isBlank(inPortUUID)){
            return null;
        }
        Inport inport = ParameterUtil.getByInPortUuid(inPortUUID);
        if (inport == null){
            throw new StreamGlobalException(GlobalExceptionType.NoSuchInPortException);
        }

        return inport;
    }

}
