package com.simpol.polisight.dto;

import lombok.Data;

@Data
public class PolicySearchCondition {
    // 1. 기본 검색 및 지역
    private String keyword;        // 검색어 (히든 인풋)
    private String regionSi;       // 시/도
    private String regionGu;       // 시/군/구

    // 2. 인적 사항
    private String gender;         // male, female
    private String birthDate;      // YYYYMMDD (HTML에서 옴)
    private Integer age;           // Service에서 계산할 나이

    // 3. 사회적 상태 (HTML 값 -> DB 코드 매핑 필요 가능성 있음)
    private String educationLevel; // UNDER_HIGH_SCHOOL, UNIV_GRAD ...
    private String employmentStatus; // UNEMPLOYED, EMPLOYED ...
    private Boolean isBusinessOwner; // 사업자 여부

    // 4. 가구 사항
    private String marry;          // Y, N
    private Integer childCount;
    private String house;          // Y, N
    private Integer familySize;

    // 5. 소득 (HTML: 만원 단위 -> Service: 원 단위 변환)
    private Long income;
    private Long householdIncome;
}