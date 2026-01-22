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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSimulationService {

    // application properties 에 작성해보기
    private final String AI_SERVER_URL = "http://localhost:8000/ask";

    public String getPolicyRecommendation(PolicySearchCondition condition) {
        // 프롬프트 생성
        String prompt = createPrompt(condition);
        log.info("Generated AI Prompt: {}", prompt);

        // HTTP 요청 준비
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        AiRequestDto requestDto = new AiRequestDto(prompt);
        HttpEntity<AiRequestDto> entity = new HttpEntity<>(requestDto, headers);

        try {
            // AI 서버로 전송
            AiResponseDto response = restTemplate.postForObject(AI_SERVER_URL, entity, AiResponseDto.class);
            return response != null ? response.getAnswer() : "AI 응답이 없습니다.";
        } catch (Exception e) {
            log.error("AI Server Connection Error", e);
            return "AI 서버 연결 오류: " + e.getMessage();
        }
    }

    private String createPrompt(PolicySearchCondition c) {
        // 데이터 Null 방지 처리
        String keyword = (c.getKeyword() != null && !c.getKeyword().isEmpty()) ? c.getKeyword() : "선택된 정책 없음";
        String region = safeString(c.getRegionSi()) + " " + safeString(c.getRegionGu());
        String ageStr = (c.getAge() != null) ? c.getAge() + "세" : "정보 없음";

        // 리스트 데이터 처리
        String education = listToString(c.getEducationLevel());
        String employment = listToString(c.getEmploymentStatus());
        String incomeStr = (c.getIncome() != null) ? c.getIncome() + "만원" : "정보 없음";



        // Enum 리스트 처리
        // , 다음 공백 고민
        String majors = (c.getMajorTypes() != null && !c.getMajorTypes().isEmpty()) ?
                c.getMajorTypes().stream().map(String::valueOf).collect(Collectors.joining(", ")) : "해당 없음";
        String sbiz = (c.getSbizTypes() != null && !c.getSbizTypes().isEmpty()) ?
                c.getSbizTypes().stream().map(String::valueOf).collect(Collectors.joining(", ")) : "해당 없음";

        // 시뮬레이션 전용 프롬프트
        return String.format(
                "========================================\n" +
                        "[1. 시뮬레이션 타겟 정책 (고정)]\n" +
                        "정책명(키워드): %s\n" +
                        "**주의: 위 정책명과 일치하는 데이터에 대해서만 분석을 진행하세요.**\n" +
                        "========================================\n" +
                        "[2. 사용자 시뮬레이션 조건 (가상 설정)]\n" +
                        "- 거주지: %s\n" +
                        "- 나이: %s\n" +
                        "- 학력: %s\n" +
                        "- 전공: %s\n" +
                        "- 취업 상태: %s\n" +
                        "- 특화/소득: %s / %s\n" +
                        "- 가구: %s / 자녀 %s명\n" +
                        "========================================\n" +
                        "[3. AI 분석 및 시뮬레이션 요청]\n" +
                        "사용자가 위 '타겟 정책'을 받기 위해 자신의 상황을 시뮬레이션하고 있습니다.\n" +
                        "제공된 정책 데이터(Context)를 바탕으로 다음 3가지를 엄격하게 분석하세요.\n\n" +
                        "Step 1. [적합/부적합 판정]\n" +
                        "   - 사용자의 현재 조건이 정책 요건(지역, 나이, 소득 등)을 충족하는지 O/X로 판단하세요.\n" +
                        "   - 탈락 사유가 있다면 구체적으로 명시하세요.\n\n" +
                        "Step 2. [미래 시뮬레이션 시나리오]\n" +
                        "   - 부적합 시: '어떻게 조건을 바꾸면' 합격할 수 있는지 구체적 행동(예: 주소 이전, 구직 등록 등)을 제안하세요.\n" +
                        "   - 적합 시: 이 혜택을 통해 얻게 될 구체적 이점과 활용 방안을 제시하세요.\n\n" +
                        "Step 3. [근거 데이터]\n" +
                        "   - 당신의 판단은 정책 공고문의 어느 문구에 기반했는지 인용하여 설명하세요.",

                keyword,
                region.trim(),
                ageStr,
                education,
                majors,
                employment,
                sbiz, incomeStr,
                safeString(c.getMarry()), (c.getChildCount() != null ? c.getChildCount() : 0)
        );
    }

    private String safeString(String input) { return (input != null) ? input : ""; }
    private String listToString(List<String> list) { return (list == null || list.isEmpty()) ? "정보 없음" : String.join(", ", list); }
}