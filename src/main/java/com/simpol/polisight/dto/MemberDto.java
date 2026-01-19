package com.simpol.polisight.dto;

import lombok.Data;

@Data
public class MemberDto {
    // [DB 컬럼]
    private Long memberIdx;
    private String email;
    private String passwordHash;
    private String memberName;
    private String provider;

    private String province; // DB 컬럼
    private String city;     // DB 컬럼

    private String gender;
    private String birthDate;

    private Long personalIncome;
    private Long familyIncome;
    private Integer familySize;

    private Integer eduLevelCode;
    private Integer empStatusCode;

    private Boolean married;
    private Integer child;
    private Boolean home;

    // [HTML Form / JSON 입력값 매핑용 필드]
    // 화면에서 regionSi, regionGu 등으로 보내므로 이를 받을 필드가 필요함!
    // MyBatis가 자동으로 매핑하거나, 서비스에서 변환해줘야 함.
    // 여기서는 편의상 화면에서 보내는 이름과 동일한 필드를 추가하고,
    // DB 저장 시 이 값들을 사용하도록 쿼리를 짭니다.

    private String regionSi;        // -> province
    private String regionGu;        // -> city

    // 화면에서 String이나 int로 넘어오는 값들
    private Integer educationLevel; // -> eduLevelCode
    private Integer employmentStatus;// -> empStatusCode

    private String marryStatus;     // 화면에서 'Y'/'N' 또는 'true'/'false'로 올 수 있음
    private String houseOwnership;  // 화면에서 'Y'/'N' 또는 'true'/'false'로 올 수 있음

    // [회원가입/수정용]
    private String userPw;
    private String userName;
    private String birthYear;
    private String birthMonth;
    private String birthDay;
    private String currentPw;
    private String newPw;
}