package com.xuelang.mqstream.message.arguments;

import java.util.ArrayList;
import java.util.List;

class AppRelations {

    static final List<String> inputs = new ArrayList<>();

    //预设20个输入
    static {
        for (int i = 1; i <= 20; i++) {
            inputs.add("in" + i);
        }
    }

}
