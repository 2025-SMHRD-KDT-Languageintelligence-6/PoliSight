package com.simpol.polisight.controller;

import com.simpol.polisight.service.MailService;
import com.simpol.polisight.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;
    // [추가] DB 조회를 위해 Service 주입
    private final MemberService memberService;

    // 1. 이메일 인증 요청 (메일 전송)
    @ResponseBody
    @PostMapping("/mail/send")
    public String MailSend(@RequestParam("mail") String mail, HttpSession session) {

        // [중요] DB에 이미 존재하는 이메일인지 확인
        // (MemberService에 existsByEmail 같은 중복 확인 메소드가 있다고 가정합니다)
        if (memberService.checkEmailDuplicate(mail)) {
            return "duplicate"; // 중복이면 'duplicate' 문자열 반환
        }

        String authCode = mailService.sendMail(mail);

        // 세션에 인증코드 저장 (나중에 검증하기 위해)
        session.setAttribute("authCode", authCode);
        session.setMaxInactiveInterval(300); // 5분 유지

        return "success";
    }

    // 2. 인증번호 확인
    @ResponseBody
    @PostMapping("/mail/verify")
    public String MailVerify(@RequestParam("code") String code, HttpSession session) {
        String savedCode = (String) session.getAttribute("authCode");

        if (savedCode != null && savedCode.equals(code)) {
            session.setAttribute("isVerified", true); // 인증 완료 플래그
            return "ok";
        } else {
            return "fail";
        }
    }
}