package com.simpol.polisight.service;

import com.google.gson.Gson;
import com.simpol.polisight.dto.AiRequestDto;
import com.simpol.polisight.dto.AiResponseDto;
import com.simpol.polisight.dto.PolicySearchCondition;
import okhttp3.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSimulationService {

    // 👇 Ngrok 주소 (바뀌면 꼭 수정하세요!)
    private static final String AI_SERVER_URL = " https://lanelle-bottlelike-everett.ngrok-free.dev/simulate ";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    // 기존 Controller에서 호출하던 메서드 시그니처 유지
    public AiResponseDto getPolicyRecommendation(PolicySearchCondition condition) {
        log.info("⚡ AI 분석 요청 시작: {}", condition);

        // 1. [변환] 기존 SearchCondition -> 새로운 AiRequestDto 생성
        String conditionSentence = formatUserConditions(condition);

        // 정책명, 지역, 나이 등 null 처리 (안전장치)
        String pName = (condition.getPolicyTitle() != null) ? condition.getPolicyTitle() : "정책 정보 없음";
        String pRegion = (condition.getRegionSi() != null) ? condition.getRegionSi() : "전국";
        String rCode = "00000"; // 지역코드가 없다면 기본값 혹은 condition에서 가져오기
        int age = (condition.getAge() != null) ? condition.getAge() : 20;

        // DTO 조립
        AiRequestDto requestDto = new AiRequestDto(
                "이 정책에 내가 지원할 수 있는지 판단해줘.", // query
                conditionSentence, // conditions (문장으로 변환된 조건)
                new AiRequestDto.PolicyInfo(pName, pRegion), // policy 객체
                rCode, // region_code
                age    // age
        );

        // 2. [통신] OkHttp + Gson 사용
        try {
            String jsonBody = gson.toJson(requestDto);

            RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(AI_SERVER_URL)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseString = response.body().string();
                    log.info("🐍 Python 응답: {}", responseString);

                    // JSON -> AiResponseDto 객체 변환
                    return gson.fromJson(responseString, AiResponseDto.class);
                }
            }
        } catch (IOException e) {
            log.error("❌ AI 서버 통신 오류", e);
        }

        // 실패 시 빈 객체 반환 (혹은 에러 처리)
        return null;
    }

    // 👇 기존에 잘 만드신 로직 (그대로 유지)
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