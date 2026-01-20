package com.simpol.polisight.dto;

import com.simpol.polisight.type.*; // Enum 패키지 임포트
import lombok.Data;
import java.util.List;

@Data
public class PolicySearchCondition {
    // 1. 기본 검색 및 지역
    private String keyword;
    private String regionSi;
    private String regionGu;

    // 2. 인적 사항 (매핑 수정)
    private String gender;
    private String birthDate;
    private Integer age;

    // 3. 사회적 상태 (Enum으로 변경 추천)
    // 사용자가 입력한 값(학력)을 DB의 SchoolType과 비교하기 위함
    // 단일 선택일 수도 있고, "고졸이 갈 수 있는 곳" 처럼 범위 검색일 수도 있으므로 List 권장
    private List<String> educationLevel;

    // 취업 상태도 여러 개 선택 가능하므로 List
    private List<String> employmentStatus;


    // 4. 가구 사항
    private String marry;
    private Integer childCount;
    private String house;
    private Integer familySize;

    // 5. 소득
    private int income;
    private Integer householdIncome;

    // 6. [NEW] 상세 필터링 (사용자가 직접 체크박스로 검색할 때)
    private List<MajorType> majorTypes; // 전공 필터
    private List<SbizType> sbizTypes;   // 특화 필터 (장애인, 농업인 등)

    // 7. AI 시뮬레이션용
    private String userPrompt;
}