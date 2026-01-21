package com.simpol.polisight.controller;

import com.simpol.polisight.dto.PolicyDto;
import com.simpol.polisight.dto.PolicySearchCondition;
import com.simpol.polisight.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class SimulationController {

    private final PolicyService policyService;

    // 1. 시뮬레이션 입력 페이지
    @GetMapping("/simulation")
    public String showSimulation(
            @RequestParam(name = "policyId", required = false) String policyId,
            Model model
    ) {
        if (policyId != null && !policyId.isBlank()) {
            PolicyDto selectedPolicy = policyService.getPolicyById(policyId);
            model.addAttribute("policy", selectedPolicy);
        }
        model.addAttribute("simulationForm", new PolicySearchCondition());
        return "simulation";
    }

    // 2. 분석 요청 (POST)
    @PostMapping("/simulation/analyze")
    public String analyzeSimulation(
            @ModelAttribute("simulationForm") PolicySearchCondition condition,
            @RequestParam(name = "policyId", required = false) String policyId,
            RedirectAttributes redirectAttributes
    ) {
        // 입력 데이터 전달 (FlashAttribute)
        redirectAttributes.addFlashAttribute("condition", condition);

        // 정책 ID 전달
        if (policyId != null && !policyId.isBlank()) {
            redirectAttributes.addFlashAttribute("policyId", policyId);
        }

        return "redirect:/simulation/result";
    }

    // 3. 결과 페이지 (GET)
    @GetMapping("/simulation/result")
    public String showSimulationResult(Model model) {

        if (!model.containsAttribute("condition")) {
            return "redirect:/simulation";
        }

        PolicySearchCondition condition = (PolicySearchCondition) model.asMap().get("condition");

        // ✅ [서비스 활용] 나이 계산
        int age = policyService.calculateAge(condition.getBirthDate());
        model.addAttribute("age", age);

        // ✅ 정책 정보 재조회
        if (model.containsAttribute("policyId")) {
            String policyId = (String) model.asMap().get("policyId");
            PolicyDto policy = policyService.getPolicyById(policyId);
            model.addAttribute("policy", policy);
        }

        model.addAttribute("score", 98); // 임시 점수

        return "result";
    }
}