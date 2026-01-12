// [컨트롤러] MemberController (변경된 전체)
package com.simpol.polisight.controller;

import com.simpol.polisight.dto.MemberDto;
import com.simpol.polisight.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @PostMapping("/updateMember")
    public String updateMemberProcess(MemberDto memberDto, HttpSession session, HttpServletResponse response) throws IOException {

        try {
            MemberDto updatedMember = memberService.updateMember(memberDto);

            if (updatedMember != null) {
                session.setAttribute("loginMember", updatedMember);

                response.setContentType("text/html; charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.println("<script>alert('회원 정보가 수정되었습니다.'); location.href='/mypage';</script>");
                out.flush();
                return null;
            }
        } catch (IllegalArgumentException e) {
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

    // 1. 이메일 링크 클릭 → 비밀번호 변경 페이지
    @GetMapping("/user/reset-pw")
    public String resetPwPage(@RequestParam("token") String token, Model model) {

        // ===== [변경] 만료/무효 토큰이면 메인으로 =====
        if (!MailController.isResetTokenValid(token)) {
            return "redirect:/?error=invalid_or_expired_token";
        }

        model.addAttribute("token", token);
        return "reset_pw";
    }

    // 2. 실제 비밀번호 변경 처리
    @PostMapping("/user/update-pw")
    public String updatePwProcess(@RequestParam("token") String token,
                                  @RequestParam("newPw") String newPw) {

        // ===== [추가] 여기서도 한 번 더 만료 체크(중요) =====
        if (!MailController.isResetTokenValid(token)) {
            return "redirect:/?error=invalid_or_expired_token";
        }

        // 토큰으로 이메일 찾기
        String email = MailController.getEmailByToken(token);
        if (email == null) {
            return "redirect:/?error=session_expired";
        }

        // DB 비밀번호 업데이트
        memberService.updatePassword(email, newPw);

        // 사용한 토큰 삭제(재사용 방지)
        MailController.resetTokenStore.remove(token);

        return "redirect:/user/pw-success";
    }

    @GetMapping("/user/pw-success")
    public String successPage() {
        return "pw_success";
    }
}
