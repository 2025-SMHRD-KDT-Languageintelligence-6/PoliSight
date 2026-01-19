package com.simpol.polisight.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SchoolType implements CodeEnum {
    UNDER_HIGH_SCHOOL("0049001", "고졸 미만"),
    HIGH_SCHOOL_ATTENDING("0049002", "고교 재학"),
    HIGH_SCHOOL_GRADUATING("0049003", "고졸 예정"),
    HIGH_SCHOOL_GRADUATE("0049004", "고교 졸업"),
    UNIV_ATTENDING("0049005", "대학 재학"),
    UNIV_GRADUATING("0049006", "대졸 예정"),
    UNIV_GRADUATE("0049007", "대학 졸업"),
    MASTER_DOCTOR("0049008", "석·박사"),
    ETC("0049009", "기타"),
    NONE("0049010", "제한없음");

    private final String code;
    private final String desc;
}