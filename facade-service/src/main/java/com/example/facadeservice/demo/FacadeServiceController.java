package com.example.facadeservice.demo;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class FacadeServiceController {
    public final static String MY_QUEUE = "my_queue";
    private final WebClient[] loggingClients = {
            WebClient.create("http://localhost:8081/"),
    };
    private final WebClient[] messaging = {
            WebClient.create("http://localhost:8082/"),
            WebClient.create("http://localhost:8084/"),
            WebClient.create("http://localhost:8086/"),
    };
    private Logger logger = Logger.getGlobal();
    private final ConnectionFactory factory = new ConnectionFactory();
    private Connection connection = null;
    private Channel channel = null;

    @SneakyThrows
    @PostMapping("/facade")
    public Mono<Void> facade(@RequestBody String text) {
        Message message = new Message(UUID.randomUUID(), text);
        logger.log(Level.ALL, text);
        send(channel, text);
        Mono<Void> voidd =  getRandomLoggingClient()
                .post()
                .uri("/log")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(message), Message.class)
                .retrieve()
                .bodyToMono(Void.class);
        return voidd;
    }

    @PostConstruct
    private void init() throws Exception {
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(MY_QUEUE, true, false, false, null);
    }

    @GetMapping("/facade")
    public Mono<String> facade() {
        Mono<String> messages =  messaging[new Random().nextInt(messaging.length)].get()
                                    .uri("/messages")
                                    .retrieve()
                                    .bodyToMono(String.class);

        Mono<String> logs = getLogs();

        return messages.zipWith(logs, (s, s2) -> s + ": " + s2).onErrorReturn("Error");
    }

    private WebClient getRandomLoggingClient() {
        return loggingClients[new Random().nextInt(loggingClients.length)];
    }

    private Mono<String> getLogs() {
        return getRandomLoggingClient().get()
                .uri("/log")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(err -> getLogs());
    }

    private static void send(Channel channel, String message) throws Exception {
        channel.basicPublish("", MY_QUEUE, null, message.getBytes());
        System.out.println(" [x] Sent " + message);
    }
}
