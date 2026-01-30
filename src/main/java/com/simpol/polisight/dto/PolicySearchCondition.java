package com.simpol.polisight.dto;

import com.simpol.polisight.type.MajorType;
import com.simpol.polisight.type.SbizType;
import lombok.Data;
import java.util.List;

@Data
public class PolicySearchCondition {

    private String plcyNo;

    // 1. 기본 검색 및 지역
    private String keyword;
    private String regionSi; // 시/도
    private String regionGu; // 시/군/구

    // 2. 인적 사항
    private String gender;
    private String birthDate;
    private Integer age;

    // 3. 사회적 상태 (List 타입)
    private List<String> educationLevel;   // 학력
    private List<String> employmentStatus; // 취업 상태

    // 4. 가구 사항
    private String marry;
    private Integer childCount;
    private String house;
    private Integer familySize;

    // 5. 소득
    private Integer income;
    private Integer householdIncome;

    // 6. 상세 필터링
    private List<MajorType> majorTypes;
    private List<SbizType> sbizTypes;

    // 7. AI 시뮬레이션용
    private String userPrompt;

    // ★ [추가됨] 정책 이름을 담을 필드 (이게 있어야 오류가 안 납니다!)
    private String policyTitle;

    private Long memberIdx;
}