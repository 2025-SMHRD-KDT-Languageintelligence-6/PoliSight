package com.simpol.polisight.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // status나 processing_time 등 필요 없는 건 무시
public class AiResponseDto {
    private String answer;

    // Getter, Setter
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}