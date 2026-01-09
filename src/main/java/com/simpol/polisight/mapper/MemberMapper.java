package com.simpol.polisight.mapper;

import com.simpol.polisight.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {
    // 회원가입
    void insertMember(MemberDto memberDto);

    // 로그인 (이메일로 조회)
    MemberDto selectMemberByEmail(String email);

    // MemberMapper.java
    void updateMember(MemberDto memberDto);




}

