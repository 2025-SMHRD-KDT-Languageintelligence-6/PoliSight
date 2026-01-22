package com.simpol.polisight.controller;

import com.simpol.polisight.dto.MemberDto;
import com.simpol.polisight.dto.PolicyDto;
import com.simpol.polisight.dto.PolicySearchCondition;
import com.simpol.polisight.mapper.FavoriteMapper;
import com.simpol.polisight.service.PolicyService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;
    private final FavoriteMapper favoriteMapper;

    // 1. 인트로 페이지
    @GetMapping("/")
    public String showIntro() {
        return "intro";
    }

    // 2. 정책 검색 페이지
    @GetMapping("/policy")
    public String showPolicySearch(
            @ModelAttribute PolicySearchCondition condition,
            Model model, HttpSession session) {
        List<PolicyDto> policies = policyService.searchPolicies(condition);
        model.addAttribute("policyList", policies);
        model.addAttribute("condition", condition);

        // 2) ✅ [추가됨] 즐겨찾기 상태 조회 로직
        // 로그인한 멤버 정보 가져오기
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        // 즐겨찾기한 정책 ID들을 담을 리스트 (기본값: 빈 리스트)
        List<String> favIds = new ArrayList<>();

        if (loginMember != null) {
            // 로그인 상태라면 DB에서 내가 찜한 정책 ID 목록 조회
            // (Mapper에 작성해둔 selectPlcyNosByMemberIdx 메소드 호출)
            favIds = favoriteMapper.selectPlcyNosByMemberIdx(loginMember.getMemberIdx());
        }

        // 3) 뷰(HTML)로 전달
        // Thymeleaf에서 th:classappend 로직에 사용됨
        model.addAttribute("favIds", favIds);

        return "policy";
    }


}