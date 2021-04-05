package com.liza.lab2;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class FacadeServiceController {

    private final WebClient messaging = WebClient.create("http://localhost:8081/");
    private final WebClient[] loggingClients = {
            WebClient.create("http://localhost:8087/"),
            WebClient.create("http://localhost:8088/"),
            WebClient.create("http://localhost:8089/")
    };
    private Logger logger = Logger.getGlobal();

    @PostMapping("/facade")
    public Mono<Void> facade(@RequestBody String text) {
        Message message = new Message(UUID.randomUUID(), text);
        logger.log(Level.ALL, text);
        Mono<Void> voidd =  getRandomLoggingClient()
                .post()
                .uri("/log")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(message), Message.class)
                .retrieve()
                .bodyToMono(Void.class);
        return voidd;
    }

    @GetMapping("/facade")
    public Mono<String> facade() {
        Mono<String> messages =  messaging.get()
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
}
