package example;

import com.xuelang.mqstream.handler.XReadGroupHandler;
import com.xuelang.mqstream.response.XReadGroupResponse;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ExampleReadGroupHandler implements XReadGroupHandler {
    @Override
    public void handle(XReadGroupResponse response) {
        log.info(String.format(
                "Subscribing queue: %s, %d message returns",
                response.getQueue(),
                response.getMessageIds().size())
        );
    }
}
