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

    // 단일 조회 (ID가 DB에서 VARCHAR이므로 String으로 받음)
    PolicyDto selectPolicyById(@Param("id") String id);

    List<Map<String, String>> selectRegionMapping();
}