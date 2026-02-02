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

    // 7. 근거 데이터 리스트
    @SerializedName("evidence")
    private List<EvidenceItem> evidence;

    // 8. 답변
    @SerializedName("answer")
    private String answer;

    // ==========================================
    // 내부 클래스 1: 시나리오 구조
    // ==========================================
    @Data
    @NoArgsConstructor
    public static class Scenario {
        @SerializedName("type") private String type;
        @SerializedName("title") private String title;
        @SerializedName("content") private String content;
    }

    // ==========================================
    // 내부 클래스 2: 추천 정책 구조
    // ==========================================
    @Data
    @NoArgsConstructor
    public static class RecommendationItem {
        @SerializedName("name") private String name;
        @SerializedName("reason") private String reason;
        // DB에서 조회한 정책 ID를 담을 필드 (JSON에는 없지만 Java에서 채워넣음)
        private String id;
    } // <--- ★★★ 여기서 닫는 괄호(})가 반드시 있어야 합니다!!! ★★★

    // ==========================================
    // 내부 클래스 3: 근거 데이터 상세 구조 (EvidenceItem)
    // ==========================================
    @Data
    @NoArgsConstructor
    public static class EvidenceItem {
        @SerializedName("type")
        private String type;

        @SerializedName("title")
        private String title;

        @SerializedName("content")
        private String content;

        @SerializedName("match_info")
        private String matchInfo;
    }

}