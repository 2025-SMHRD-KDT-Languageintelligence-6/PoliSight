package com.simpol.polisight.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
public class PolicyDto {
    // 1. DB 컬럼과 1:1 매칭
    private String plcyNo;          // 정책 ID
    private String plcyNm;          // 정책명
    private String plcyExplnCn;     // 정책 설명
    private String lclsfNm;         // 기관/분야
    private LocalDate bizPrdBgngYmd;
    private LocalDate bizPrdEndYmd; // 마감일

    // ==========================================
    // 2. HTML 화면 표시용 Alias (Getter 추가)
    // ==========================================

    // HTML: ${policy.id}
    public String getId() { return plcyNo; }

    // HTML: ${policy.title}
    public String getTitle() { return plcyNm; }

    // HTML: ${policy.description}
    public String getDescription() { return plcyExplnCn; }

    // HTML: ${policy.department}
    public String getDepartment() { return lclsfNm; }

    // [추가] HTML: ${policy.region}
    // DB의 지역 정보가 복잡하므로(JSON), 우선 기본값이나 기관명을 반환하도록 설정
    public String getRegion() {
        return "전국/서울"; // 또는 lclsfNm (임시)
    }

    // [추가] HTML: ${policy.supportContent}
    // 별도 컬럼이 없다면 설명(description)을 그대로 사용
    public String getSupportContent() {
        return plcyExplnCn;
    }

    // [추가] HTML: ${policy.requirement}
    // 요건 컬럼이 없다면 고정 멘트 반환
    public String getRequirement() {
        return "상세 요건 참조";
    }

    // HTML: ${policy.dDay}
    public long getDDay() {
        if (bizPrdEndYmd == null) return 999;
        return ChronoUnit.DAYS.between(LocalDate.now(), bizPrdEndYmd);
    }
}