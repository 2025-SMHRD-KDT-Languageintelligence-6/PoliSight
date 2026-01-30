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

        // 로그인 체크
        Object loginMemberObj = session.getAttribute("loginMember");
        Long memberIdx = 1L; // 기본값 (비회원 등)
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

        // =================================================================
        // ★ [핵심 수정] 정책 번호(ID) 확보 로직 강화
        // =================================================================
        String policyId = (String) model.asMap().get("policyId");

        // 1. Model에 없으면 DTO(condition)에서 꺼내옵니다. (이게 있어야 DB 에러 방지!)
        if (policyId == null || policyId.isBlank()) {
            policyId = condition.getPlcyNo();
        }

        // 2. 정책 정보 조회
        if (policyId != null && !policyId.isBlank()) {
            PolicyDto policy = policyService.getPolicyById(policyId);
            model.addAttribute("policy", policy);
            condition.setPolicyTitle(policy.getTitle());
        } else {
            // ID가 끝까지 없으면 로그를 남겨 디버깅을 돕습니다.
            log.warn("⚠️ 정책 ID(plcyNo)가 누락되었습니다. DB 저장 시 에러가 발생할 수 있습니다.");
        }

        // =================================================================
        // AI 분석 호출
        // =================================================================
        AiResponseDto aiResponseDto = aiSimulationService.getPolicyRecommendation(condition);

        // 기본값 설정
        String content = "분석 결과 없음";
        String suitability = "N";
        String basis = "분석 근거 정보가 없습니다.";

        if (aiResponseDto != null) {
            content = aiResponseDto.getContent();
            suitability = aiResponseDto.getSuitability();
            basis = aiResponseDto.getBasis();
        }

        // 결과 전달
        model.addAttribute("result", aiResponseDto);
        model.addAttribute("aiResult", content);
        model.addAttribute("suitability", suitability);
        model.addAttribute("basis", basis);

        // DB 저장 (RecordDto)
        try {
            RecordDto newRecord = RecordDto.builder()
                    .memberIdx(memberIdx)
                    .plcyNo(policyId) // 위에서 확보한 policyId 사용
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
                    .content(content)
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