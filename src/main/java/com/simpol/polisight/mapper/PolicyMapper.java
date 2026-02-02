package com.simpol.polisight.mapper;

import com.simpol.polisight.dto.PolicyDto;
import com.simpol.polisight.dto.PolicySearchCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PolicyMapper {
    // 조건 검색
    List<PolicyDto> selectPoliciesByCondition(PolicySearchCondition condition);

    // 단일 조회 (ID로 조회)
    PolicyDto selectPolicyById(@Param("id") String id);

    // 지역 코드 매핑 조회
    List<Map<String, String>> selectRegionMapping();

    // ▼▼▼ [추가됨] 정책 이름으로 단일 조회 (AI 추천 결과 매핑용) ▼▼▼
    PolicyDto selectPolicyByName(@Param("plcyNm") String plcyNm);
}