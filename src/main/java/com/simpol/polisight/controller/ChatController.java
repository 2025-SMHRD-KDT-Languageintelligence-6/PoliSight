package com.simpol.polisight.controller;

import com.simpol.polisight.dto.ChatDto;
import com.simpol.polisight.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/chat")
    public ChatDto.Response chat(@RequestBody ChatDto.Request request) {

        log.info("📨 [리아 채팅] 사용자: {}, 입력: {}", request.getUserName(), request.getUserInput());

        return chatService.chatWithRia(request.getUserInput(), request.getUserName());
    }
}