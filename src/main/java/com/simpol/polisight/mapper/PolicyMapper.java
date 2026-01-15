package com.simpol.polisight.mapper;

import com.simpol.polisight.dto.PolicyDto;
import com.simpol.polisight.dto.PolicySearchCondition;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PolicyMapper {
    List<PolicyDto> selectPoliciesByCondition(PolicySearchCondition condition);
}