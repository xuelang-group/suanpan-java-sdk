package com.xuelang.suanpan;

import com.xuelang.suanpan.client.ISpClient;
import com.xuelang.suanpan.client.SpClientBuilder;
import org.junit.Test;

public class TestStream {

    @Test
    public void testStream(){
        ISpClient spClient = SpClientBuilder.build();
        spClient.stream().pub();
    }

}
