package com.simpol.polisight.service;

import com.google.gson.Gson;
import com.simpol.polisight.dto.ChatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    @Value("${ai.server.url}")
    private String aiServerUrl;

    // HTTP 클라이언트 (타임아웃 설정 등)
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private final Gson gson = new Gson();

    /**
     * [챗봇 기능] 리아와 대화하기
     */
    public ChatDto.Response chatWithRia(String userMessage, String userName) {
        // AI 서버의 채팅 엔드포인트
        String chatUrl = this.aiServerUrl + "/chat";

        try {
            Map<String, String> data = new HashMap<>();
            data.put("user_input", userMessage);
            data.put("user_name", userName);

            String jsonBody = gson.toJson(data);
            log.info("🤖 챗봇 요청: {} (이름: {})", userMessage, userName);

            RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(chatUrl)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String resString = response.body().string();
                    log.info("✅ 챗봇 응답: {}", resString);
                    return gson.fromJson(resString, ChatDto.Response.class);
                } else {
                    log.error("❌ 챗봇 통신 실패: 코드={}", response.code());
                }
            }
        } catch (IOException e) {
            log.error("❌ 챗봇 연결 오류", e);
        }

        // 에러 발생 시 사용자에게 보여줄 메시지 리턴
        ChatDto.Response errorRes = new ChatDto.Response();
        errorRes.setAnswer("죄송해요, 리아와 연결이 안 돼요 😢 잠시 후 다시 시도해주세요.");
        return errorRes;
    }
}