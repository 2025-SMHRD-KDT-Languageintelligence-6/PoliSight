package com.simpol.polisight.controller;

import com.simpol.polisight.dto.MemberDto;
import com.simpol.polisight.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/login";

        MemberDto fresh = memberService.getMemberByEmail(loginMember.getEmail());
        if (fresh != null) session.setAttribute("loginMember", fresh);

        return "mypage";
    }

    // ==========================================
    // 이름 변경
    // ==========================================
    @PostMapping("/updateName")
    public String updateName(@RequestParam("userName") String userName,
                             HttpSession session,
                             RedirectAttributes rttr) {

        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/login";

        MemberDto updatedMember = memberService.updateName(loginMember.getEmail(), userName);

        if (updatedMember != null) {
            session.setAttribute("loginMember", updatedMember);
            rttr.addFlashAttribute("msg", "이름이 성공적으로 변경되었습니다.");
        } else {
            rttr.addFlashAttribute("msg", "이름 변경에 실패했습니다.");
        }

        return "redirect:/mypage";
    }

    // ==========================================
    // 비밀번호 변경
    // ==========================================
    @PostMapping("/updatePassword")
    public String updatePassword(@RequestParam("currentPw") String currentPw,
                                 @RequestParam("newPw") String newPw,
                                 HttpSession session,
                                 RedirectAttributes rttr) {

        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/login";

        try {
            boolean result = memberService.changePassword(loginMember.getEmail(), currentPw, newPw);

            if (result) rttr.addFlashAttribute("msg", "비밀번호가 성공적으로 변경되었습니다.");
            else rttr.addFlashAttribute("msg", "현재 비밀번호가 일치하지 않습니다.");

        } catch (Exception e) {
            rttr.addFlashAttribute("msg", "오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/mypage";
    }

    // ==========================================
    // (기존) 회원정보 수정
    // ==========================================
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

    // ==========================================
    // ✅ 마이페이지 "내 조건" 저장 (JSON fetch 대응)
    // ==========================================
    @PostMapping(value = "/updateConditions", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> updateConditions(@RequestBody MemberDto memberDto,
                                                   HttpSession session) {

        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(401).body("unauthorized");
        }

        // 이메일은 세션 기준으로 고정
        memberDto.setEmail(loginMember.getEmail());

        try {
            MemberDto updated = memberService.updateConditions(memberDto);
            if (updated != null) {
                session.setAttribute("loginMember", updated);
                return ResponseEntity.ok("success");
            }
            return ResponseEntity.ok("fail");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("error:" + e.getMessage());
        }
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
