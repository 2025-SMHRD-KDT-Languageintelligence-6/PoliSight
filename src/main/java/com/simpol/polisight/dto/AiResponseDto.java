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

    // 4. 미래 시뮬레이션
    @SerializedName("scenarios")
    private List<Scenario> scenarios;

    // 5. 추천 정책
    @SerializedName("recommendations")
    private List<RecommendationItem> recommendations;

    // 6. 근거 데이터
    @SerializedName("basis")
    private String basis;

    // 7. 답변
    @SerializedName("answer")
    private String answer;

    // ==========================================
    // 내부 클래스 1: 시나리오 구조
    // ==========================================
    @Data
    @NoArgsConstructor
    public static class Scenario {
        @SerializedName("type")
        private String type;
        @SerializedName("title")
        private String title;
        @SerializedName("content")
        private String content;
    }

    // ==========================================
    // 내부 클래스 2: 추천 정책 구조
    // ==========================================
    @Data
    @NoArgsConstructor
    public static class RecommendationItem {
        @SerializedName("name")
        private String name;    // 정책 이름

        @SerializedName("reason")
        private String reason;  // 추천 사유

        // ▼ [추가] DB 조회 후 채워 넣을 정책 ID
        private String id;
    }
}