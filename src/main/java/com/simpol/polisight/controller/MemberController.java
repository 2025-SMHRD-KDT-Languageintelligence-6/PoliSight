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

    // ==========================================
    // [추가] 1. 이름 변경 (자바스크립트 프롬프트 요청 처리)
    // ==========================================
    @PostMapping("/updateName")
    public String updateName(@RequestParam("userName") String userName,
                             HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/login";

        // 서비스 호출
        MemberDto updatedMember = memberService.updateName(loginMember.getEmail(), userName);

        // 세션 정보 갱신 (이름 변경 반영)
        if (updatedMember != null) {
            session.setAttribute("loginMember", updatedMember);
        }

        return "redirect:/mypage";
    }

    // ==========================================
    // [추가] 2. 비밀번호 변경 (마이페이지 폼 요청 처리)
    // ==========================================
    @PostMapping("/updatePassword")
    public String updatePassword(@RequestParam("currentPw") String currentPw,
                                 @RequestParam("newPw") String newPw,
                                 HttpSession session,
                                 HttpServletResponse response) throws IOException {

        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/login";

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // 서비스 호출 (현재 비번 확인 -> 새 비번 변경)
            boolean result = memberService.changePassword(loginMember.getEmail(), currentPw, newPw);

            if (result) {
                out.println("<script>alert('비밀번호가 성공적으로 변경되었습니다.'); location.href='/mypage';</script>");
            } else {
                out.println("<script>alert('현재 비밀번호가 일치하지 않습니다.'); history.back();</script>");
            }
        } catch (Exception e) {
            out.println("<script>alert('오류가 발생했습니다: " + e.getMessage() + "'); history.back();</script>");
        }

        out.flush();
        return null; // 직접 응답을 작성했으므로 뷰 리턴 없음
    }

    // (기존 회원수정 로직 - 혹시 모를 상황 대비 유지하거나 삭제해도 됨)
    @PostMapping("/updateMember")
    public String updateMemberProcess(MemberDto memberDto, HttpSession session, HttpServletResponse response) throws IOException {
        // 기존 로직 유지...
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
        if (!MailController.isResetTokenValid(token)) {
            return "redirect:/?error=invalid_or_expired_token";
        }
        model.addAttribute("token", token);
        return "reset_pw";
    }

    // 2. 실제 비밀번호 변경 처리 (이메일 찾기용)
    @PostMapping("/user/update-pw")
    public String updatePwProcess(@RequestParam("token") String token,
                                  @RequestParam("newPw") String newPw) {
        if (!MailController.isResetTokenValid(token)) {
            return "redirect:/?error=invalid_or_expired_token";
        }
        String email = MailController.getEmailByToken(token);
        if (email == null) {
            return "redirect:/?error=session_expired";
        }
        // 여기서는 기존 비밀번호 체크 없이 강제 변경 (분실 상황이므로)
        memberService.updatePassword(email, newPw);
        MailController.resetTokenStore.remove(token);
        return "redirect:/user/pw-success";
    }

    @GetMapping("/user/pw-success")
    public String successPage() {
        return "pw_success";
    }
}