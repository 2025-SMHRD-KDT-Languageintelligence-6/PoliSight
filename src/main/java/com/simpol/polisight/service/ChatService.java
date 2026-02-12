package com.simpol.polisight.service;

import com.google.gson.Gson;
import com.simpol.polisight.dto.ChatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    @Value("${ai.server.url}")
    private String aiServerUrl;

    // 스트리밍을 위해 타임아웃 최적화
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // 데이터가 계속 들어오므로 읽기 타임아웃 해제
            .build();

    private final Gson gson = new Gson();

    /**
     * [수정 완료] 한글 깨짐 방지 및 스트리밍 중계 로직
     */
    public void chatWithRiaStream(String userMessage, String userName, SseEmitter emitter) {
        String chatUrl = this.aiServerUrl + "/chat";

        Map<String, String> data = new HashMap<>();
        data.put("user_input", userMessage);
        data.put("user_name", userName);

        RequestBody body = RequestBody.create(gson.toJson(data), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(chatUrl)
                .post(body)
                .build();

        // 비동기 방식으로 AI 서버 요청
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("❌ AI 서버 연결 실패: {}", e.getMessage());
                try {
                    emitter.send(SseEmitter.event().name("error").data("리아와 연결이 끊어졌어요 😢"));
                } catch (IOException ignored) {}
                emitter.completeWithError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    emitter.completeWithError(new RuntimeException("AI 서버 응답 오류: " + response.code()));
                    return;
                }

                // [핵심 수정 1] BufferedSource 대신 BufferedReader + UTF-8 조합으로 한글 깨짐 원천 봉쇄
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            // [핵심 수정 2] "data: " 접두사를 떼어내고 알맹이(JSON)만 추출
                            // SseEmitter.send()가 내부적으로 다시 "data: "를 붙이기 때문에 중복 방지를 위함입니다.
                            String jsonContent = line.substring(6).trim();

                            if (!jsonContent.isEmpty()) {
                                // 프론트엔드로 실시간 전송
                                emitter.send(jsonContent);
                            }
                        }
                    }
                    emitter.complete(); // 모든 전송 완료
                    log.info("✅ [리아] 답변 스트리밍 완료 (사용자: {})", userName);

                } catch (Exception e) {
                    log.error("❌ 스트리밍 중 중단됨: {}", e.getMessage());
                    emitter.completeWithError(e);
                }
            }
        });
    }
}