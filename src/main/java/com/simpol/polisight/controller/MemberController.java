package com.simpol.polisight.controller;

import com.simpol.polisight.dto.MemberDto;
import com.simpol.polisight.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MemberController {

    @Autowired
    private MemberService memberService;

    // 1. 로그인/회원가입 페이지 이동
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // 2. 회원가입 처리
    @PostMapping("/join")
    public String joinProcess(MemberDto memberDto) {
        memberService.join(memberDto);
        return "redirect:/login";
    }

    // 3. 로그인 처리
    @PostMapping("/login")
    public String loginProcess(@RequestParam("email") String email,
                               @RequestParam("userPw") String userPw,
                               HttpSession session) {

        MemberDto loginMember = memberService.login(email, userPw);

        if (loginMember != null) {
            session.setAttribute("loginMember", loginMember);
            return "redirect:/policy"; // 로그인 성공 시 policy로 이동
        } else {
            return "redirect:/login?error=true";
        }
    }

    // 4. 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }


    // [추가됨] 6. 마이페이지 이동
    @GetMapping("/mypage")
    public String myPage(HttpSession session) {
        // 로그인 안 한 상태로 접근 시 로그인 페이지로 리다이렉트 (안전장치)
        if (session.getAttribute("loginMember") == null) {
            return "redirect:/login";
        }
        return "mypage"; // templates/mypage.html 반환
    }

    @GetMapping("/setup")
    public String setupPage() {
        return "setup";
    }
}