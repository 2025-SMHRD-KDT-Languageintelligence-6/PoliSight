package com.simpol.polisight.controller;

import com.simpol.polisight.dto.ChatDto;
import com.simpol.polisight.service.AiSimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController // ★ 중요: 화면(HTML)이 아니라 '데이터(JSON)'만 주고받는 컨트롤러입니다.
@RequestMapping("/api/chat") // 웹(JS)에서 이 주소로 요청을 보냅니다.
@RequiredArgsConstructor
public class ChatController {

    private final AiSimulationService aiService;

    // 프론트엔드(채팅창)에서 보낸 메시지를 받아서 -> Python 리아에게 전달 -> 결과를 반환
    @PostMapping
    public ChatDto.Response chat(@RequestBody ChatDto.Request request) {
        log.info("📨 [리아 채팅 요청] 사용자 입력: {}", request.getUser_input());

        // Service에 추가했던 메서드 호출
        return aiService.chatWithRia(request.getUser_input());
    }
}