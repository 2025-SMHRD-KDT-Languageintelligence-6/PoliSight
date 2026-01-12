package com.simpol.polisight.service;

import com.simpol.polisight.dto.MemberDto;
import com.simpol.polisight.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;

    // [추가] 암호화 기계 주입
    private final PasswordEncoder passwordEncoder;

    // 1. 회원가입
    public void join(MemberDto dto) {
        // 비밀번호 암호화 (Encode)
        String rawPw = dto.getUserPw();
        String encodedPw = passwordEncoder.encode(rawPw);
        dto.setPasswordHash(encodedPw); // 암호화된 비밀번호로 교체
        dto.setMemberName(dto.getUserName());

        // [날짜 조합] 년/월/일이 모두 있을 때만 조합 (안전 장치 추가)
        if (isValidDateInput(dto)) {
            String combinedDate = combineDate(dto.getBirthYear(), dto.getBirthMonth(), dto.getBirthDay());
            dto.setBirthDate(combinedDate);
        } else {
            // 날짜가 하나라도 없으면 null (DB에서 허용 시) 또는 기본값 처리
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

    // 3. 회원 정보 수정
    public MemberDto updateMember(MemberDto dto) {
        // (1) DB에서 기존 정보 조회
        MemberDto dbMember = memberMapper.selectMemberByEmail(dto.getEmail());

        if (dbMember == null) {
            return null;
        }

        // (2) 이름 매핑
        dto.setMemberName(dto.getUserName());

        // (3) [★수정됨★] 생년월일 조합 로직 추가
        // HTML form에서 birthYear, birthMonth, birthDay가 넘어옵니다.
        if (isValidDateInput(dto)) {
            // 입력된 값이 있으면 조합해서 DTO에 설정 (YYYY-MM-DD)
            String combinedDate = combineDate(dto.getBirthYear(), dto.getBirthMonth(), dto.getBirthDay());
            dto.setBirthDate(combinedDate);
        } else {
            // 수정 폼에서 날짜를 건드리지 않았거나 비어있다면? -> 기존 DB 값 유지
            dto.setBirthDate(dbMember.getBirthDate());
        }

        // (4) 비밀번호 변경 로직
        if (dto.getNewPw() != null && !dto.getNewPw().trim().isEmpty()) {
            // 기존 비번 확인
            if (!passwordEncoder.matches(dto.getCurrentPw(), dbMember.getPasswordHash())) {
                throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
            }

            // 새 비밀번호 암호화해서 저장
            String encodedNewPw = passwordEncoder.encode(dto.getNewPw());
            dto.setPasswordHash(encodedNewPw);
        } else {
            // 변경 안 함 (XML에서 null check로 제외됨)
            dto.setPasswordHash(null);
        }

        // (5) DB 업데이트
        memberMapper.updateMember(dto);

        // (6) 세션 갱신용 최신 정보 반환
        return memberMapper.selectMemberByEmail(dto.getEmail());
    }

    // 비밀번호 재설정 (비밀번호 찾기 이메일 링크용)
    public void updatePassword(String email, String newPw) {
        // 새 비밀번호 암호화
        String encodedPw = passwordEncoder.encode(newPw);

        // DB 업데이트 (Mapper에 이 기능을 하는 쿼리가 있어야 합니다)
        // memberMapper.updatePassword(email, encodedPw);
        // -> 만약 updatePassword 메소드가 없다면 아래처럼 기존 updateMember를 활용하거나 새로 만들어야 합니다.
        // 여기서는 Mapper에 updatePassword(String email, String password)가 있다고 가정합니다.
        memberMapper.updatePassword(email, encodedPw);
    }

    // --- 유틸리티 메서드 (중복 제거용) ---

    // 날짜 입력값 유효성 체크
    private boolean isValidDateInput(MemberDto dto) {
        return dto.getBirthYear() != null && !dto.getBirthYear().isEmpty() &&
                dto.getBirthMonth() != null && !dto.getBirthMonth().isEmpty() &&
                dto.getBirthDay() != null && !dto.getBirthDay().isEmpty();
    }

    // 년,월,일 문자열 합치기 (YYYY-MM-DD)
    private String combineDate(String y, String m, String d) {
        return String.format("%s-%02d-%02d", y, Integer.parseInt(m), Integer.parseInt(d));
    }

    // [추가] 이메일 중복 체크 메소드
    public boolean checkEmailDuplicate(String email) {
        // memberMapper에서 이메일 개수를 세어 0보다 크면 true(중복), 아니면 false 리턴
        return memberMapper.countByEmail(email) > 0;
    }

    public MemberDto getMemberByEmail(String email) {
        return memberMapper.selectMemberByEmail(email);
    }
}