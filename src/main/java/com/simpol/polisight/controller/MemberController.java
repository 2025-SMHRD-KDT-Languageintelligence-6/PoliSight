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
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // [중요] 필수 임포트

import java.io.IOException;

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
    // [수정] 1. 이름 변경 (RedirectAttributes 사용)
    // ==========================================
    @PostMapping("/updateName")
    public String updateName(@RequestParam("userName") String userName,
                             HttpSession session,
                             RedirectAttributes rttr) { // RedirectAttributes 추가

        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/login";

        // 서비스 호출
        MemberDto updatedMember = memberService.updateName(loginMember.getEmail(), userName);

        if (updatedMember != null) {
            // 세션 정보 갱신 (이름 변경 반영)
            session.setAttribute("loginMember", updatedMember);
            // 성공 메시지 전달 (HTML에서 ${msg}로 받음)
            rttr.addFlashAttribute("msg", "이름이 성공적으로 변경되었습니다.");
        } else {
            rttr.addFlashAttribute("msg", "이름 변경에 실패했습니다.");
        }

        return "redirect:/mypage";
    }

    // ==========================================
    // [수정] 2. 비밀번호 변경 (RedirectAttributes 사용)
    // ==========================================
    @PostMapping("/updatePassword")
    public String updatePassword(@RequestParam("currentPw") String currentPw,
                                 @RequestParam("newPw") String newPw,
                                 HttpSession session,
                                 RedirectAttributes rttr) { // RedirectAttributes 추가

        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/login";

        try {
            // 서비스 호출 (현재 비번 확인 -> 새 비번 변경)
            boolean result = memberService.changePassword(loginMember.getEmail(), currentPw, newPw);

            if (result) {
                rttr.addFlashAttribute("msg", "비밀번호가 성공적으로 변경되었습니다.");
            } else {
                rttr.addFlashAttribute("msg", "현재 비밀번호가 일치하지 않습니다.");
            }
        } catch (Exception e) {
            rttr.addFlashAttribute("msg", "오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/mypage";
    }

    // (기존 회원수정 로직 - RedirectAttributes로 변경)
    @PostMapping("/updateMember")
    public String updateMemberProcess(MemberDto memberDto, HttpSession session, RedirectAttributes rttr) {

        try {
            MemberDto updatedMember = memberService.updateMember(memberDto);

            if (updatedMember != null) {
                session.setAttribute("loginMember", updatedMember);
                rttr.addFlashAttribute("msg", "회원 정보가 수정되었습니다.");
            }
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("msg", e.getMessage());
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

    // 2. 실제 비밀번호 변경 처리
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

        memberService.updatePassword(email, newPw);
        MailController.resetTokenStore.remove(token);

        return "redirect:/user/pw-success";
    }

    @GetMapping("/user/pw-success")
    public String successPage() {
        return "pw_success";
    }
}