package com.example2.messagesservice.messagesservice;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@RestController
public class MessagesServiceController {
    private final Set<String> set = new HashSet<>();
    public final static String MY_QUEUE = "my_queue";
    public Channel channel = null;
    private static final ConnectionFactory factory = new ConnectionFactory();

    @PostConstruct
    private void constructor() {
        try {
            init();
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                set.add(message);
                System.out.println(" [x] Received '" + message + "'");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), true);
            };
            channel.basicConsume(MY_QUEUE, false, deliverCallback, consumerTag -> { });
        } catch (IOException ignored) {
        }
    }

    @SneakyThrows
    private void init() {
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();
        channel.basicQos(1);
    }

    @GetMapping("/messages")
    @ResponseBody
    public String message(){
        String str = null;
        for (String s : set) {
            str += s + "\n";
        }
        return str;
    }
}
