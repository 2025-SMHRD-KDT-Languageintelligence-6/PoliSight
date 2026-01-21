package com.simpol.polisight.controller;

import com.simpol.polisight.dto.PolicyDto;
import com.simpol.polisight.dto.PolicySearchCondition;
import com.simpol.polisight.service.AiSimulationService;
import com.simpol.polisight.service.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SimulationController {

    private final PolicyService policyService;
    private final AiSimulationService aiSimulationService;

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
        // 입력 데이터 전달
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

        // ✅ [나이 계산] 생년월일이 있으면 나이를 계산해서 DTO와 Model에 세팅
        if (condition.getBirthDate() != null && !condition.getBirthDate().isBlank()) {
            int age = policyService.calculateAge(condition.getBirthDate());
            condition.setAge(age); // 서비스에서 쓰기 위해 DTO에 저장
            model.addAttribute("age", age);
        } else if (condition.getAge() != null) {
            model.addAttribute("age", condition.getAge());
        }

        // ✅ [AI 연동] DTO를 통째로 넘기면, 서비스가 알아서 프롬프트 만들고 답변 받아옴
        String aiResponse = aiSimulationService.getPolicyRecommendation(condition);

        // 결과 뷰로 전달
        model.addAttribute("aiResult", aiResponse);

        // 기존 정책 정보 재조회
        if (model.containsAttribute("policyId")) {
            String policyId = (String) model.asMap().get("policyId");
            PolicyDto policy = policyService.getPolicyById(policyId);
            model.addAttribute("policy", policy);
        }

        model.addAttribute("score", 98); // 임시 점수

        return "result";
    }
}