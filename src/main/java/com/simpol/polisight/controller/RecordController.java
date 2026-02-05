package com.simpol.polisight.controller;

import com.simpol.polisight.dto.MemberDto;
import com.simpol.polisight.dto.RecordDto;
import com.simpol.polisight.service.PolicyService; // ★ 추가
import com.simpol.polisight.service.RecordService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;
    private final PolicyService policyService; // ★ 추가: 정책 상세 정보를 위해 필요

    // 기록 페이지 조회
    @GetMapping("/record")
    public String showRecordPage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "keyword", required = false) String keyword,
            HttpSession session,
            Model model
    ) {
        // 1. 로그인 체크 및 세션 정보 가져오기
        Object loginMemberObj = session.getAttribute("loginMember");
        if (loginMemberObj == null) {
            return "redirect:/login";
        }

        // 2. 세션 객체를 MemberDto로 캐스팅하여 memberIdx 추출
        MemberDto loginMember = (MemberDto) loginMemberObj;
        Long memberIdx = loginMember.getMemberIdx();

        log.info("기록 조회 요청 - 회원ID: {}, 키워드: {}", memberIdx, keyword);

        int pageSize = 5;

        // 3. Service 호출
        List<RecordDto> recordList = recordService.getRecords(memberIdx, page, pageSize, keyword);
        int totalCount = recordService.getTotalCount(memberIdx, keyword);

        // 4. 페이지네이션 계산
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(totalPages, page + 2);

        if (endPage - startPage < 4) {
            endPage = Math.min(totalPages, startPage + 4);
            if (endPage - startPage < 4) startPage = Math.max(1, endPage - 4);
        }
        if (startPage < 1) startPage = 1;

        // 5. Model 담기
        model.addAttribute("recordList", recordList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        // ★ 추가: 정책 상세 모달에서 지역 코드를 한글로 변환하기 위해 필요
        model.addAttribute("regionMapping", policyService.getRegionMapping());

        return "record";
    }

    // 기록 삭제 API
    @PostMapping("/api/record/delete")
    @ResponseBody
    public ResponseEntity<String> deleteRecords(@RequestBody List<Long> simIdxList) {
        if (simIdxList == null || simIdxList.isEmpty()) {
            return ResponseEntity.badRequest().body("삭제할 항목이 없습니다.");
        }
        recordService.deleteRecords(simIdxList);
        return ResponseEntity.ok("success");
    }
}