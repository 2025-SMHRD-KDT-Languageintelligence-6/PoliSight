package com.simpol.polisight.mapper;

import com.simpol.polisight.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {

    void insertMember(MemberDto memberDto);

    MemberDto selectMemberByEmail(String email);

    // [신규] 새 메서드 추가 (파라미터를 명확하게 분리)
    void updateMemberName(@Param("email") String email, @Param("name") String name);

    int countByEmail(String email);

    void updatePassword(@Param("email") String email, @Param("passwordHash") String encodedPw);

    // ✅ 추가: 마이페이지 내 조건 업데이트
    void updateConditions(MemberDto memberDto);

    // 회원 탈퇴
    void deleteMember(String email);
}
