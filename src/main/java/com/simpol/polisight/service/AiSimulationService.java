package com.simpol.polisight.service;

import com.google.gson.Gson;
import com.simpol.polisight.dto.*; // DTO 일괄 import
import com.simpol.polisight.dto.AiResponseDto.RecommendationItem;
import com.simpol.polisight.mapper.PolicyMapper;
import com.simpol.polisight.mapper.RecordMapper; // RecordMapper 추가
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 추가

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSimulationService {

    private final PolicyMapper policyMapper;
    private final RecordMapper recordMapper; // DB 저장을 위해 필요

    private static final String AI_SERVER_URL = "https://lanelle-bottlelike-everett.ngrok-free.dev/simulate";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private final Gson gson = new Gson();

    /**
     * AI 분석 요청 및 결과 저장 (메인 메서드)
     */
    @Transactional // DB 저장까지 한 번에 처리
    public AiResponseDto getPolicyRecommendation(PolicySearchCondition condition, MemberDto member, String plcyNo) {
        log.info("⚡ AI 분석 요청 시작: {}", condition);

        // 1. AI 서버 통신 준비
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

            // 2. AI 서버 요청 및 응답 대기
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseString = response.body().string();
                    log.info("🐍 [Python 응답]: {}", responseString);

                    AiResponseDto result = gson.fromJson(responseString, AiResponseDto.class);

                    // --- [데이터 보정 로직] ---
                    if (result != null) {
                        // evidence 보정 (테스트용)
                        if (result.getEvidence() == null || result.getEvidence().isEmpty()) {
                            List<AiResponseDto.EvidenceItem> fakeEvidence = new ArrayList<>();
                            AiResponseDto.EvidenceItem item1 = new AiResponseDto.EvidenceItem();
                            item1.setType("법령");
                            item1.setTitle("청년고용촉진 특별법 (테스트성공)");
                            item1.setMatchInfo("나이 26세 < 34세 (조건 만족)");
                            fakeEvidence.add(item1);
                            result.setEvidence(fakeEvidence);
                        }
                    }

                    // 정책 ID 매핑
                    if (result != null && result.getRecommendations() != null) {
                        for (RecommendationItem item : result.getRecommendations()) {
                            if (item.getName() != null && !item.getName().isBlank()) {
                                try {
                                    PolicyDto policyDto = policyMapper.selectPolicyByName(item.getName());
                                    if (policyDto != null) {
                                        item.setId(policyDto.getPlcyNo());
                                    }
                                } catch (Exception e) {
                                    log.error("❌ 정책 ID 조회 중 에러: {}", e.getMessage());
                                }
                            }
                        }
                    }

                    // 3. ★ 핵심 수정: 분석 결과를 JSON 문자열로 변환하여 DB에 저장
                    if (member != null && plcyNo != null) {
                        saveSimulationResult(member, result, condition, plcyNo);
                    }

                    return result;

                } else {
                    log.error("❌ 통신 실패: 코드={}", response.code());
                }
            }
        } catch (IOException e) {
            log.error("❌ AI 서버 통신 오류", e);
        }

        return null;
    }

    /**
     * AI 결과를 JSON 통째로 DB에 저장하는 헬퍼 메서드
     */
    private void saveSimulationResult(MemberDto member, AiResponseDto aiResult, PolicySearchCondition condition, String plcyNo) {
        try {
            // (1) AI 결과 전체를 JSON 문자열로 변환 (모든 시나리오, 추천, 근거 포함됨)
            String jsonContent = gson.toJson(aiResult);

            // (2) RecordDto 생성
            RecordDto record = RecordDto.builder()
                    .memberIdx(member.getMemberIdx())
                    .plcyNo(plcyNo)
                    // 인적 사항 매핑
                    .province(condition.getRegionSi())
                    .city(condition.getRegionGu())
                    .gender(null) // condition에 gender 필드가 없다면 null 또는 추가 필요
                    .personalIncome(condition.getIncome())
                    // .birthDate(...) 등 필요한 필드 매핑
                    .familySize(condition.getFamilySize())
                    .child(condition.getChildCount())
                    .prompt(condition.getUserPrompt())

                    // ★ 여기가 핵심: 단순 텍스트가 아니라 JSON 전체를 저장
                    .content(jsonContent)
                    .build();

            // (3) DB 저장
            recordMapper.insertRecord(record);
            log.info("💾 시뮬레이션 기록 DB 저장 완료 (JSON 포맷)");

        } catch (Exception e) {
            log.error("💾 DB 저장 실패", e);
        }
    }

    // --- 기존 헬퍼 메서드들 (유지) ---
    private String formatUserConditions(PolicySearchCondition c) {
        // ... (기존 코드와 동일) ...
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

    // 리아 채팅 기능 (기존 유지)
    public com.simpol.polisight.dto.ChatDto.Response chatWithRia(String userMessage) {
        String baseUrl = AI_SERVER_URL.replace("/simulate", "");
        String chatUrl = baseUrl + "/chat";
        // ... (기존 채팅 로직 동일) ...
        try {
            java.util.Map<String, String> data = new java.util.HashMap<>();
            data.put("user_input", userMessage);
            String jsonBody = gson.toJson(data);
            RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(chatUrl).post(body).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return gson.fromJson(response.body().string(), com.simpol.polisight.dto.ChatDto.Response.class);
                }
            }
        } catch (Exception e) { log.error("채팅 오류", e); }
        return new com.simpol.polisight.dto.ChatDto.Response();
    }
}