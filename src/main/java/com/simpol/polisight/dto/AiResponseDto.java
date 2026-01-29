package com.simpol.polisight.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List; // ★ List 사용을 위해 필수 import

@Data
@NoArgsConstructor
public class AiResponseDto {

    // 1. 적합 여부 (Python: "suitability")
    @SerializedName("suitability")
    private String suitability;

    // 2. 지역 헤더 (Python: "region_header")
    @SerializedName("region_header")
    private String regionHeader;

    // 3. 분석 내용 (Python: "content")
    private String content;

    // 4. 미래 시뮬레이션 시나리오 (Python: "future_scenario")
    @SerializedName("future_scenario")
    private String futureScenario;

    // 5. 추천 정책 리스트 (Python: "recommendations")
    @SerializedName("recommendations")
    private List<String> recommendations;

    // 6. 근거 데이터 (Python: "basis")
    private String basis;

    // (선택사항) 보험용 메서드
    public String get적합여부() {
        return this.suitability;
    }
}