package com.simpol.polisight.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MrgSttsType implements CodeEnum {
    MARRIED("0055001", "기혼"),
    SINGLE("0055002", "미혼"),
    NONE("0055003", "제한없음");

    private final String code;
    private final String desc;
}