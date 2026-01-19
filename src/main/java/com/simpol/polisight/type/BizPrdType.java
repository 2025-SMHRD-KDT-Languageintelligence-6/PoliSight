package com.simpol.polisight.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BizPrdType implements CodeEnum {
    SPECIFIC("0056001", "특정기간"),
    ETC("0056002", "기타");

    private final String code;
    private final String desc;
}