package com.simpol.polisight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ChatDto {

    // 1. 요청 (Java -> Python)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        // 자바에서는 userInput, JSON으로 보낼 땐 "user_input"으로 변신!
        @JsonProperty("user_input")
        private String userInput;

        // 리아가 이름을 불러주기 위해 추가했어요!
        @JsonProperty("user_name")
        private String userName;
    }

    // 2. 응답 (Python -> Java)
    @Data
    @NoArgsConstructor
    public static class Response {
        private String answer;    // Python이 {"answer": "내용"} 으로 줌
    }
}