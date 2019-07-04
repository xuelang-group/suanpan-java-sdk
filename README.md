# suanpan-java-sdk

The sdk itself manipulates `lettuce-core` to communicate with Redis.

Currently, the SDK implements the following interfaces.

## establish connection

```java
// url
MqClient mqClient = new RedisStreamMqClient("redis://127.0.0.1");

// host and port
MqClient mqClient = new RedisStreamMqClient("192.168.99.100", 6379);
```

## relase connection

```java
mqClient.destroy();
```


## create queue

Create consumer group for a given Redis stream.

By default, the consumerId is `"0"` and mkStream is `true`(the stream will be created if it does not exist).

```java
Queue queue = Queue.builder()
                .name("js_queue")
                .group("js_group")
                .consumeId("$")
                .mkStream(true)
                .build();
mqClient.createQueue(queue, true);
```

## send message

```java
Message message = Message.builder()
                .queue("js_queue")
                .keysAndValues(Message.prepareKeysAndValues("java", "cool with redis", "javascript", "async"))
                .build();
String messageId = mqClient.sendMessage(message);
System.out.println(messageId);
```

MaxLength by default is set to 1000, and the data format follows the styles provided by both lettuce-core and Redis command line.

## subscribe queue

Please be noted that the subscribe queue interface will run infinitely, blocking the whole application.

PS: You may consider manipulate `ExecutorService` to execute subscribeQueue interface, preventing from blocking the main thread.

When new message arrives in the subscribed queue, this method will fetch the message and pass it to the `Handler` object.

If exception occurs within this method, the exception will be passed to the `exceptionHandler` object.

```java
Consumer consumer = Consumer.builder()
                .queue("js_queue")
                .group("js_group")
                .count(1)
                .build();
mqClient.subscribeQueue(
        consumer, 
        new ExampleReadGroupHandler(), 
        new ExampleExceptionHandler()
);
```

ExampleReadGroupHandler implements XReadGroupHandler interface, 
while ExampleExceptionHandler implements ExceptionHandler interface.

```java
public interface XReadGroupHandler {
    void handle(XReadGroupResponse response);
}
```

```java
public interface ExceptionHandler {
    void handle(Exception e);
}
```

## retry pending messages

TODO


## deployment

need discussion, may consider private maven repo.

 