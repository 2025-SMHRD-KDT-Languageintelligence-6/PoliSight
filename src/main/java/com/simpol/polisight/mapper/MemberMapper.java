package com.simpol.polisight.mapper;

import com.simpol.polisight.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {
    // 회원가입
    void insertMember(MemberDto memberDto);

    // 로그인 (이메일로 조회)
    MemberDto selectMemberByEmail(String email);

    // MemberMapper.java
    void updateMember(MemberDto memberDto);

    // [추가] 이메일로 회원 수 조회
    int countByEmail(String email);


    void updatePassword(@Param("email") String email, @Param("passwordHash") String encodedPw);
}

