package com.liza.logging.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class LoggingServiceController {
    private Logger logger = Logger.getGlobal();
    private Map<UUID, String> map = new Hashtable<>();

    @GetMapping("/log")
    public String getLogs() {
        logger.log(Level.ALL, map.toString());
        return map.toString();
    }

    @PostMapping("/log")
    public ResponseEntity<Void> log(@RequestBody Message msg) {
        logger.log(Level.ALL, msg.toString());
        map.put(msg.getUUID(), msg.getText());
        return ResponseEntity.ok().build();
    }
}
