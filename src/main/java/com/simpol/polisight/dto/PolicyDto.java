package com.simpol.polisight.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PolicyDto {
    private String plcyNo;          // 정책 ID
    private String plcyNm;          // 정책명 (title)
    private String plcyExplnCn;     // 정책 설명 (description)
    private String lclsfNm;         // 기관/분야 (department)

    private LocalDate bizPrdBgngYmd;
    private LocalDate bizPrdEndYmd; // 마감일

    // 화면 표시용 (Alias)
    private String title;           // getter로 plcyNm 반환
    private String description;     // getter로 plcyExplnCn 반환
    private String department;      // getter로 lclsfNm 반환

    private Long dDay;              // D-Day (SQL에서 계산)

    // Lombok이 만들어주지만, 타임리프 호환성을 위해 명시적 Getter 추가 (선택사항)
    public String getTitle() { return plcyNm; }
    public String getDescription() { return plcyExplnCn; }
    public String getDepartment() { return lclsfNm; }
}