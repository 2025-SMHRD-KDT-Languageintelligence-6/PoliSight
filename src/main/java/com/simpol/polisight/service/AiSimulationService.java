package com.simpol.polisight.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpol.polisight.dto.PolicySearchCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSimulationService {

    // [중요] Ngrok 주소 확인 (바뀌었으면 수정 필수!)
    private final String AI_SERVER_URL = "https://lanelle-bottlelike-everett.ngrok-free.dev/simulate";

    public Map<String, Object> getPolicyRecommendation(PolicySearchCondition condition) {
        log.info("⚡ AI 분석 요청 시작: {}", condition);

        // 1. 요청 데이터 생성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", "이 정책에 내가 지원할 수 있는지 판단해줘.");
        requestBody.put("conditions", formatUserConditions(condition));

        Map<String, String> policyInfo = new HashMap<>();
        String pTitle = condition.getPolicyTitle() != null ? condition.getPolicyTitle() : "정책 정보 없음";
        policyInfo.put("정책명", pTitle);
        requestBody.put("policy", policyInfo);

        // 2. HTTP 요청 설정 (한글 깨짐 방지 적용)
        RestTemplate restTemplate = new RestTemplate();
        // ★ 한글 로그가 ????로 깨지는 것을 방지하기 위해 UTF-8 컨버터 추가
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 3. Python 서버 호출
            ResponseEntity<String> response = restTemplate.postForEntity(AI_SERVER_URL, entity, String.class);

            // 로그 확인 (이제 한글이 잘 보일 겁니다)
            System.out.println("\n🐍 [Python 응답 원본]: " + response.getBody());

            // 4. 응답 파싱
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> result = mapper.readValue(response.getBody(), Map.class);

            // ★★★ [핵심 수정] 호환성 확보 ★★★
            // HTML/Controller가 'suitability'를 찾든 '적합여부'를 찾든 다 되게 만듦
            if (result.containsKey("suitability")) {
                result.put("적합여부", result.get("suitability")); // 옛날 코드 호환용 복사
            }

            System.out.println("📦 [최종 반환 데이터]: " + result + "\n");
            return result;

        } catch (Exception e) {
            log.error("AI Server Error", e);
            Map<String, Object> errorResult = new HashMap<>();

            // 에러 시에도 두 가지 키를 다 넣어줌
            errorResult.put("suitability", "Error");
            errorResult.put("적합여부", "N");
            errorResult.put("content", "AI 서버와 연결할 수 없습니다. (오류: " + e.getMessage() + ")");
            errorResult.put("basis", "연결 실패");
            return errorResult;
        }
    }

    private String formatUserConditions(PolicySearchCondition c) {
        String education = listToString(c.getEducationLevel());
        String employment = listToString(c.getEmploymentStatus());
        String incomeStr = (c.getIncome() != null) ? c.getIncome() + "만원" : "정보 없음";
        String majors = (c.getMajorTypes() != null && !c.getMajorTypes().isEmpty()) ?
                c.getMajorTypes().stream().map(String::valueOf).collect(Collectors.joining(", ")) : "해당 없음";

        return String.format(
                "거주지: %s %s, 나이: %s세, 학력: %s, 전공: %s, 취업상태: %s, 소득: %s, 가구원: %s명, 결혼: %s, 자녀: %d명",
                safeString(c.getRegionSi()), safeString(c.getRegionGu()),
                (c.getAge() != null ? c.getAge() : "미상"),
                education, majors, employment, incomeStr,
                (c.getFamilySize() != null ? c.getFamilySize() : 1),
                safeString(c.getMarry()),
                (c.getChildCount() != null ? c.getChildCount() : 0)
        );
    }

    private String safeString(String input) { return (input != null) ? input : ""; }
    private String listToString(List<String> list) { return (list == null || list.isEmpty()) ? "정보 없음" : String.join(", ", list); }
}