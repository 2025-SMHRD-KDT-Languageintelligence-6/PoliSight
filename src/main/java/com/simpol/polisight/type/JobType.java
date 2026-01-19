package com.simpol.polisight.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobType implements CodeEnum {
    WORKER("0013001", "재직자"),
    SELF_EMPLOYED("0013002", "자영업자"),
    UNEMPLOYED("0013003", "미취업자"),
    FREELANCER("0013004", "프리랜서"),
    DAILY_WORKER("0013005", "일용근로자"),
    ENTREPRENEUR("0013006", "(예비)창업자"),
    SHORT_TERM_WORKER("0013007", "단기근로자"),
    FARMER("0013008", "영농종사자"),
    ETC("0013009", "기타"),
    NONE("0013010", "제한없음");

    private final String code;
    private final String desc;
}