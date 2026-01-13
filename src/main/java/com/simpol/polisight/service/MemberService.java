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

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    // 시큐리티 로그인 처리
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

        if (isValidDateInput(dto)) {
            String combinedDate = combineDate(dto.getBirthYear(), dto.getBirthMonth(), dto.getBirthDay());
            dto.setBirthDate(combinedDate);
        } else {
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

    // ==========================================
    // [추가] 이름만 변경
    // ==========================================
    public MemberDto updateName(String email, String newName) {
        MemberDto member = memberMapper.selectMemberByEmail(email);
        if (member != null) {
            member.setMemberName(newName);
            memberMapper.updateMember(member); // 기존 Mapper 재활용 (MyBatis 설정에 따라 전체 업데이트됨)
            return member;
        }
        return null;
    }

    // ==========================================
    // [추가] 비밀번호 변경 (현재 비밀번호 확인 포함 - 마이페이지용)
    // ==========================================
    public boolean changePassword(String email, String currentPw, String newPw) {
        MemberDto member = memberMapper.selectMemberByEmail(email);
        if (member == null) return false;

        // 1. 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPw, member.getPasswordHash())) {
            return false; // 불일치
        }

        // 2. 새 비밀번호 암호화 및 저장
        String encodedNewPw = passwordEncoder.encode(newPw);
        memberMapper.updatePassword(email, encodedNewPw);
        return true;
    }

    // 3. 기존 회원 정보 수정 (사용 안할 수도 있지만 유지)
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

    // 4. 비밀번호 재설정 (이메일 찾기용 - 기존 비밀번호 확인 안함)
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