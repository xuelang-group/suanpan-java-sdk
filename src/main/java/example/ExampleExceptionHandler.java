package example;

import com.xuelang.mqstream.handler.ExceptionHandler;

public class ExampleExceptionHandler implements ExceptionHandler {
    @Override
    public void handle(Exception e) {
        System.out.println(e.getLocalizedMessage());
    }
}
