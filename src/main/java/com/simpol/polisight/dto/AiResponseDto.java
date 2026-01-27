package com.simpol.polisight.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AiResponseDto {

    // ⭐ 가장 중요: Python 서버가 보내주는 "적합여부"라는 한글 키를 자바 변수와 연결
    @SerializedName("적합여부")
    private String suitability; // 예: "Y" 또는 "N"

    private String content;     // 예: "신청 가능합니다..."
    private String basis;       // 예: "서울시 거주 조건 충족..."
}