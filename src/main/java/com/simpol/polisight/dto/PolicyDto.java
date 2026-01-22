package com.simpol.polisight.dto;

import com.simpol.polisight.type.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PolicyDto {
    // ==========================================
    // 1. 기존 컬럼
    // ==========================================
    private String plcyNo;
    private String plcyNm;
    private String plcyExplnCn;
    private String lclsfNm;

    // ✅ DB 컬럼 plcyKywdNm과 매핑될 필드
    private String plcyKywdNm;

    private LocalDate bizPrdBgngYmd;
    private LocalDate bizPrdEndYmd;

    // ==========================================
    // 2. Enum 타입 컬럼
    // ==========================================
    private AplyPrdType aplyPrdSeCd;
    private BizPrdType bizPrdSeCd;
    private MrgSttsType mrgSttsCd;
    private EarnCndType earnCndSeCd;

    private List<MajorType> plcyMajorCd;
    private List<JobType> jobCd;
    private List<SchoolType> schoolCd;
    private List<SbizType> sbizCd;

    // ==========================================
    // 3. HTML 화면 표시용 Alias
    // ==========================================

    public String getId() { return plcyNo; }
    public String getTitle() { return plcyNm; }
    public String getDescription() { return plcyExplnCn; }
    public String getDepartment() { return lclsfNm; }
    public String getRegion() { return "전국/서울"; }

    public String getRequirement() {
        if (schoolCd != null && !schoolCd.isEmpty()) {
            return schoolCd.stream()
                    .map(SchoolType::getDesc)
                    .collect(Collectors.joining(", "));
        }
        return "상세 요건 참조";
    }

    public String getSupportContent() { return plcyExplnCn; }

    public long getDDay() {
        if (bizPrdEndYmd == null) return 999;
        return ChronoUnit.DAYS.between(LocalDate.now(), bizPrdEndYmd);
    }
}