package com.simpol.polisight.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PolicyController {

    // 1. 인트로 페이지
    @GetMapping("/")
    public String showIntro() {
        return "intro";
    }

    // 2. 정책 검색 페이지
    @GetMapping("/policy")
    public String showPolicySearch() {
        return "policy";
    }

    // 3. 시뮬레이션 페이지
    @GetMapping("/simulation")
    public String showSimulation() {
        return "simulation";
    }
}
