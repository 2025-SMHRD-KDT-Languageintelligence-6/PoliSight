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

    // 2. 회원가입 처리 (POST)
    @PostMapping("/join")
    public String joinProcess(MemberDto memberDto) {
        System.out.println("== 회원가입 요청 ==");
        System.out.println("Email: " + memberDto.getEmail());
        System.out.println("Name: " + memberDto.getUserName());

        // 서비스 호출 -> DB 저장
        memberService.join(memberDto);

        System.out.println("회원가입 완료! 로그인 페이지로 이동합니다.");
        return "redirect:/login";
    }

    // 3. 로그인 처리 (POST)
    @PostMapping("/login")
    public String loginProcess(@RequestParam("email") String email,
                               @RequestParam("userPw") String userPw,
                               HttpSession session) {

        System.out.println("== 로그인 시도 ==");
        System.out.println("Email: " + email);

        MemberDto loginMember = memberService.login(email, userPw);

        if (loginMember != null) {
            // 로그인 성공 -> 세션에 정보 저장
            session.setAttribute("loginMember", loginMember);
            System.out.println("로그인 성공: " + loginMember.getMemberName() + "님 환영합니다.");
            return "redirect:/"; // 메인 페이지로 이동
        } else {
            // 로그인 실패
            System.out.println("로그인 실패: 아이디/비번 불일치");
            return "redirect:/login?error=true";
        }
    }

    // 4. 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/setup")
    public String setupPage() {
        return "setup";
    }
}