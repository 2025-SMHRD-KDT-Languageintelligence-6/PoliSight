package com.simpol.polisight.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordDto {

    // ==========================================
    // 1. DB 원본 데이터 (simulation 테이블)
    // ==========================================
    private Long simIdx;         // PK: 시뮬레이션 고유 번호
    private Long memberIdx;      // FK: 회원 번호
    private String plcyNo;       // FK: 정책 번호

    // 지역 및 인적 사항
    private String province;     // 시/도
    private String city;         // 시/군/구
    private String gender;       // 성별 (M/F)
    private LocalDate birthDate; // 생년월일

    // 소득 및 가구
    private Integer personalIncome; // 개인 소득
    private Integer familyIncome;   // 가구 소득
    private Integer familySize;     // 가구원 수

    // 상태 코드 & 여부
    private Integer eduLevelCode;   // 학력 코드
    private Integer empStatusCode;  // 고용 상태 코드
    private Boolean married;        // 결혼 여부
    private Integer child;          // 자녀 수
    private Boolean home;           // 주택 소유 여부

    // 내용
    private String prompt;       // 사용자 프롬프트
    private String content;      // AI 결과 내용 (JSON 전체)
    private LocalDateTime createdAt; // 생성일시

    // ==========================================
    // 2. JOIN 데이터 (policy 테이블)
    // ==========================================
    private String policyName;   // 정책 이름 (plcyNm)
    private String policyIntro;  // 정책 한줄 소개 (plcyExplnCn)

    // ==========================================
    // 3. 화면 표시용 가공 데이터
    // ==========================================
    private int resultScore;     // 결과 점수
    private String regionText;   // "서울 강남구"
    private int userAge;         // 만 나이
    private String jobName;      // 직업 한글명 (코드 변환)

    // ★ [추가] 적합 여부 (Y/N)
    // DB에는 없지만 Service에서 JSON을 파싱해서 채워넣는 필드입니다.
    private String suitability;
}