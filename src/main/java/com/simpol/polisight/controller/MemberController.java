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

    // 1. 이메일 링크를 클릭했을 때 페이지 보여주기
    @GetMapping("/user/reset-pw")
    public String resetPwPage(@RequestParam("token") String token, Model model) {
        // 토큰 검증: 저장소에 없는 토큰이면 에러 페이지나 홈으로 튕기기
        if (!MailController.resetTokenStore.containsKey(token)) {
            return "redirect:/?error=invalid_token"; // 홈으로 리다이렉트
        }

        // HTML에 토큰을 전달 (나중에 POST 할 때 쓰려고)
        model.addAttribute("token", token);
        return "reset_pw"; // reset_pw.html 열기
    }

    // 2. 실제 비밀번호 변경 요청 처리
    @PostMapping("/user/update-pw")
    public String updatePwProcess(@RequestParam("token") String token,
                                  @RequestParam("newPw") String newPw) {

        // 토큰으로 이메일 찾기
        String email = MailController.resetTokenStore.get(token);

        if (email == null) {
            return "redirect:/?error=session_expired";
        }

        // DB 비밀번호 업데이트
        memberService.updatePassword(email, newPw);

        // 사용한 토큰 삭제 (재사용 방지)
        MailController.resetTokenStore.remove(token);

        // 로그인 페이지로 이동 (성공 메시지 전달)
        return "redirect:/?message=pw_changed";
    }
}