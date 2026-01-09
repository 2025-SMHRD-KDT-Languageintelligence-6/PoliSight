package com.simpol.polisight.controller;

import com.simpol.polisight.dto.MemberDto;
import com.simpol.polisight.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.PrintWriter;

@Controller
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @PostMapping("/join")
    public String joinProcess(MemberDto memberDto) {
        memberService.join(memberDto);
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String loginProcess(@RequestParam("email") String email,
                               @RequestParam("userPw") String userPw,
                               HttpSession session) {
        MemberDto loginMember = memberService.login(email, userPw);
        if (loginMember != null) {
            session.setAttribute("loginMember", loginMember);
            return "redirect:/policy";
        } else {
            return "redirect:/login?error=true";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/mypage")
    public String myPage(HttpSession session) {
        if (session.getAttribute("loginMember") == null) {
            return "redirect:/login";
        }
        return "mypage";
    }

    // [추가됨] 회원 정보 수정 처리
    @PostMapping("/updateMember")
    public String updateMemberProcess(MemberDto memberDto, HttpSession session, HttpServletResponse response) throws IOException {

        try {
            // 서비스 호출
            MemberDto updatedMember = memberService.updateMember(memberDto);

            if (updatedMember != null) {
                // 성공 시 세션 정보 갱신 (중요: 이걸 안 하면 로그아웃 했다 들어와야 바뀜)
                session.setAttribute("loginMember", updatedMember);

                // 알림창 띄우고 마이페이지로 이동
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.println("<script>alert('회원 정보가 수정되었습니다.'); location.href='/mypage';</script>");
                out.flush();
                return null; // 위에서 직접 응답을 보냈으므로 null 반환
            }
        } catch (IllegalArgumentException e) {
            // 비밀번호 불일치 예외 처리
            response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('" + e.getMessage() + "'); history.back();</script>");
            out.flush();
            return null;
        }

        return "redirect:/mypage";
    }

    @GetMapping("/setup")
    public String setupPage() { return "setup"; }
}