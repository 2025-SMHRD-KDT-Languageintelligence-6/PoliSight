package com.simpol.polisight.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

public class ChatDto {

    // 1. 프론트엔드 -> Java -> Python (요청)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String user_input; // Python이 이 이름("user_input")을 기다리고 있음!
    }

    // 2. Python -> Java -> 프론트엔드 (응답)
    @Data
    @NoArgsConstructor
    public static class Response {
        private String answer;    // Python이 이 이름("answer")으로 줌!
    }
}
