package com.simpol.polisight.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberController {

    // 1. 로그인 페이지 이동
    // "localhost:8089
    @GetMapping({"/login"})
    public String loginPage() {
        return "login";
    }

    @GetMapping("/setup")
    public String setupPage() {
        return "setup"; // templates/setup.html을 반환
    }

}