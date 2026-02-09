package com.simpol.polisight.dto;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiRequestDto {
    // 1. 질문 내용 (기존 question 대신 query 사용)
    private String query;

    // 2. 사용자 상세 조건 (나이, 직업, 소득 등)
    private String conditions;

    // 3. 정책 정보 (정책명, 지역) - 아래 내부 클래스 사용
    private PolicyInfo policy;

    // [★추가됨] Python 서버가 'userPrompt'를 기다리고 있어서 이거 한 줄 추가해야 함!
    private String userPrompt;

    // 4. 지역 코드 (Python 서버가 region_code로 인식함)
    @SerializedName("region_code")
    private String regionCode;

    // 5. 나이
    private int age;

    // [내부 클래스] 정책 정보 꾸러미
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PolicyInfo {
        @SerializedName("정책명") // Python 서버가 "정책명"이라는 한글 키를 원함
        private String policyName;

        @SerializedName("지역") // Python 서버가 "지역"이라는 한글 키를 원함
        private String region;
    }
}