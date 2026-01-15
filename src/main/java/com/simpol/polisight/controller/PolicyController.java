package com.simpol.polisight.controller;

import com.simpol.polisight.dto.PolicyDto;
import com.simpol.polisight.dto.PolicySearchCondition;
import com.simpol.polisight.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    // 1. 인트로 페이지
    @GetMapping("/")
    public String showIntro() {
        return "intro";
    }

    // 2. 정책 검색 페이지 (GET 요청 처리)
    @GetMapping("/policy")
    public String showPolicySearch(
            @ModelAttribute PolicySearchCondition condition, // 폼 데이터 자동 바인딩
            Model model) {

        // 검색 서비스 호출
        List<PolicyDto> policies = policyService.searchPolicies(condition);

        // 결과 전달
        model.addAttribute("policyList", policies);

        // 검색 조건 유지를 위해 condition도 전달 (선택사항)
        model.addAttribute("condition", condition);

        return "policy";
    }

    // 3. 시뮬레이션 페이지
    @GetMapping("/simulation")
    public String showSimulation() {
        return "simulation";
    }
}