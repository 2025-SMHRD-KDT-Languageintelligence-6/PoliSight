package com.simpol.polisight.service;

import com.simpol.polisight.dto.MemberDto;
import com.simpol.polisight.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    @Autowired
    private MemberMapper memberMapper;

    // 회원가입 로직
    public void join(MemberDto dto) {
        // 1. HTML의 입력값(userPw, userName)을 DB용 필드(passwordHash, memberName)로 옮김
        // (현재는 암호화 없이 평문 저장)
        dto.setPasswordHash(dto.getUserPw());
        dto.setMemberName(dto.getUserName());

        // 2. 생년월일 조합 (YYYY + MM + DD -> YYYY-MM-DD)
        // MySQL DATE 타입에 넣기 위해 형식을 맞춥니다.
        String y = dto.getBirthYear();
        String m = String.format("%02d", Integer.parseInt(dto.getBirthMonth())); // 1 -> 01
        String d = String.format("%02d", Integer.parseInt(dto.getBirthDay()));   // 1 -> 01

        dto.setBirthDate(y + "-" + m + "-" + d);

        // 3. DB 저장 실행
        memberMapper.insertMember(dto);
    }

    // 로그인 로직
    public MemberDto login(String email, String inputPw) {
        // 1. 이메일로 회원 정보 조회
        MemberDto member = memberMapper.selectMemberByEmail(email);

        // 2. 검증
        if (member != null) {
            // DB에 있는 passwordHash와 사용자가 입력한 inputPw가 같은지 확인
            if (member.getPasswordHash().equals(inputPw)) {
                return member; // 로그인 성공
            }
        }

        return null; // 로그인 실패
    }
}