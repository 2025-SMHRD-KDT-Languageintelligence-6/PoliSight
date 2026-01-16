package com.simpol.polisight.dto;

import lombok.Data;

@Data
public class PolicySearchCondition {
    // 1. 기본 검색 및 지역
    private String keyword;
    private String regionSi;
    private String regionGu;

    // 2. 인적 사항
    private String gender;
    private String birthDate;
    private Integer age;

    // 3. 사회적 상태
    private String educationLevel;
    private String employmentStatus;
    private Boolean isBusinessOwner; // HTML: bizCheck / simulation: isBusinessOwner

    // 4. 가구 사항
    private String marry;          // Y, N
    private Integer childCount;
    private String house;          // Y, N
    private Integer familySize;

    // 5. 소득
    private Long income;           // 본인 소득
    private Long householdIncome;  // 가구 소득

    // 6. AI 시뮬레이션용 (추가됨)
    private String userPrompt;
}