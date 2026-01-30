package com.simpol.polisight.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class AiResponseDto {

    // 1. 적합 여부
    @SerializedName("suitability")
    private String suitability;

    // 2. 지역 헤더
    @SerializedName("region_header")
    private String regionHeader;

    // 3. 분석 내용
    @SerializedName("content")
    private String content;

    // [변경 1] 미래 시뮬레이션 (기존 String -> List<Scenario> 객체로 변경)
    // Python 프롬프트가 'scenarios' 라는 이름의 리스트를 줍니다.
    @SerializedName("scenarios")
    private List<Scenario> scenarios;

    // [변경 2] 추천 정책 (기존 List<String> -> List<RecommendationItem> 객체로 변경)
    // ★★★ 여기가 에러의 핵심 원인입니다! ★★★
    @SerializedName("recommendations")
    private List<RecommendationItem> recommendations;

    // 6. 근거 데이터
    @SerializedName("basis")
    private String basis;

    // ==========================================
    // ★ 내부 클래스 1: 시나리오 구조 (Type, Title, Content)
    // ==========================================
    @Data
    @NoArgsConstructor
    public static class Scenario {
        @SerializedName("type")
        private String type;    // risk, benefit, solution, roadmap

        @SerializedName("title")
        private String title;   // 제목

        @SerializedName("content")
        private String content; // 내용
    }

    // ==========================================
    // ★ 내부 클래스 2: 추천 정책 구조 (Name, Reason)
    // ==========================================
    @Data
    @NoArgsConstructor
    public static class RecommendationItem {
        @SerializedName("name")
        private String name;    // 정책 이름

        @SerializedName("reason")
        private String reason;  // 추천 사유
    }
}