package com.simpol.polisight.dto;

import lombok.Data;

@Data
public class MemberDto {
    // [DB 컬럼]
    private Long memberIdx;
    private String email;
    private String passwordHash;
    private String memberName;
    private String birthDate; // YYYY-MM-DD 형식

    // [HTML Form 입력값]
    private String userPw;    // 회원가입 시 사용
    private String userName;  // 회원가입/수정 시 사용 (Form name="userName")

    // [회원가입용 분할 날짜]
    private String birthYear;
    private String birthMonth;
    private String birthDay;

    // [회원수정용 추가 필드]
    private String currentPw; // 현재 비밀번호 확인용
    private String newPw;     // 변경할 새 비밀번호
}