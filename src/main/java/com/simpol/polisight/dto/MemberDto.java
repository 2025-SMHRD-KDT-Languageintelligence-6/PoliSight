package com.simpol.polisight.dto;

import lombok.Data;

@Data
public class MemberDto {
    // [DB 컬럼과 1:1 매칭되는 필드]
    private Long memberIdx;       // PK
    private String email;         // 이메일 (Form name="email"과 일치)
    private String passwordHash;  // DB 컬럼: 비밀번호
    private String memberName;    // DB 컬럼: 이름
    private String birthDate;     // DB 컬럼: 생년월일 (YYYY-MM-DD)

    // 그 외 DB 컬럼들 (필요시 사용, 현재는 null로 들어감)
    private String province;
    private String city;
    private String gender;
    // ... 나머지 컬럼 생략 (필요할 때 추가)

    // [HTML Form에서만 넘어오는 입력값 (DB 컬럼 아님)]
    private String userPw;        // Form name="userPw"
    private String userName;      // Form name="userName"

    private String birthYear;     // Form name="birthYear"
    private String birthMonth;    // Form name="birthMonth"
    private String birthDay;      // Form name="birthDay"
}