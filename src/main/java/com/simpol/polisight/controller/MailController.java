package com.simpol.polisight.controller;

import com.simpol.polisight.service.MailService;
import com.simpol.polisight.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;
    //  DB 조회를 위해 Service 주입
    private final MemberService memberService;

    // 토큰 저장소 (실무에서는 DB나 Redis를 씁니다)
    // "랜덤토큰" : "이메일" 형태로 저장됨
    public static Map<String, String> resetTokenStore = new ConcurrentHashMap<>();

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

    // 비밀번호 재설정 링크 발송 요청
    @ResponseBody
    @PostMapping("/mail/send-reset")
    public String sendResetLink(@RequestParam("mail") String mail) {

        // 1. 가입된 이메일인지 확인 (가입 안 된 사람이면 fail 리턴)
        if (!memberService.checkEmailDuplicate(mail)) {
            return "not_found";
        }

        // 2. 랜덤 토큰(암호표) 생성 (예: a1b2-c3d4...)
        String token = UUID.randomUUID().toString();

        // 3. 임시 저장소에 저장 (토큰으로 이메일을 찾기 위해)
        resetTokenStore.put(token, mail);

        // 4. 메일 발송
        mailService.sendResetMail(mail, token);

        return "success";
    }
}