package com.simpol.polisight.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpol.polisight.dto.PolicySearchCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSimulationService {

    // [복구] 다시 로컬 주소로 변경
    private final String AI_SERVER_URL = "http://localhost:8000/simulate";

    public Map<String, Object> getPolicyRecommendation(PolicySearchCondition condition) {
        log.info("⚡ AI 분석 요청 시작: {}", condition);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", "내 조건으로 이 정책을 지원할 수 있을지 판별해주고, 불가능하다면 대안을 제시해줘.");
        requestBody.put("conditions", formatUserConditions(condition));

        Map<String, String> policyInfo = new HashMap<>();
        String pTitle = condition.getPolicyTitle() != null ? condition.getPolicyTitle() : "정책 정보 없음";
        policyInfo.put("정책명", pTitle);
        requestBody.put("policy", policyInfo);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(AI_SERVER_URL, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            log.error("AI Server Error", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("적합여부", "N");
            errorResult.put("content", "AI 서버와 연결할 수 없습니다. (오류: " + e.getMessage() + ")");
            errorResult.put("연관된 정책", "없음");
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