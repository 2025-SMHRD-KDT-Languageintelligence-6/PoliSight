package com.simpol.polisight.controller;

import com.simpol.polisight.dto.*;
import com.simpol.polisight.service.AiSimulationService;
import com.simpol.polisight.service.PolicyService;
import com.simpol.polisight.service.RecordService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SimulationController {

    private final PolicyService policyService;
    private final AiSimulationService aiSimulationService;
    private final RecordService recordService;

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
        redirectAttributes.addFlashAttribute("condition", condition);
        if (policyId != null && !policyId.isBlank()) {
            redirectAttributes.addFlashAttribute("policyId", policyId);
        }
        return "redirect:/simulation/result";
    }

    // 3. 결과 페이지 (GET)
    @GetMapping("/simulation/result")
    public String showSimulationResult(Model model, HttpSession session) {

        if (!model.containsAttribute("condition")) {
            return "redirect:/simulation";
        }

        // 로그인 체크
        Object loginMemberObj = session.getAttribute("loginMember");
        Long memberIdx = 1L; // 기본값
        if (loginMemberObj != null) {
            MemberDto loginMember = (MemberDto) loginMemberObj;
            memberIdx = loginMember.getMemberIdx();
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

        // 정책 정보 확인
        String policyId = (String) model.asMap().get("policyId");
        if (policyId != null) {
            PolicyDto policy = policyService.getPolicyById(policyId);
            model.addAttribute("policy", policy);
            condition.setPolicyTitle(policy.getTitle());
        } else {
            policyId = null;
        }

        // =================================================================
        // ★ [수정됨] AI 분석 호출 (Map -> DTO 변경)
        // =================================================================
        AiResponseDto aiResult = aiSimulationService.getPolicyRecommendation(condition);

        // 기본값 설정
        String content = "분석 결과 없음";
        String suitability = "N";
        String basis = "분석 근거 정보가 없습니다.";

        // 결과가 정상적으로 왔다면 덮어쓰기
        if (aiResult != null) {
            content = aiResult.getContent();
            suitability = aiResult.getSuitability();
            basis = aiResult.getBasis();
        }

        // 모델에 담기 (화면에 보여줄 데이터)
        model.addAttribute("aiResult", content);     // 상세 내용
        model.addAttribute("suitability", suitability); // 적합 여부 (Y/N)
        model.addAttribute("basis", basis);          // 판단 근거
        model.addAttribute("relatedPolicy", "없음"); // (AI 서버가 아직 안 주는 값이라 기본값 처리)

        // DB 저장 (RecordDto)
        try {
            RecordDto newRecord = RecordDto.builder()
                    .memberIdx(memberIdx)
                    .plcyNo(policyId)
                    .province(condition.getRegionSi())
                    .city(condition.getRegionGu())
                    .birthDate(parseDate(condition.getBirthDate()))
                    .gender(convertGender(condition.getGender()))
                    .personalIncome(condition.getIncome())
                    .familyIncome(condition.getHouseholdIncome())
                    .familySize(condition.getFamilySize())
                    .eduLevelCode(convertEducation(condition.getEducationLevel()))
                    .empStatusCode(convertEmployment(condition.getEmploymentStatus()))
                    .married("Y".equals(condition.getMarry()))
                    .child(condition.getChildCount())
                    .home("Y".equals(condition.getHouse()))
                    .prompt(condition.getUserPrompt())
                    .content(content) // AI 분석 결과 저장
                    .build();

            recordService.saveRecord(newRecord);
        } catch (Exception e) {
            log.error("Failed to save simulation record", e);
        }

        return "result";
    }

    // 유틸리티 메서드들 (기존 유지)
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) return null;
        try { return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd")); }
        catch (Exception e) { return null; }
    }
    private String convertGender(String gender) { return "male".equalsIgnoreCase(gender) ? "M" : "female".equalsIgnoreCase(gender) ? "F" : null; }
    private Integer convertEducation(List<String> eduList) {
        if (eduList == null || eduList.isEmpty()) return null;
        String code = eduList.get(0);
        if (code.endsWith("001")) return 1; if (code.endsWith("002")) return 2; if (code.endsWith("003")) return 3;
        if (code.endsWith("004")) return 4; if (code.endsWith("005")) return 5; if (code.endsWith("006")) return 6;
        if (code.endsWith("007")) return 7; if (code.endsWith("008")) return 8; return 0;
    }
    private Integer convertEmployment(List<String> empList) {
        if (empList == null || empList.isEmpty()) return null;
        String status = empList.get(0);
        if ("UNEMPLOYED".equals(status)) return 1; if ("EMPLOYED".equals(status)) return 2;
        if ("SELF_EMPLOYED".equals(status)) return 3; if ("FREELANCER".equals(status)) return 4;
        if ("FOUNDER".equals(status)) return 5; return 0;
    }
}