package com.simpol.polisight.controller;

import com.simpol.polisight.dto.MemberDto;
import com.simpol.polisight.dto.PolicyDto;
import com.simpol.polisight.service.FavoriteService;
import com.simpol.polisight.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private FavoriteService favoriteService; // ✅ [추가] 즐겨찾기 서비스 주입

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @PostMapping("/join")
    public String joinProcess(MemberDto memberDto) {
        memberService.join(memberDto);
        // [수정] ?signup=success 파라미터 추가 (이게 있어야 모달이 뜹니다)
        return "redirect:/login?signup=success";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/mypage")
    public String myPage(HttpSession session, Model model) { // ✅ [추가] Model 파라미터 필요

        // 1. 로그인 체크 (기존 코드)
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/login";

        // 2. 회원 정보 갱신 (기존 코드)
        MemberDto fresh = memberService.getMemberByEmail(loginMember.getEmail());
        if (fresh != null) {
            session.setAttribute("loginMember", fresh);
            loginMember = fresh; // 갱신된 정보로 변수 업데이트
        }

        // 3. ✅ [추가] 즐겨찾기 목록 가져오기
        List<PolicyDto> favoriteList = favoriteService.getFavoritePolicies(loginMember.getMemberIdx());

        // 4. ✅ [추가] 화면으로 전달
        model.addAttribute("favoriteList", favoriteList);

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

    // [추가] AJAX 요청용 내 정보 조회 API
    @GetMapping("/api/my-info")
    @ResponseBody
    public MemberDto getMyInfo(HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return null; // 로그인 안 된 상태면 null 반환
        }
        // 세션 정보보다 DB 최신 정보를 가져오는 것이 안전함
        return memberService.getMemberByEmail(loginMember.getEmail());
    }

    // ==========================================
    // 5. 회원 탈퇴 요청 처리 (연동 해제 포함)
    // ==========================================
    @PostMapping("/withdraw")
    public String withdraw(HttpSession session, RedirectAttributes rttr) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            // ★ 중요: 로그인할 때 세션에 저장해둔 액세스 토큰을 가져옵니다.
            String accessToken = (String) session.getAttribute("socialAccessToken");

            // 서비스 호출 (이메일, 제공자, 토큰 전달)
            memberService.withdraw(loginMember.getEmail(), loginMember.getProvider(), accessToken);

            // 세션 삭제
            session.invalidate();

            rttr.addFlashAttribute("msg", "회원 탈퇴 및 계정 연동 해제가 완료되었습니다.");
            return "redirect:/";

        } catch (Exception e) {
            e.printStackTrace();
            rttr.addFlashAttribute("msg", "탈퇴 처리 중 오류가 발생했습니다.");
            return "redirect:/mypage";
        }
    }
    // ==========================================
    // ✅ [추가] 회원가입 후 초기 셋업 (updateConditions 재활용)
    // ==========================================
    @PostMapping(value = "/user/setup/update", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> setupUpdate(@RequestBody MemberDto memberDto, HttpSession session) {

        // 기존에 만들어두신 마이페이지 업데이트 로직(updateConditions)을 그대로 호출합니다.
        // 로직이 완전히 동일하기 때문입니다.
        return updateConditions(memberDto, session);
    }
}
