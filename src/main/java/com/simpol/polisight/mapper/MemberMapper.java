package com.simpol.polisight.mapper;

import com.simpol.polisight.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {

    void insertMember(MemberDto memberDto);

    MemberDto selectMemberByEmail(String email);

    void updateMember(MemberDto memberDto);

    int countByEmail(String email);

    void updatePassword(@Param("email") String email, @Param("passwordHash") String encodedPw);

    // ✅ 추가: 마이페이지 내 조건 업데이트
    void updateConditions(MemberDto memberDto);
}
