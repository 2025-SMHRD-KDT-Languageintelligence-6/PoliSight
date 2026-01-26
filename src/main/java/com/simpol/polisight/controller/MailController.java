// [컨트롤러] MailController (변경된 전체)
package com.simpol.polisight.controller;

import com.simpol.polisight.dto.MemberDto;
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
    private final MemberService memberService;

    // ===== [변경] 토큰 저장 구조: email + expiresAt =====
    public static class ResetTokenInfo {
        private final String email;
        private final long expiresAt; // 만료 시각(ms)

        public ResetTokenInfo(String email, long expiresAt) {
            this.email = email;
            this.expiresAt = expiresAt;
        }
        public String getEmail() { return email; }
        public long getExpiresAt() { return expiresAt; }
    }

    // "토큰" : ResetTokenInfo
    public static Map<String, ResetTokenInfo> resetTokenStore = new ConcurrentHashMap<>();

    // 10분 TTL
    private static final long RESET_TOKEN_TTL_MS = 10 * 60 * 1000L;

    // 1. 이메일 인증 요청 (메일 전송)
    @ResponseBody
    @PostMapping("/mail/send")
    public String MailSend(@RequestParam("mail") String mail, HttpSession session) {

        // DB에 이미 존재하는 이메일인지 확인
        if (memberService.checkEmailDuplicate(mail)) {
            return "duplicate";
        }

        String authCode = mailService.sendMail(mail);

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
            session.setAttribute("isVerified", true);
            return "ok";
        } else {
            return "fail";
        }
    }

    // 비밀번호 재설정 링크 발송 요청
    @ResponseBody
    @PostMapping("/mail/send-reset")
    public String sendResetLink(@RequestParam("mail") String mail) {

        MemberDto member = memberService.getMemberByEmail(mail);

        // 가입된 이메일인지 확인
        if (member == null) {
            return "not_found";
        }

        String provider = member.getProvider();

        if (provider != null && !provider.equals("polisight")) {
            return "social_user:" + provider; // 예: "social_user:kakao"
        }

        // 토큰 생성
        String token = UUID.randomUUID().toString();

        // ===== [추가] 10분 만료시간 저장 =====
        long expiresAt = System.currentTimeMillis() + RESET_TOKEN_TTL_MS;
        resetTokenStore.put(token, new ResetTokenInfo(mail, expiresAt));

        // 메일 발송
        mailService.sendResetMail(mail, token);

        return "success";
    }

    // ===== [추가] 토큰 유효성 검사(만료되면 자동 삭제) =====
    public static boolean isResetTokenValid(String token) {
        ResetTokenInfo info = resetTokenStore.get(token);
        if (info == null) return false;

        if (System.currentTimeMillis() > info.getExpiresAt()) {
            resetTokenStore.remove(token); // 만료된 토큰은 삭제
            return false;
        }
        return true;
    }

    // ===== [추가] 토큰으로 이메일 꺼내기 =====
    public static String getEmailByToken(String token) {
        ResetTokenInfo info = resetTokenStore.get(token);
        return (info == null) ? null : info.getEmail();
    }
}
