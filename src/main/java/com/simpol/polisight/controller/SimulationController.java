package com.simpol.polisight.controller;

import com.simpol.polisight.dto.*;
import com.simpol.polisight.service.AiSimulationService;
import com.simpol.polisight.service.PolicyService;
// import com.simpol.polisight.service.RecordService; // 더 이상 여기서 직접 저장 안 함
import jakarta.servlet.http.HttpSession;
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
    // private final RecordService recordService; // 여기서 사용 안 함 (Service 내부로 이동됨)

    // 1. 시뮬레이션 입력 페이지 (기존 유지)
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

    // 2. 분석 요청 (POST) (기존 유지)
    @PostMapping("/simulation/analyze")
    public String analyzeSimulation(
            @ModelAttribute("simulationForm") PolicySearchCondition condition,
            @RequestParam(name = "policyId", required = false) String policyId,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute("condition", condition);
        if (policyId != null && !policyId.isBlank()) {
            redirectAttributes.addFlashAttribute("policyId", policyId);
        }
        return "redirect:/simulation/result";
    }

    // 3. 결과 페이지 (GET) - ★ 수정된 부분
    @GetMapping("/simulation/result")
    public String showSimulationResult(Model model, HttpSession session) {

        if (!model.containsAttribute("condition")) {
            return "redirect:/simulation";
        }

        // =================================================================
        // 1. 회원 정보 확보 (Service에 넘겨주기 위해 객체화)
        // =================================================================
        Object loginMemberObj = session.getAttribute("loginMember");
        MemberDto member;

        if (loginMemberObj != null) {
            member = (MemberDto) loginMemberObj;
        } else {
            // 비회원일 경우, 임시 Member객체 생성 (DB 저장을 위해 memberIdx=1 등 기본값 필요)
            member = new MemberDto();
            member.setMemberIdx(1L); // 비회원 공용 ID (DB에 1번 회원이 존재해야 함)
        }

        PolicySearchCondition condition = (PolicySearchCondition) model.asMap().get("condition");

        // 나이 계산
        if (condition.getBirthDate() != null && !condition.getBirthDate().isBlank()) {
            int age = policyService.calculateAge(condition.getBirthDate());
            condition.setAge(age);
            model.addAttribute("age", age);
        } else if (condition.getAge() != null) {
            model.addAttribute("age", condition.getAge());
        }

        // =================================================================
        // 2. 정책 번호(ID) 확보
        // =================================================================
        String policyId = (String) model.asMap().get("policyId");

        if (policyId == null || policyId.isBlank()) {
            policyId = condition.getPlcyNo();
        }

        if (policyId != null && !policyId.isBlank()) {
            PolicyDto policy = policyService.getPolicyById(policyId);
            model.addAttribute("policy", policy);
            condition.setPolicyTitle(policy.getTitle());
        } else {
            log.warn("⚠️ 정책 ID(plcyNo)가 누락되었습니다.");
        }

        // =================================================================
        // 3. AI 분석 호출 (파라미터 3개로 변경!)
        // ★ Service 내부에서 DB 저장(JSON 통째로)까지 자동으로 수행함
        // =================================================================
        AiResponseDto aiResponseDto = aiSimulationService.getPolicyRecommendation(condition, member, policyId);

        // 기본값 설정 (화면 표시용)
        String content = "분석 결과 없음";
        String suitability = "N";
        String basis = "분석 근거 정보가 없습니다.";
        String answer = "";

        if (aiResponseDto != null) {
            content = aiResponseDto.getContent();
            suitability = aiResponseDto.getSuitability();
            basis = aiResponseDto.getBasis();
            answer = aiResponseDto.getAnswer();
        }

        // 결과 전달
        model.addAttribute("result", aiResponseDto);
        model.addAttribute("aiResult", content);
        model.addAttribute("suitability", suitability);
        model.addAttribute("basis", basis);
        model.addAttribute("answer", answer);

        // ★ [삭제됨] Controller에서의 수동 DB 저장 로직 삭제
        // AiSimulationService가 이미 저장했으므로 또 저장하면 중복됨.

        return "result";
    }
}