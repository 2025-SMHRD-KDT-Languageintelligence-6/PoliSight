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

    // 이름 변경
    public MemberDto updateName(String email, String newName) {
        MemberDto member = memberMapper.selectMemberByEmail(email);
        if (member != null) {
            member.setMemberName(newName);
            memberMapper.updateMember(member);
            return memberMapper.selectMemberByEmail(email);
        }
        return null;
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

    // (기존) 회원 정보 수정
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
