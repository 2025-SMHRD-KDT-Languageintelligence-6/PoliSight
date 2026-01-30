package com.simpol.polisight.service;

import com.google.gson.Gson;
import com.simpol.polisight.dto.AiResponseDto;
import com.simpol.polisight.dto.PolicySearchCondition;
import okhttp3.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // ✅ Ngrok 주소 (공백 없이 정확함)
    private static final String AI_SERVER_URL = "https://lanelle-bottlelike-everett.ngrok-free.dev/simulate";

    // 타임아웃 설정을 위해 Builder를 사용합니다.
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 연결 타임아웃 30초
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)    // ★ 읽기 타임아웃 60초 (AI 답변 대기 시간)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)   // 쓰기 타임아웃 30초
            .build();
    private final Gson gson = new Gson();

    public AiResponseDto getPolicyRecommendation(PolicySearchCondition condition) {
        log.info("⚡ AI 분석 요청 시작: {}", condition);

        // 1. [변환] 조건들을 하나의 문장으로 합침
        String conditionSentence = formatUserConditions(condition);

        // 2. [데이터 준비] 성공했던 코드처럼 Map을 사용하여 직접 JSON 구조를 만듭니다.
        // 이렇게 하면 DTO 파일이 어떻게 되어있든 상관없이 무조건 "정책명"으로 날아갑니다.
        String pName = (condition.getPolicyTitle() != null) ? condition.getPolicyTitle() : "정책 정보 없음";

        // 요청 데이터 (JSON) 만들기
        Map<String, Object> requestData = new HashMap<>();
        // [추가] 사용자가 적은 내용(userPrompt)이 있으면 반영하는 코드
        String defaultQuery = "이 정책에 내가 지원할 수 있는지 판단해줘.";
        String userCustomPrompt = condition.getUserPrompt();

        if (userCustomPrompt != null && !userCustomPrompt.isBlank()) {
            // 사용자가 내용을 적었으면 합쳐서 보냄
            requestData.put("query", defaultQuery + " (추가 상황: " + userCustomPrompt + ")");
        } else {
            // 안 적었으면 기본 질문만 전송
            requestData.put("query", defaultQuery);
        }
        requestData.put("conditions", conditionSentence);

        // ★ 핵심 수정: 'policyName'이 아니라 '정책명'이라는 키값을 직접 넣습니다.
        Map<String, String> policyInfo = new HashMap<>();
        policyInfo.put("정책명", pName);

        // 파이썬 서버가 'region'을 안 쓴다면 생략해도 되지만, 필요하다면 아래 주석 해제
        // String pRegion = (condition.getRegionSi() != null) ? condition.getRegionSi() : "전국";
        // policyInfo.put("지역", pRegion);

        requestData.put("policy", policyInfo);

        // 3. [통신] OkHttp + Gson 사용
        try {
            // Map을 JSON 문자열로 변환 (결과: {"policy": {"정책명": "..."} ... })
            String jsonBody = gson.toJson(requestData);

            // 로그로 확인해보세요. "정책명"이 확실히 보일 겁니다.
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

                    // 응답은 기존 DTO로 받습니다.
                    return gson.fromJson(responseString, AiResponseDto.class);
                } else {
                    log.error("❌ 통신 실패: 코드={}, 내용={}", response.code(), (response.body() != null ? response.body().string() : "null"));
                }
            }
        } catch (IOException e) {
            log.error("❌ AI 서버 통신 오류", e);
        }

        // 실패 시 null 반환
        return null;
    }

    // 👇 [수정] 코드를 한글로 변환하여 문장을 만드는 메서드
    private String formatUserConditions(PolicySearchCondition c) {

        // 1. 학력 코드 변환 (0049002 -> 고교 재학)
        String education = convertEducationToKorean(c.getEducationLevel());

        // 2. 고용 상태 변환 (UNEMPLOYED -> 미취업)
        String employment = convertEmploymentToKorean(c.getEmploymentStatus());

        String incomeStr = (c.getIncome() != null) ? c.getIncome() + "만원" : "정보 없음";
        String majors = (c.getMajorTypes() != null && !c.getMajorTypes().isEmpty()) ?
                c.getMajorTypes().stream().map(String::valueOf).collect(Collectors.joining(", ")) : "해당 없음";

        return String.format(
                "거주지: %s %s, 나이: %s세, 학력: %s, 전공: %s, 취업상태: %s, 소득: %s, 가구원: %s명, 결혼: %s, 자녀: %d명",
                safeString(c.getRegionSi()), safeString(c.getRegionGu()),
                (c.getAge() != null ? c.getAge() : "미상"),
                education,  // 변환된 한글 값 들어감
                majors,
                employment, // 변환된 한글 값 들어감
                incomeStr,
                (c.getFamilySize() != null ? c.getFamilySize() : 1),
                safeString(c.getMarry()),
                (c.getChildCount() != null ? c.getChildCount() : 0)
        );
    }

    // 👇 [추가] 학력 코드를 한글로 바꾸는 기능 (복사해서 아래쪽에 추가하세요)
    private String convertEducationToKorean(List<String> list) {
        if (list == null || list.isEmpty()) return "정보 없음";
        String code = list.get(0); // 리스트의 첫 번째 값

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

    // 👇 [추가] 고용 상태를 한글로 바꾸는 기능 (복사해서 아래쪽에 추가하세요)
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
    private String listToString(List<String> list) { return (list == null || list.isEmpty()) ? "정보 없음" : String.join(", ", list); }
}