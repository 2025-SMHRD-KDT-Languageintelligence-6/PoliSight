package com.simpol.polisight.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MajorType implements CodeEnum {
    HUMANITIES("0011001", "인문계열"),
    SOCIAL("0011002", "사회계열"),
    COMMERCE("0011003", "상경계열"),
    SCIENCE("0011004", "이학계열"),
    ENGINEERING("0011005", "공학계열"),
    ARTS_SPORTS("0011006", "예체능계열"),
    AGRICULTURE("0011007", "농산업계열"),
    ETC("0011008", "기타"),
    NONE("0011009", "제한없음");

    private final String code;
    private final String desc;
}