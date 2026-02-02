package com.simpol.polisight.service;

import com.google.gson.Gson;
import com.simpol.polisight.dto.AiResponseDto;
import com.simpol.polisight.dto.AiResponseDto.RecommendationItem;
import com.simpol.polisight.dto.PolicyDto;
import com.simpol.polisight.dto.PolicySearchCondition;
import com.simpol.polisight.mapper.PolicyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSimulationService {

    // ✅ JPA Repository 대신 MyBatis Mapper를 주입합니다.
    private final PolicyMapper policyMapper;

    private static final String AI_SERVER_URL = "https://lanelle-bottlelike-everett.ngrok-free.dev/simulate";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private final Gson gson = new Gson();

    public AiResponseDto getPolicyRecommendation(PolicySearchCondition condition) {
        log.info("⚡ AI 분석 요청 시작: {}", condition);

        String conditionSentence = formatUserConditions(condition);
        String pName = (condition.getPolicyTitle() != null) ? condition.getPolicyTitle() : "정책 정보 없음";

        Map<String, Object> requestData = new HashMap<>();
        String defaultQuery = "이 정책에 내가 지원할 수 있는지 판단해줘.";
        String userCustomPrompt = condition.getUserPrompt();

        if (userCustomPrompt != null && !userCustomPrompt.isBlank()) {
            requestData.put("query", defaultQuery + " (추가 상황: " + userCustomPrompt + ")");
        } else {
            requestData.put("query", defaultQuery);
        }
        requestData.put("conditions", conditionSentence);
        requestData.put("userPrompt", userCustomPrompt);

        Map<String, String> policyInfo = new HashMap<>();
        policyInfo.put("정책명", pName);
        requestData.put("policy", policyInfo);

        try {
            String jsonBody = gson.toJson(requestData);
            log.info("📤 [자바가 보내는 JSON]: " + jsonBody);

            RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(AI_SERVER_URL)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseString = response.body().string();
                    log.info("🐍 [Python 응답]: {}", responseString);

                    AiResponseDto result = gson.fromJson(responseString, AiResponseDto.class);

                    // =================================================================
                    // ✅ [수정 완료] MyBatis Mapper를 사용하여 ID 조회 및 설정
                    // =================================================================
                    if (result != null && result.getRecommendations() != null) {
                        for (RecommendationItem item : result.getRecommendations()) {
                            if (item.getName() != null && !item.getName().isBlank()) {
                                try {
                                    // 1. 정책 이름으로 DB 조회 (Mapper 사용)
                                    PolicyDto policyDto = policyMapper.selectPolicyByName(item.getName());

                                    if (policyDto != null) {
                                        // 2. 조회된 정책이 있다면 ID 설정
                                        log.info("✅ 정책 매칭 성공: [{}] -> ID: {}", item.getName(), policyDto.getPlcyNo());
                                        // DTO 필드에 값 주입
                                        item.setId(policyDto.getPlcyNo());
                                    } else {
                                        log.warn("⚠️ 정책 매칭 실패 (DB 없음): [{}]", item.getName());
                                    }
                                } catch (Exception e) {
                                    log.error("❌ 정책 ID 조회 중 에러: {}", e.getMessage());
                                }
                            }
                        }
                    }

                    return result;

                } else {
                    log.error("❌ 통신 실패: 코드={}, 내용={}", response.code(), (response.body() != null ? response.body().string() : "null"));
                }
            }
        } catch (IOException e) {
            log.error("❌ AI 서버 통신 오류", e);
        }

        return null;
    }

    private String formatUserConditions(PolicySearchCondition c) {
        String education = convertEducationToKorean(c.getEducationLevel());
        String employment = convertEmploymentToKorean(c.getEmploymentStatus());

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

    private String convertEducationToKorean(List<String> list) {
        if (list == null || list.isEmpty()) return "정보 없음";
        String code = list.get(0);
        if (code.endsWith("001")) return "중졸 이하";
        if (code.endsWith("002")) return "고교 재학";
        if (code.endsWith("003")) return "고졸 예정";
        if (code.endsWith("004")) return "고졸";
        if (code.endsWith("005")) return "대학 재학";
        if (code.endsWith("006")) return "대졸 예정";
        if (code.endsWith("007")) return "대졸";
        if (code.endsWith("008")) return "석/박사";
        return "기타 (" + code + ")";
    }

    private String convertEmploymentToKorean(List<String> list) {
        if (list == null || list.isEmpty()) return "정보 없음";
        String status = list.get(0);
        if ("UNEMPLOYED".equals(status)) return "미취업(구직자)";
        if ("EMPLOYED".equals(status)) return "직장인(재직중)";
        if ("SELF_EMPLOYED".equals(status)) return "자영업/소상공인";
        if ("FREELANCER".equals(status)) return "프리랜서";
        if ("FOUNDER".equals(status)) return "창업자";
        return status;
    }

    private String safeString(String input) { return (input != null) ? input : ""; }
}