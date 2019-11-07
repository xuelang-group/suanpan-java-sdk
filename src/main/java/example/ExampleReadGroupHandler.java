package example;

import com.xuelang.mqstream.handler.XReadGroupHandler;
import com.xuelang.mqstream.message.arguments.CommonType;
import com.xuelang.mqstream.message.arguments.MessageDataType;
import com.xuelang.mqstream.response.XReadGroupResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public class ExampleReadGroupHandler implements XReadGroupHandler {
    @Override
    public void handle(XReadGroupResponse response) {
        log.info(String.format(
                "Subscribing queue: %s, %d message returns, %s",
                response.getQueue(),
                response.getMessageIds().size(),
                response.getMessages())
        );
    }

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        MessageDataType messageDataType = MessageDataType.EVENT;

        Class cls = messageDataType.getCls();

        Constructor declaredConstructor = cls.getDeclaredConstructor(Map.class);

        Map<String, String> message = new HashMap<>();
        message.put("in1","{\"event\":\"abcEvent\",\"data\":{}}");
        message.put("extra","abc");
        message.put("id","a12");

        CommonType baseType = (CommonType) declaredConstructor.newInstance(message);

        System.out.println(baseType);
    }
}
