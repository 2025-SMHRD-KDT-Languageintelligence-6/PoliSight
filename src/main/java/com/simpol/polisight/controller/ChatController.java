package com.simpol.polisight.controller;

import com.simpol.polisight.dto.ChatDto;
// [수정] 없는 AiService 대신, 우리가 수정한 AiSimulationService를 가져옵니다.
import com.simpol.polisight.service.AiSimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ChatController {

    // [수정] 여기도 AiSimulationService로 변경!
    private final AiSimulationService aiSimulationService;

    @PostMapping("/chat")
    public ChatDto.Response chat(@RequestBody ChatDto.Request request) {

        log.info("📨 [리아 채팅] 사용자: {}, 입력: {}", request.getUserName(), request.getUserInput());

        // [수정] 서비스 이름이 바뀌었으니 호출하는 변수명도 변경
        return aiSimulationService.chatWithRia(request.getUserInput(), request.getUserName());
    }
}