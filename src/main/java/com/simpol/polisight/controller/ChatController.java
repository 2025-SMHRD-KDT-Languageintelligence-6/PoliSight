package com.simpol.polisight.controller;

import com.simpol.polisight.dto.ChatDto;
import com.simpol.polisight.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // [수정됨] produces를 TEXT_EVENT_STREAM_VALUE로 설정하여 스트리밍임을 명시합니다.
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatDto.Request request) {

        log.info("🌊 [리아 스트리밍 채팅] 사용자: {}, 입력: {}", request.getUserName(), request.getUserInput());

        // 스트리밍을 처리할 Emitter 생성 (타임아웃 2분 설정)
        SseEmitter emitter = new SseEmitter(120 * 1000L);

        // 서비스에서 비동기적으로 스트리밍 데이터를 채워넣도록 호출
        chatService.chatWithRiaStream(request.getUserInput(), request.getUserName(), emitter);

        return emitter;
    }
}