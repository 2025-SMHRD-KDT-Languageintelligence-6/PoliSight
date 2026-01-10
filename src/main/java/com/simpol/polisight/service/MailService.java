package com.simpol.polisight.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private static final String SENDER_EMAIL = "bsb0107p@gmail.com"; // application.properties와 동일하게

    // 1. 랜덤 번호 생성
    public String createNumber() {
        Random random = new Random();
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            key.append(random.nextInt(10));
        }
        return key.toString();
    }

    // 2. 메일 생성 및 전송
    public String sendMail(String mail) {
        String authNum = createNumber();
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(SENDER_EMAIL, "PoliSight");
            helper.setTo(mail);
            helper.setSubject("[PoliSight] 회원가입 인증번호");
            String body = "";
            body += "<h3>요청하신 인증 번호입니다.</h3>";
            body += "<h1>" + authNum + "</h1>";
            body += "<h3>감사합니다.</h3>";
            helper.setText(body, true);

            javaMailSender.send(message);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return authNum; // 생성된 번호를 컨트롤러로 리턴
    }

    // 비밀번호 재설정 링크 발송
    public void sendResetMail(String mail, String token) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(SENDER_EMAIL, "PoliSight");
            helper.setTo(mail.trim());
            helper.setSubject("[PoliSight] 비밀번호 재설정 링크");

            // 내 서버 포트가 8089라고 가정 (변경 필요시 수정)
            String resetLink = "http://localhost:8089/user/reset-pw?token=" + token;

            String body = "";
            body += "<h3>비밀번호 재설정</h3>";
            body += "<p>아래 링크를 클릭하여 비밀번호를 재설정하세요.</p>";
            body += "<a href='" + resetLink + "'>비밀번호 변경하기</a>";
            body += "<p>이 링크는 10분간 유효합니다.</p>";

            helper.setText(body, true);

            javaMailSender.send(message);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("메일 발송 실패", e);
        }
    }
}