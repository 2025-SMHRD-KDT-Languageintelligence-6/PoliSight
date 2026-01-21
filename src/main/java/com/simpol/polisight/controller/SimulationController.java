// [컨트롤러] SimulationController.java
package com.simpol.polisight.controller;

import com.simpol.polisight.dto.PolicyDto;
import com.simpol.polisight.dto.PolicySearchCondition;
import com.simpol.polisight.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class SimulationController {

    private final PolicyService policyService; // 정책 정보 조회를 위해 필요

    // 1. 시뮬레이션 입력 페이지
    @GetMapping("/simulation")
    public String showSimulation(
            @RequestParam(name = "policyId", required = false) String policyId,
            Model model
    ) {

        // 특정 정책에서 '시뮬레이션 돌리기'를 눌러서 왔을 경우 처리
        if (policyId != null && !policyId.isBlank()) {
            PolicyDto selectedPolicy = policyService.getPolicyById(policyId);
            model.addAttribute("policy", selectedPolicy);
        }

        // 폼 바인딩용 객체
        model.addAttribute("simulationForm", new PolicySearchCondition());

        return "simulation"; // templates/simulation.html
    }

    // 2. 시뮬레이션 분석 요청 처리 (POST)
    // ✅ 분석 결과 보기 버튼을 누르면 여기로 POST가 들어와야 함
    @PostMapping("/simulation/analyze")
    public String analyzeSimulation(@ModelAttribute("simulationForm") PolicySearchCondition condition) {

        // TODO: 나중에 AI 모델/파이썬 서버로 condition 전송 → 결과 수신
        System.out.println(">>> 분석 요청 들어옴: " + condition);

        // 처리 후 결과 페이지로 이동 (PRG 패턴)
        return "redirect:/simulation/result";
    }

    // 3. 시뮬레이션 결과 페이지 (GET)
    @GetMapping("/simulation/result")
    public String showSimulationResult(Model model) {

        // Mock Data (나중에 analyze에서 저장한 세션/DB 결과로 교체)
        model.addAttribute("age", 26);
        model.addAttribute("gender", "남성");
        model.addAttribute("score", 98);

        return "result"; // templates/result.html
    }

    // ✅ (보험) 실수로 /simulation/result 로 POST가 오더라도 405 방지
    @PostMapping("/simulation/result")
    public String fallbackResultPost() {
        return "redirect:/simulation/result";
    }
}
