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

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService { // 1. 시큐리티 인터페이스 추가

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    // =============================================================
    // ★ [추가] 스프링 시큐리티 로그인 처리 메서드
    // 이 메서드가 있어야 SecurityConfig의 빨간 줄이 사라집니다.
    // =============================================================
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        MemberDto member = memberMapper.selectMemberByEmail(email);

        if (member == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
        }

        return User.builder()
                .username(member.getEmail())
                .password(member.getPasswordHash()) // DB의 암호화된 비번 사용
                .roles("USER")
                .build();
    }

    // =============================================================
    // ★ 기존 사용자 로직 (그대로 유지됨)
    // =============================================================

    // 1. 회원가입
    public void join(MemberDto dto) {
        String rawPw = dto.getUserPw();
        String encodedPw = passwordEncoder.encode(rawPw);
        dto.setPasswordHash(encodedPw);
        dto.setMemberName(dto.getUserName());

        if (isValidDateInput(dto)) {
            String combinedDate = combineDate(dto.getBirthYear(), dto.getBirthMonth(), dto.getBirthDay());
            dto.setBirthDate(combinedDate);
        } else {
            dto.setBirthDate(null);
        }

        dto.setProvider("PoliSight");
        memberMapper.insertMember(dto);
    }

    // 2. 로그인 (기존 컨트롤러에서 호출할 때 사용)
    public MemberDto login(String email, String inputPw) {
        MemberDto member = memberMapper.selectMemberByEmail(email);
        if (member != null && passwordEncoder.matches(inputPw, member.getPasswordHash())) {
            return member;
        }
        return null;
    }

    // 3. 회원 정보 수정
    public MemberDto updateMember(MemberDto dto) {
        MemberDto dbMember = memberMapper.selectMemberByEmail(dto.getEmail());
        if (dbMember == null) return null;

        dto.setMemberName(dto.getUserName());

        if (isValidDateInput(dto)) {
            String combinedDate = combineDate(dto.getBirthYear(), dto.getBirthMonth(), dto.getBirthDay());
            dto.setBirthDate(combinedDate);
        } else {
            dto.setBirthDate(dbMember.getBirthDate());
        }

        if (dto.getNewPw() != null && !dto.getNewPw().trim().isEmpty()) {
            if (!passwordEncoder.matches(dto.getCurrentPw(), dbMember.getPasswordHash())) {
                throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
            }
            String encodedNewPw = passwordEncoder.encode(dto.getNewPw());
            dto.setPasswordHash(encodedNewPw);
        } else {
            dto.setPasswordHash(null);
        }

        memberMapper.updateMember(dto);
        return memberMapper.selectMemberByEmail(dto.getEmail());
    }

    // 4. 비밀번호 재설정
    public void updatePassword(String email, String newPw) {
        String encodedPw = passwordEncoder.encode(newPw);
        memberMapper.updatePassword(email, encodedPw);
    }

    // --- 유틸리티 메서드 ---
    private boolean isValidDateInput(MemberDto dto) {
        return dto.getBirthYear() != null && !dto.getBirthYear().isEmpty() &&
                dto.getBirthMonth() != null && !dto.getBirthMonth().isEmpty() &&
                dto.getBirthDay() != null && !dto.getBirthDay().isEmpty();
    }

    private String combineDate(String y, String m, String d) {
        return String.format("%s-%02d-%02d", y, Integer.parseInt(m), Integer.parseInt(d));
    }

    public boolean checkEmailDuplicate(String email) {
        return memberMapper.countByEmail(email) > 0;
    }

    public MemberDto getMemberByEmail(String email) {
        return memberMapper.selectMemberByEmail(email);
    }
}