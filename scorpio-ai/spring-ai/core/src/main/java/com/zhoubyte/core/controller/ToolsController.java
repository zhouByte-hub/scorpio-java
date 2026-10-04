package com.zhoubyte.core.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/tool")
public class ToolsController {

    @Autowired
    @Qualifier("toolChatClient")
    private ChatClient toolChatClient;

    @GetMapping(value = "/chat")
    public String chat(@RequestParam("message") String message) {
        return toolChatClient.prompt(message).call().content();
    }
}
