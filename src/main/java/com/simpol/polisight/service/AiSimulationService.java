package com.simpol.polisight.service;

import com.simpol.polisight.dto.AiRequestDto;
import com.simpol.polisight.dto.AiResponseDto;
import com.simpol.polisight.dto.PolicySearchCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSimulationService {

    // 파이썬 서버 주소
    private final String AI_SERVER_URL = "http://localhost:8000/ask";

    /**
     * DTO를 받아 프롬프트를 생성하고 AI에게 질문을 보냅니다.
     */
    public String getPolicyRecommendation(PolicySearchCondition condition) {
        // 1. DTO 데이터를 자연어 문장(프롬프트)으로 변환
        String prompt = createPrompt(condition);
        log.info("Generated AI Prompt: {}", prompt);

        // 2. HTTP 요청 준비
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 3. 요청 전송
        AiRequestDto requestDto = new AiRequestDto(prompt);
        HttpEntity<AiRequestDto> entity = new HttpEntity<>(requestDto, headers);

        try {
            AiResponseDto response = restTemplate.postForObject(AI_SERVER_URL, entity, AiResponseDto.class);
            return response != null ? response.getAnswer() : "AI 응답이 없습니다.";
        } catch (Exception e) {
            log.error("AI Server Connection Error", e);
            return "AI 서버 연결 오류: " + e.getMessage();
        }
    }

    /**
     * PolicySearchCondition 데이터를 조합하여 자연어 질문을 만듭니다.
     */
    private String createPrompt(PolicySearchCondition c) {
        // 지역 병합 (Null 체크)
        String region = (c.getRegionSi() != null ? c.getRegionSi() : "") + " " +
                (c.getRegionGu() != null ? c.getRegionGu() : "");

        // 리스트 데이터를 문자열로 변환 (Null 체크)
        String education = (c.getEducationLevel() != null && !c.getEducationLevel().isEmpty()) ?
                String.join(", ", c.getEducationLevel()) : "정보 없음";

        String employment = (c.getEmploymentStatus() != null && !c.getEmploymentStatus().isEmpty()) ?
                String.join(", ", c.getEmploymentStatus()) : "정보 없음";

        // 나이 확인
        String ageStr = (c.getAge() != null) ? c.getAge() + "세" : "나이 정보 없음";

        // 프롬프트 조립
        return String.format(
                "저는 %s에 거주하는 %s 청년입니다. 학력은 %s이고, 현재 취업 상태는 %s입니다. " +
                        "저의 상황에 딱 맞는 청년 정책을 추천해주시고, 신청 자격과 혜택을 요약해서 알려주세요.",
                region.trim(),
                ageStr,
                education,
                employment
        );
    }
}