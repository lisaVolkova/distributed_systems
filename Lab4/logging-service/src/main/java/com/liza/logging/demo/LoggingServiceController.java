package com.liza.logging.demo;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class LoggingServiceController {
    private Logger logger = Logger.getGlobal();
    private final HazelcastInstance instance = Hazelcast.newHazelcastInstance();

    @GetMapping("/log")
    public String getLogs() {
        IMap<UUID, String> map = instance.getMap("map");
        StringBuilder builder = new StringBuilder();
        for (Map.Entry entry : map.entrySet()) {
            builder.append("{" + entry.getKey() + " " + entry.getValue() +"}");
        }
        logger.log(Level.ALL, builder.toString());
        return builder.toString();
    }

    @PostMapping("/log")
    public ResponseEntity<Void> log(@RequestBody Message msg) {

        IMap<UUID, String> map = instance.getMap("map");
        logger.log(Level.ALL, msg.toString());
        System.out.println(msg.getUUID() + " " + msg.getText());
        map.put(msg.getUUID(), msg.getText());
        return ResponseEntity.ok().build();
    }
}
