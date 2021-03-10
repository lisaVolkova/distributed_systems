package com.liza.message.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessagesServiceController {
    @GetMapping("/messages")
    public String message(){
        return "Not implemented yet";
    }
}
