package com.simpol.polisight.service;

import com.simpol.polisight.dto.MemberDto;
import com.simpol.polisight.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream; // ★ 추가
import java.io.BufferedReader; // ★ 추가
import java.io.InputStreamReader; // ★ 추가
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        MemberDto member = memberMapper.selectMemberByEmail(email);
        if (member == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
        }
        return User.builder()
                .username(member.getEmail())
                .password(member.getPasswordHash())
                .roles("USER")
                .build();
    }

    // 1. 회원가입
    public void join(MemberDto dto) {
        String rawPw = dto.getUserPw();
        String encodedPw = passwordEncoder.encode(rawPw);
        dto.setPasswordHash(encodedPw);
        dto.setMemberName(dto.getUserName());

        // ▼▼▼ [수정된 날짜 처리 로직] ▼▼▼

        // case 1: 년/월/일이 따로 들어온 경우 (기존 로직 유지)
        if (isValidDateInput(dto)) {
            String combinedDate = combineDate(dto.getBirthYear(), dto.getBirthMonth(), dto.getBirthDay());
            dto.setBirthDate(combinedDate);
        }
        // case 2: 'birthDate' 필드에 8자리 숫자 문자열("19950101")이 들어온 경우 (회원가입 시)
        else if (dto.getBirthDate() != null) {
            // 숫자만 추출
            String rawDate = dto.getBirthDate().replaceAll("[^0-9]", "");

            if (rawDate.length() == 8) {
                // "19950101" -> "1995-01-01" 로 변환하여 저장
                String formattedDate = String.format("%s-%s-%s",
                        rawDate.substring(0, 4),
                        rawDate.substring(4, 6),
                        rawDate.substring(6, 8));
                dto.setBirthDate(formattedDate);
            }
            // 길이가 8이 아니거나 비어있으면 null 혹은 그대로 둠
        }
        // case 3: 아무것도 없으면 null
        else {
            dto.setBirthDate(null);
        }

        dto.setProvider("PoliSight");
        memberMapper.insertMember(dto);
    }

    // 2. 로그인
    public MemberDto login(String email, String inputPw) {
        MemberDto member = memberMapper.selectMemberByEmail(email);
        if (member != null && passwordEncoder.matches(inputPw, member.getPasswordHash())) {
            return member;
        }
        return null;
    }

    // [수정] 이름 변경 (기존 updateMember 호출하던 것을 교체)
    public MemberDto updateName(String email, String newName) {
        // DB 조회 확인 (없으면 null)
        MemberDto member = memberMapper.selectMemberByEmail(email);
        if (member == null) return null;

        // ★ 여기서 새 메서드 호출! (DTO 세팅 필요 없음)
        memberMapper.updateMemberName(email, newName);

        // 변경된 정보 다시 조회해서 반환
        return memberMapper.selectMemberByEmail(email);
    }

    // 비밀번호 변경 (마이페이지)
    public boolean changePassword(String email, String currentPw, String newPw) {
        MemberDto member = memberMapper.selectMemberByEmail(email);
        if (member == null) return false;

        if (!passwordEncoder.matches(currentPw, member.getPasswordHash())) return false;

        String encodedNewPw = passwordEncoder.encode(newPw);
        memberMapper.updatePassword(email, encodedNewPw);
        return true;
    }


    // 비밀번호 재설정(이메일)
    public void updatePassword(String email, String newPw) {
        String encodedPw = passwordEncoder.encode(newPw);
        memberMapper.updatePassword(email, encodedPw);
    }

    // ✅ [추가] 마이페이지 "내 조건" 업데이트
    @Transactional
    public MemberDto updateConditions(MemberDto dto) {
        MemberDto dbMember = memberMapper.selectMemberByEmail(dto.getEmail());
        if (dbMember == null) return null;

        // 조건만 업데이트 (이름/비번/provider 등은 건드리지 않음)
        memberMapper.updateConditions(dto);

        return memberMapper.selectMemberByEmail(dto.getEmail());
    }

    public boolean checkEmailDuplicate(String email) {
        return memberMapper.countByEmail(email) > 0;
    }

    public MemberDto getMemberByEmail(String email) {
        return memberMapper.selectMemberByEmail(email);
    }

    // 회원 탈퇴 기능
    // [회원 탈퇴 통합 메서드]
    @Transactional
    public void withdraw(String email, String provider, String accessToken) {
        // 1. 소셜 계정이면 연동 해제 요청 먼저 수행
        if (provider != null && accessToken != null && !accessToken.isEmpty()) {
            if (provider.equals("kakao")) {
                unlinkKakao(accessToken);
            } else if (provider.equals("google")) {
                unlinkGoogle(accessToken);
            }
        }

        // 2. 연동 해제 성공(혹은 일반회원) 후 DB 삭제
        memberMapper.deleteMember(email);
    }

    // --- [카카오 연동 해제] ---
    private void unlinkKakao(String accessToken) {
        try {
            // [수정] new URL(...) 대신 URI.create(...).toURL() 사용
            URL url = URI.create("https://kapi.kakao.com/v1/user/unlink").toURL();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = conn.getResponseCode();
            System.out.println("카카오 연동 해제 결과: " + responseCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- [구글 연동 해제 (수정판)] ---
    private void unlinkGoogle(String accessToken) {
        try {
            // 1. URL 설정 (쿼리 파라미터 없이 깔끔하게)
            URL url = URI.create("https://oauth2.googleapis.com/revoke").toURL();

            // 2. 연결 설정
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true); // POST Body에 데이터를 쓰겠다는 설정

            // 3. 데이터 전송 (Body에 token 담기)
            String data = "token=" + accessToken;
            try (OutputStream os = conn.getOutputStream()) {
                os.write(data.getBytes());
                os.flush();
            }

            // 4. 응답 확인 (로그 찍기)
            int responseCode = conn.getResponseCode();
            System.out.println("구글 연동 해제 요청 결과 코드: " + responseCode);

            // 성공(200)이 아니면 에러 메시지 읽어보기 (디버깅용)
            if (responseCode != 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    System.out.println("구글 에러 응답: " + response.toString());
                }
            }
        } catch (Exception e) {
            System.out.println("구글 연동 해제 중 예외 발생 (DB 삭제는 진행됨)");
            e.printStackTrace();
        }
    }

    // --- 유틸 ---
    private boolean isValidDateInput(MemberDto dto) {
        return dto.getBirthYear() != null && !dto.getBirthYear().isEmpty() &&
                dto.getBirthMonth() != null && !dto.getBirthMonth().isEmpty() &&
                dto.getBirthDay() != null && !dto.getBirthDay().isEmpty();
    }

    private String combineDate(String y, String m, String d) {
        return String.format("%s-%02d-%02d", y, Integer.parseInt(m), Integer.parseInt(d));
    }
}
