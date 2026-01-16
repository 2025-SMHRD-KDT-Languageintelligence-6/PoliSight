package com.simpol.polisight.controller;

import com.simpol.polisight.dto.PolicyDto;
import com.simpol.polisight.dto.PolicySearchCondition;
import com.simpol.polisight.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

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

    // 2. 정책 검색 페이지
    @GetMapping("/policy")
    public String showPolicySearch(
            @ModelAttribute PolicySearchCondition condition,
            Model model) {
        List<PolicyDto> policies = policyService.searchPolicies(condition);
        model.addAttribute("policyList", policies);
        model.addAttribute("condition", condition);
        return "policy";
    }

    // 3. 시뮬레이션 페이지 (수정됨)
    @GetMapping("/simulation")
    public String showSimulation(
            @RequestParam(name = "policyId", required = false) String policyId, // String 타입으로 받음
            Model model) {

        // 정책 ID가 넘어왔다면 DB에서 조회하여 'policy' 키로 모델에 담음
        if (policyId != null) {
            PolicyDto selectedPolicy = policyService.getPolicyById(policyId);
            model.addAttribute("policy", selectedPolicy);
        }

        // 시뮬레이션 페이지의 폼 바인딩을 위한 빈 객체 전달 (이름: simulationForm)
        model.addAttribute("simulationForm", new PolicySearchCondition());

        return "simulation";
    }
}