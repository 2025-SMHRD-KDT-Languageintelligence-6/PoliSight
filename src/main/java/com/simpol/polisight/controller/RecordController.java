package com.simpol.polisight.controller;

import com.simpol.polisight.dto.RecordDto;
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

    // 기록 페이지 조회
    @GetMapping("/record")
    public String showRecordPage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "keyword", required = false) String keyword,
            HttpSession session,
            Model model
    ) {
        // 로그인 체크
        Object loginMember = session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/login";

        // TODO: 세션에서 실제 회원 ID 추출 (임시 1L)
        Long memberIdx = 1L;

        int pageSize = 5;
        List<RecordDto> recordList = recordService.getRecords(memberIdx, page, pageSize, keyword);
        int totalCount = recordService.getTotalCount(memberIdx, keyword);

        // 페이지네이션
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(totalPages, page + 2);

        if (endPage - startPage < 4) {
            endPage = Math.min(totalPages, startPage + 4);
            if (endPage - startPage < 4) startPage = Math.max(1, endPage - 4);
        }
        if (startPage < 1) startPage = 1;

        model.addAttribute("recordList", recordList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

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