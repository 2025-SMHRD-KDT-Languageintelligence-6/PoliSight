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

        // ✅ [수정] 로그인 정보를 가져와서 검색 조건(condition)에 추가
        // 이렇게 해야 DB(XML)에서 "누가" 즐겨찾기 했는지 알 수 있음
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        List<String> favIds = new ArrayList<>();

        if (loginMember != null) {
            // 1) 정렬을 위해 condition에 ID 주입
            condition.setMemberIdx(loginMember.getMemberIdx());

            // 2) 화면 별표 표시를 위해 즐겨찾기 목록 별도 조회
            favIds = favoriteMapper.selectPlcyNosByMemberIdx(loginMember.getMemberIdx());
        }

        // 서비스 호출 (memberIdx가 담긴 condition 전달)
        List<PolicyDto> policies = policyService.searchPolicies(condition);

        model.addAttribute("policyList", policies);
        model.addAttribute("condition", condition);
        model.addAttribute("favIds", favIds);

        return "policy";
    }
}