package com.simpol.polisight.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SbizType implements CodeEnum {
    SME("0014001", "중소기업"),
    FEMALE("0014002", "여성"),
    LOW_INCOME("0014003", "기초생활수급자"),
    SINGLE_PARENT("0014004", "한부모가정"),
    DISABLED("0014005", "장애인"),
    FARMER("0014006", "농업인"),
    SOLDIER("0014007", "군인"),
    LOCAL_TALENT("0014008", "지역인재"),
    ETC("0014009", "기타"),
    NONE("0014010", "제한없음");

    private final String code;
    private final String desc;
}