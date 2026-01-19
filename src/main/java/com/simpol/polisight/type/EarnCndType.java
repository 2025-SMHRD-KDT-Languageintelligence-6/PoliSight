package com.simpol.polisight.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EarnCndType implements CodeEnum {
    IRRELEVANT("0043001", "무관"),
    ANNUAL_INCOME("0043002", "연소득"),
    ETC("0043003", "기타");

    private final String code;
    private final String desc;
}