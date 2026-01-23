package com.simpol.polisight.controller;

import com.simpol.polisight.dto.MemberDto; // [필수 Import]
import com.simpol.polisight.dto.PolicyDto;
import com.simpol.polisight.dto.PolicySearchCondition;
import com.simpol.polisight.dto.RecordDto;
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
        // 입력 데이터 전달
        redirectAttributes.addFlashAttribute("condition", condition);

        // 정책 ID 전달
        if (policyId != null && !policyId.isBlank()) {
            redirectAttributes.addFlashAttribute("policyId", policyId);
        }

        return "redirect:/simulation/result";
    }

    // 3. 결과 페이지 (GET) - DB 저장 로직 포함
    @GetMapping("/simulation/result")
    public String showSimulationResult(Model model, HttpSession session) {

        // 1. 입력 데이터 확인
        if (!model.containsAttribute("condition")) {
            return "redirect:/simulation";
        }

        // 2. 로그인 체크 및 회원 정보 가져오기
        Object loginMemberObj = session.getAttribute("loginMember");
        if (loginMemberObj == null) {
            return "redirect:/login";
        }

        // [수정 완료] 세션에서 실제 회원 정보 추출
        MemberDto loginMember = (MemberDto) loginMemberObj;
        Long memberIdx = loginMember.getMemberIdx();

        log.info("시뮬레이션 결과 저장 요청 - 사용자: {}", memberIdx); // 로그 확인용

        PolicySearchCondition condition = (PolicySearchCondition) model.asMap().get("condition");

        // 3. 나이 계산
        if (condition.getBirthDate() != null && !condition.getBirthDate().isBlank()) {
            int age = policyService.calculateAge(condition.getBirthDate());
            condition.setAge(age);
            model.addAttribute("age", age);
        } else if (condition.getAge() != null) {
            model.addAttribute("age", condition.getAge());
        }

        // 4. AI 분석 호출
        String aiResponse = aiSimulationService.getPolicyRecommendation(condition);
        model.addAttribute("aiResult", aiResponse);

        // 5. 정책 정보 확인
        String policyId = (String) model.asMap().get("policyId");
        if (policyId != null) {
            PolicyDto policy = policyService.getPolicyById(policyId);
            model.addAttribute("policy", policy);
        } else {
            policyId = null;
        }

        // ==========================================
        // ✅ [DB 저장 로직] - RecordDto 변환 및 저장
        // ==========================================
        try {
            RecordDto newRecord = RecordDto.builder()
                    .memberIdx(memberIdx) // [핵심] 실제 로그인한 회원의 ID가 들어감
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
                    .content(aiResponse)
                    .build();

            recordService.saveRecord(newRecord);
            log.info("Simulation record saved successfully. SimIdx: {}, MemberIdx: {}", newRecord.getSimIdx(), memberIdx);

        } catch (Exception e) {
            log.error("Failed to save simulation record", e);
        }

        model.addAttribute("score", 98); // 임시 점수
        return "result";
    }

    // ==========================================
    // 🛠️ Private Helper Methods (데이터 변환용)
    // ==========================================

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) return null;
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return null;
        }
    }

    private String convertGender(String gender) {
        if ("male".equalsIgnoreCase(gender)) return "M";
        if ("female".equalsIgnoreCase(gender)) return "F";
        return null;
    }

    private Integer convertEducation(List<String> eduList) {
        if (eduList == null || eduList.isEmpty()) return null;
        String code = eduList.get(0);

        switch (code) {
            case "0049001": return 1;
            case "0049002": return 2;
            case "0049003": return 3;
            case "0049004": return 4;
            case "0049005": return 5;
            case "0049006": return 6;
            case "0049007": return 7;
            case "0049008": return 8;
            default: return 0;
        }
    }

    private Integer convertEmployment(List<String> empList) {
        if (empList == null || empList.isEmpty()) return null;
        String status = empList.get(0);

        switch (status) {
            case "UNEMPLOYED": return 1;
            case "EMPLOYED": return 2;
            case "SELF_EMPLOYED": return 3;
            case "FREELANCER": return 4;
            case "FOUNDER": return 5;
            default: return 0;
        }
    }
}