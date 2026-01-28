package com.simpol.polisight.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    // application.properties에서 설정한 주소를 가져옵니다.
    @Value("${site.base-url}")
    private String baseUrl;

    // 보내는 사람 이메일 (application.properties와 동일하게 설정)
    private static final String SENDER_EMAIL = "bsb0107p@gmail.com";

    // 1. 랜덤 인증번호 6자리 생성
    public String createNumber() {
        Random random = new Random();
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            key.append(random.nextInt(10));
        }
        return key.toString();
    }

    // 2. 회원가입 인증번호 메일 발송
    public String sendMail(String mail) {
        String authNum = createNumber();
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(SENDER_EMAIL, "PoliSight");
            helper.setTo(mail);
            helper.setSubject("[PoliSight] 회원가입 인증번호 안내");

            String body = getHtmlHeader();
            body += "  <div style='padding: 30px 20px; text-align: center;'>";
            body += "    <h2 style='font-size: 18px; font-weight: bold; color: #333; margin-bottom: 10px;'>안녕하세요.</h2>";
            body += "    <p style='font-size: 16px; color: #555; line-height: 1.6; margin-bottom: 20px;'>";
            body += "      PoliSight 가입을 진행해 주셔서 감사합니다.<br>아래 <strong>인증번호 6자리</strong>를 입력하여 본인 인증을 완료해 주세요.";
            body += "    </p>";
            body += "    <div style='background-color: #f4f7f9; border-radius: 8px; padding: 20px; display: inline-block; margin: 10px 0;'>";
            body += "      <span style='font-size: 32px; font-weight: 900; color: #007bff; letter-spacing: 4px;'>" + authNum + "</span>";
            body += "    </div>";
            body += "    <p style='font-size: 13px; color: #888; margin-top: 20px;'>이 인증번호는 <strong>5분간</strong> 유효합니다.</p>";
            body += "  </div>";
            body += getHtmlFooter();

            helper.setText(body, true);
            javaMailSender.send(message);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return authNum;
    }

    // 3. 비밀번호 재설정 링크 발송
    public void sendResetMail(String mail, String token) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(SENDER_EMAIL, "PoliSight");
            helper.setTo(mail.trim());
            helper.setSubject("[PoliSight] 비밀번호 재설정 요청");

            String resetLink = baseUrl + "/user/reset-pw?token=" + token;

            String body = getHtmlHeader();
            body += "  <div style='padding: 30px 20px; text-align: center;'>";
            body += "    <h2 style='font-size: 20px; font-weight: bold; color: #333; margin-bottom: 20px;'>비밀번호 재설정</h2>";
            body += "    <p style='font-size: 15px; color: #666; line-height: 1.6; margin-bottom: 30px;'>";
            body += "      비밀번호 재설정 요청을 받았습니다.<br>아래 버튼을 클릭하여 새로운 비밀번호를 설정해 주세요.";
            body += "    </p>";
            body += "    <a href='" + resetLink + "' style='background-color: #007bff; color: white; text-decoration: none; padding: 14px 24px; border-radius: 8px; font-size: 16px; font-weight: bold; display: inline-block; box-shadow: 0 4px 6px rgba(0,123,255,0.2);'>비밀번호 변경하기</a>";
            body += "    <p style='margin-top: 30px; font-size: 13px; color: #999; line-height: 1.5;'>";
            body += "      ⚠️ 본인이 요청하지 않았다면 이 메일을 무시하세요.<br>이 링크는 10분 동안만 유효합니다.";
            body += "    </p>";
            body += "  </div>";
            body += getHtmlFooter();

            helper.setText(body, true);
            javaMailSender.send(message);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("메일 발송 실패", e);
        }
    }

    // 4. [수정됨] 마감 임박 알림 메일 발송 (HTML 디자인 적용)
    public void sendDeadlineNotification(String toEmail, String policyName, String policyNo) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(SENDER_EMAIL, "PoliSight");
            helper.setTo(toEmail);
            helper.setSubject("[PoliSight] 마감 임박 알림: '" + policyName + "' 신청이 3일 남았습니다.");

            String policyLink = baseUrl + "/policy";

            String body = getHtmlHeader();
            body += "  <div style='padding: 30px 20px; text-align: center;'>";
            body += "    <div style='font-size: 40px; margin-bottom: 15px;'>⏰</div>";
            body += "    <h2 style='font-size: 20px; font-weight: bold; color: #333; margin-bottom: 20px;'>신청 마감이 얼마 남지 않았어요!</h2>";
            body += "    <p style='font-size: 15px; color: #555; line-height: 1.6; margin-bottom: 25px;'>";
            body += "      안녕하세요. PoliSight에서 알려드립니다.<br>";
            body += "      관심 정책으로 등록하신 <strong>[" + policyName + "]</strong>의<br>";
            body += "      신청 마감일이 <strong>3일 전</strong>으로 다가왔습니다.";
            body += "    </p>";
            body += "    <div style='background-color: #f8f9fa; border-left: 4px solid #007bff; padding: 15px; text-align: left; margin: 0 auto 30px auto; max-width: 400px;'>";
            body += "      <div style='font-size: 13px; color: #888; margin-bottom: 5px;'>정책명</div>";
            body += "      <div style='font-size: 16px; color: #333; font-weight: bold;'>" + policyName + "</div>";
            body += "    </div>";
            body += "    <a href='" + policyLink + "' style='background-color: #007bff; color: white; text-decoration: none; padding: 14px 24px; border-radius: 8px; font-size: 16px; font-weight: bold; display: inline-block; box-shadow: 0 4px 6px rgba(0,123,255,0.2);'>지금 바로 확인하기</a>";
            body += "    <p style='margin-top: 30px; font-size: 12px; color: #999;'>";
            body += "      기한 내에 신청하여 혜택을 놓치지 마세요!<br>";
            body += "      알림 수신 거부는 마이페이지 > 알림 관리에서 가능합니다.";
            body += "    </p>";
            body += "  </div>";
            body += getHtmlFooter();

            helper.setText(body, true);
            javaMailSender.send(message);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    // 공통 헤더 (로고 포함)
    private String getHtmlHeader() {
        String header = "<div style='font-family: \"Pretendard\", -apple-system, BlinkMacSystemFont, \"Malgun Gothic\", sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 12px; background-color: #ffffff;'>";
        header += "  <div style='text-align: center; padding-bottom: 20px; border-bottom: 1px solid #f0f0f0;'>";
        header += "    <table align='center' cellpadding='0' cellspacing='0' style='margin: 0 auto;'>";
        header += "      <tr>";
        header += "        <td style='width: 36px; height: 36px; background-color: #007bff; border-radius: 8px; text-align: center; vertical-align: middle;'>";
        header += "          <span style='color: #ffffff; font-size: 22px; font-weight: 900; font-family: sans-serif; line-height: 36px; display: block;'>P</span>";
        header += "        </td>";
        header += "        <td style='width: 10px;'></td>";
        header += "        <td style='vertical-align: middle;'>";
        header += "          <span style='color: #007bff; font-size: 30px; font-weight: 900; letter-spacing: -1px; font-family: \"Pretendard\", sans-serif;'>PoliSight</span>";
        header += "        </td>";
        header += "      </tr>";
        header += "    </table>";
        header += "  </div>";
        return header;
    }

    // 공통 푸터
    private String getHtmlFooter() {
        String footer = "  <div style='margin-top: 20px; border-top: 1px solid #f0f0f0; padding-top: 20px; text-align: center; font-size: 12px; color: #aaa;'>";
        footer += "    본 메일은 발신 전용이며, 회신되지 않습니다.<br>© 2026 PoliSight. All rights reserved.";
        footer += "  </div>";
        footer += "</div>";
        return footer;
    }
}