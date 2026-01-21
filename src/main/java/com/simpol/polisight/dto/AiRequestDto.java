package com.simpol.polisight.dto;

public class AiRequestDto {
    private String question;

    public AiRequestDto(String question) {
        this.question = question;
    }
    // Getter, Setter
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
}