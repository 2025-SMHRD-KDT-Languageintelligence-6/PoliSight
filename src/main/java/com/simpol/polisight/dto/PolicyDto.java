package com.simpol.polisight.dto;

import com.simpol.polisight.type.*; // 새로 만든 Enum 패키지 임포트
import lombok.Data;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PolicyDto {
    // ==========================================
    // 1. 기존 컬럼 (DB 일반 컬럼)
    // ==========================================
    private String plcyNo;          // 정책 ID
    private String plcyNm;          // 정책명
    private String plcyExplnCn;     // 정책 설명
    private String lclsfNm;         // 기관/분야
    private LocalDate bizPrdBgngYmd;
    private LocalDate bizPrdEndYmd; // 마감일

    // ==========================================
    // 2. [NEW] 공통 코드 컬럼 (MyBatis가 Enum으로 변환해줌)
    // ==========================================

    // [단일 선택] - CodeEnumTypeHandler가 처리
    private AplyPrdType aplyPrdSeCd;   // 신청기간 구분
    private BizPrdType bizPrdSeCd;     // 사업기간 구분
    private MrgSttsType mrgSttsCd;     // 결혼상태
    private EarnCndType earnCndSeCd;   // 소득조건

    // [다중 선택(JSON)] - JsonListTypeHandler가 처리
    private List<MajorType> plcyMajorCd; // 정책전공
    private List<JobType> jobCd;         // 취업상태
    private List<SchoolType> schoolCd;   // 학력요건
    private List<SbizType> sbizCd;       // 특화요건

    // ==========================================
    // 3. HTML 화면 표시용 Alias (기존 코드 유지 + 업그레이드)
    // ==========================================

    public String getId() { return plcyNo; }
    public String getTitle() { return plcyNm; }
    public String getDescription() { return plcyExplnCn; }
    public String getDepartment() { return lclsfNm; }
    public String getRegion() { return "전국/서울"; }

    // [업그레이드] 요건 정보가 Enum으로 들어오므로, 한글 설명으로 변환해서 반환 가능
    public String getRequirement() {
        // 예: 학력요건들을 콤마로 연결해서 출력
        if (schoolCd != null && !schoolCd.isEmpty()) {
            return schoolCd.stream()
                    .map(SchoolType::getDesc) // "고교 졸업", "대학 재학" 등
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