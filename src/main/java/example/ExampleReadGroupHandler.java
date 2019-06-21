package example;

import com.xuelang.mqstream.handler.XReadGroupHandler;
import com.xuelang.mqstream.response.XReadGroupResponse;

import java.util.logging.Logger;

public class ExampleReadGroupHandler implements XReadGroupHandler {
    private static final Logger LOGGER = Logger.getLogger("example.XReadGroupHandler");
    @Override
    public void handle(XReadGroupResponse response) {
        LOGGER.info(String.format(
                "Subscribing queue: %s, %d message returns",
                response.getQueue(),
                response.getMessageIds().size())
        );
    }
}
