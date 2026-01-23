package com.simpol.polisight.controller;

import com.simpol.polisight.dto.MemberDto; // MemberDto import 필요
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
        // 1. 로그인 체크 및 세션 정보 가져오기
        Object loginMemberObj = session.getAttribute("loginMember");
        if (loginMemberObj == null) {
            return "redirect:/login";
        }

        // 2. 세션 객체를 MemberDto로 캐스팅하여 memberIdx 추출 (핵심 수정 부분)
        MemberDto loginMember = (MemberDto) loginMemberObj;
        Long memberIdx = loginMember.getMemberIdx();

        // 디버깅용 로그 (확인 후 삭제 가능)
        log.info("기록 조회 요청 - 회원ID: {}, 키워드: {}", memberIdx, keyword);

        int pageSize = 5;

        // 3. Service 호출 (memberIdx 전달)
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