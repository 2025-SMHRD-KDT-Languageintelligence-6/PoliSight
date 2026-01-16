package com.simpol.polisight.service;

import com.simpol.polisight.dto.PolicyDto;
import com.simpol.polisight.dto.PolicySearchCondition;
import com.simpol.polisight.mapper.PolicyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyMapper policyMapper;

    // 검색
    public List<PolicyDto> searchPolicies(PolicySearchCondition condition) {
        if (condition.getBirthDate() != null && condition.getBirthDate().length() == 8) {
            try {
                int birthYear = Integer.parseInt(condition.getBirthDate().substring(0, 4));
                int currentYear = LocalDate.now().getYear();
                condition.setAge(currentYear - birthYear);
            } catch (NumberFormatException e) {
                // 무시
            }
        }
        if (condition.getIncome() != null) {
            condition.setIncome(condition.getIncome() * 10000);
        }
        if (condition.getHouseholdIncome() != null) {
            condition.setHouseholdIncome(condition.getHouseholdIncome() * 10000);
        }
        return policyMapper.selectPoliciesByCondition(condition);
    }

    // [추가] 단일 조회 (String ID)
    public PolicyDto getPolicyById(String id) {
        return policyMapper.selectPolicyById(id);
    }
}