package com.simpol.polisight.service;

import com.simpol.polisight.dto.PolicyDto;
import com.simpol.polisight.dto.PolicySearchCondition;
import com.simpol.polisight.mapper.PolicyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyMapper policyMapper;

    // ✅ [신규] 나이 계산 유틸 메서드 (컨트롤러에서도 사용 가능)
    public int calculateAge(String birthDateStr) {
        if (birthDateStr == null || birthDateStr.length() != 8) return 0;
        try {
            LocalDate birthDate = LocalDate.parse(birthDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate currentDate = LocalDate.now();
            return Period.between(birthDate, currentDate).getYears();
        } catch (Exception e) {
            return 0;
        }
    }

    // 검색
    public List<PolicyDto> searchPolicies(PolicySearchCondition condition) {

        // 1. 생년월일 -> 나이 변환 (공통 메서드 사용)
        if (condition.getBirthDate() != null) {
            condition.setAge(calculateAge(condition.getBirthDate()));
        }

        // 2. 소득 단위 변환 (만원 -> 원)
        if (condition.getIncome() != null) {
            condition.setIncome(condition.getIncome());
        }
        if (condition.getHouseholdIncome() != null) {
            condition.setHouseholdIncome(condition.getHouseholdIncome());
        }

        // 3. 고용 상태 매핑
        if (condition.getEmploymentStatus() != null && !condition.getEmploymentStatus().isEmpty()) {
            List<String> expandedCodes = new ArrayList<>();
            for (String type : condition.getEmploymentStatus()) {
                switch (type) {
                    case "UNEMPLOYED": expandedCodes.add("0013003"); break;
                    case "EMPLOYED":
                        expandedCodes.add("0013001");
                        expandedCodes.add("0013005");
                        expandedCodes.add("0013007");
                        break;
                    case "SELF_EMPLOYED":
                        expandedCodes.add("0013002");
                        expandedCodes.add("0013008");
                        break;
                    case "FREELANCER": expandedCodes.add("0013004"); break;
                    case "FOUNDER": expandedCodes.add("0013006"); break;
                }
            }
            condition.setEmploymentStatus(expandedCodes);
        }

        // 4. 결혼 여부 매핑
        if (condition.getMarry() != null) {
            String val = condition.getMarry();
            if ("Y".equalsIgnoreCase(val)) condition.setMarry("0055001");
            else if ("N".equalsIgnoreCase(val)) condition.setMarry("0055002");
            else condition.setMarry(null);
        }

        return policyMapper.selectPoliciesByCondition(condition);
    }

    public PolicyDto getPolicyById(String id) {
        return policyMapper.selectPolicyById(id);
    }
}