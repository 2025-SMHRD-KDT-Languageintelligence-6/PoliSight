package com.simpol.polisight.service;

import com.google.gson.Gson;
import com.simpol.polisight.dto.*;
import com.simpol.polisight.dto.AiResponseDto.RecommendationItem;
import com.simpol.polisight.mapper.PolicyMapper;
import com.simpol.polisight.mapper.RecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationService {

    private final PolicyMapper policyMapper;
    private final RecordMapper recordMapper;

    // [수정] application.properties에서 주소 가져오기
    @org.springframework.beans.factory.annotation.Value("${ai.server.url}")
    private String aiServerUrl;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private final Gson gson = new Gson();

    /**
     * AI 분석 요청 및 결과 저장
     */
    @Transactional
    public AiResponseDto getPolicyRecommendation(PolicySearchCondition condition, MemberDto member, String plcyNo) {
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
        policyInfo.put("id", plcyNo);
        requestData.put("policy", policyInfo);

        try {
            String jsonBody = gson.toJson(requestData);
            log.info("📤 [자바가 보내는 JSON]: " + jsonBody);

            RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
            // [수정] 기본 주소 뒤에 "/simulate"를 직접 붙여줍니다.
            String simUrl = this.aiServerUrl + "/simulate";

            Request request = new Request.Builder()
                    .url(simUrl)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseString = response.body().string();
                    log.info("🐍 [Python 응답]: {}", responseString);

                    AiResponseDto result = gson.fromJson(responseString, AiResponseDto.class);

                    // 데이터 보정 (테스트용)
                    if (result != null) {
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

                    // 3. DB 저장
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
     * AI 결과를 JSON 통째로 DB에 저장
     */
    private void saveSimulationResult(MemberDto member, AiResponseDto aiResult, PolicySearchCondition condition, String plcyNo) {
        try {
            String jsonContent = gson.toJson(aiResult);

            RecordDto record = RecordDto.builder()
                    .memberIdx(member.getMemberIdx())
                    .plcyNo(plcyNo)
                    .province(condition.getRegionSi())
                    .city(condition.getRegionGu())
                    .gender(convertGender(condition.getGender()))
                    .birthDate(parseDate(condition.getBirthDate()))
                    .personalIncome(condition.getIncome())
                    .familyIncome(condition.getHouseholdIncome())
                    .familySize(condition.getFamilySize())
                    // ★ 중요: 여기서 학력을 숫자로 변환해 저장함 (이 값이 HTML로 전달됨)
                    .eduLevelCode(convertEducationToCode(condition.getEducationLevel()))
                    .empStatusCode(convertEmploymentToCode(condition.getEmploymentStatus()))
                    .married("Y".equals(condition.getMarry()))
                    .child(condition.getChildCount())
                    .home("Y".equals(condition.getHouse()))
                    .prompt(condition.getUserPrompt())
                    .content(jsonContent)
                    .build();

            recordMapper.insertRecord(record);
            log.info("💾 시뮬레이션 기록 DB 저장 완료");

        } catch (Exception e) {
            log.error("💾 DB 저장 실패", e);
        }
    }

    // --- 변환 헬퍼 메서드 ---

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) return null;
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return null;
        }
    }

    private String convertGender(String gender) {
        if ("male".equalsIgnoreCase(gender)) return "M";
        if ("female".equalsIgnoreCase(gender)) return "F";
        return null;
    }

    private Integer convertEducationToCode(List<String> eduList) {
        if (eduList == null || eduList.isEmpty()) return null;
        String code = eduList.get(0);
        // DB 저장용 코드 (1~8)
        if (code.endsWith("001")) return 1; // 중졸 이하
        if (code.endsWith("002")) return 2; // 고교 재학
        if (code.endsWith("003")) return 3; // 고졸 예정
        if (code.endsWith("004")) return 4; // 고졸
        if (code.endsWith("005")) return 5; // 대학 재학
        if (code.endsWith("006")) return 6; // 대졸 예정
        if (code.endsWith("007")) return 7; // 대졸
        if (code.endsWith("008")) return 8; // 석/박사
        return 0; // 기타
    }

    private Integer convertEmploymentToCode(List<String> empList) {
        if (empList == null || empList.isEmpty()) return null;
        String status = empList.get(0);
        if ("UNEMPLOYED".equals(status)) return 1;
        if ("EMPLOYED".equals(status)) return 2;
        if ("SELF_EMPLOYED".equals(status)) return 3;
        if ("FREELANCER".equals(status)) return 4;
        if ("FOUNDER".equals(status)) return 5;
        return 0;
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