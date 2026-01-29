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

    // ✅ 추가해야 할 필드들 (DB 컬럼명과 일치시키기)
    private String plcySprtCn;      // 지원내용
    private Integer sprtTrgtMinAge;  // 최소연령
    private Integer sprtTrgtMaxAge;  // 최대연령
    private String earnEtcCn;       // 소득기타내용
    private String sbmsnDcmntCn;    // 제출서류내용
    private String etcMttrCn;       // 기타사항내용
    private String aplyUrlAddr;  // 신청 URL
    private String refUrlAddr1;  // 참고 URL 1
    private String refUrlAddr2;  // 참고 URL 2

    // ✅ DB 컬럼 plcyKywdNm과 매핑될 필드
    private String plcyKywdNm;

    private LocalDate bizPrdBgngYmd;
    private LocalDate bizPrdEndYmd;

    private String aplyYmd;

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

    // 화면 표기용
    private Boolean notify;

    // ==========================================
    // 3. HTML 화면 표시용 Alias
    // ==========================================

    public String getId() { return plcyNo; }
    public String getTitle() { return plcyNm; }
    public String getDescription() { return plcyExplnCn; }
    public String getDepartment() { return lclsfNm; }

    public String getRequirement() {
        if (schoolCd != null && !schoolCd.isEmpty()) {
            return schoolCd.stream()
                    .map(SchoolType::getDesc)
                    .collect(Collectors.joining(", "));
        }
        return "상세 요건 참조";
    }

    public String getSupportContent() { return plcyExplnCn; }

    // ==========================================
    // 1. 색상/정렬을 위한 숫자 계산 (HTML의 classappend 등에서 사용)
    // ==========================================
    public long getDDay() {
        // 1. 코드가 없으면 마감 처리
        if (aplyPrdSeCd == null) return -1;
        String code = aplyPrdSeCd.getCode();

        // ① [마감] 코드면 -> -1 리턴
        if ("0057003".equals(code)) return -1;

        // ② [상시] 코드면 -> 9999 리턴
        if ("0057002".equals(code)) return 9999;

        // ③ [기간] 코드면 -> 여러 기간 중 "가장 가까운 미래"를 찾음
        if ("0057001".equals(code) && aplyYmd != null) {
            long closestDDay = Long.MAX_VALUE; // 가장 가까운 D-Day 저장용
            boolean hasFuture = false;         // 미래 일정이 하나라도 있는지 체크

            // 1. \N 또는 줄바꿈(\n)으로 기간 분리
            // (데이터에 \N이 문자로 들어있을 경우와 실제 엔터일 경우 모두 대비)
            String[] periods = aplyYmd.split("\\\\N|\\n");

            for (String period : periods) {
                if (!period.contains("~")) continue;

                try {
                    // 2. 각 기간의 종료일 파싱 ("20260101 ~ 20261231" -> "20261231")
                    String endDateStr = period.split("~")[1].trim();
                    LocalDate endDate = LocalDate.parse(endDateStr, java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

                    // 3. D-Day 계산
                    long diff = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), endDate);

                    // 4. 아직 지나지 않은(0 이상) 날짜 중 가장 작은 값(임박한 값) 선택
                    if (diff >= 0) {
                        if (diff < closestDDay) {
                            closestDDay = diff;
                        }
                        hasFuture = true;
                    }
                } catch (Exception e) {
                    // 날짜 형식이 깨진 건 무시하고 다음 줄 확인
                    continue;
                }
            }

            // 미래 일정이 하나라도 있으면 그 D-Day 반환, 없으면(모두 지났으면) -1 반환
            return hasFuture ? closestDDay : -1;
        }

        return -1;
    }

    // ==========================================
    // 2. 화면에 보여줄 텍스트 (HTML의 th:text에서 사용)
    // ==========================================
    public String getDDayText() {
        long days = getDDay(); // 위에서 계산한 값 가져오기

        // 코드가 '상시'이거나 계산된 값이 9999이상이면
        if (days >= 9999) return "상시";

        // 코드가 '마감'이거나 날짜가 지났으면(음수)
        if (days < 0) return "마감";

        // 딱 오늘이면
        if (days == 0) return "D-Day";

        // 그 외 (D-5 등)
        return "D-" + days;
    }
}