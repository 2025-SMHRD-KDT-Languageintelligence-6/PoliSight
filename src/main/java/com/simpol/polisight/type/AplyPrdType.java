package com.simpol.polisight.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AplyPrdType implements CodeEnum {
    SPECIFIC("0057001", "특정기간"),
    ALWAYS("0057002", "상시"),
    CLOSED("0057003", "마감");

    private final String code;
    private final String desc;
}