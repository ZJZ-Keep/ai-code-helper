package org.example.aicodehelper.controller;

import jakarta.annotation.Resource;
import org.example.aicodehelper.ai.AiCodeHelperService;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class Controller {
    @Resource
    private AiCodeHelperService aiCodeHelperService;


    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(@RequestParam int memoryId, @RequestParam String message){
        return aiCodeHelperService.chatStream(memoryId,message)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
}
