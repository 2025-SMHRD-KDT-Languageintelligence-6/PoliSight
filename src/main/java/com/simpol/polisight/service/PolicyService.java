package com.simpol.polisight.service;

import com.simpol.polisight.dto.PolicyDto;
import com.simpol.polisight.dto.PolicySearchCondition;
import com.simpol.polisight.mapper.PolicyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList; // [추가] 리스트 확장을 위해 필요
import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyMapper policyMapper;

    // 검색
    public List<PolicyDto> searchPolicies(PolicySearchCondition condition) {

        // 1. 생년월일 -> 나이 변환 로직 (기존 유지)
        if (condition.getBirthDate() != null && condition.getBirthDate().length() == 8) {
            try {
                int birthYear = Integer.parseInt(condition.getBirthDate().substring(0, 4));
                int currentYear = LocalDate.now().getYear();
                condition.setAge(currentYear - birthYear);
            } catch (NumberFormatException e) {
                // 무시
            }
        }

        // 2. 소득 단위 변환 (만원 -> 원) (기존 유지)
        if (condition.getIncome() != null) {
            condition.setIncome(condition.getIncome() * 10000);
        }
        if (condition.getHouseholdIncome() != null) {
            condition.setHouseholdIncome(condition.getHouseholdIncome() * 10000);
        }

        // 3. [신규 추가] 고용 상태 매핑 (화면 키워드 -> DB 코드 확장)
        // 화면에서는 '직장인' 하나만 선택해도, 실제로는 '재직자, 일용직, 단기근로자'를 모두 검색하도록 변환합니다.
        if (condition.getEmploymentStatus() != null && !condition.getEmploymentStatus().isEmpty()) {
            List<String> expandedCodes = new ArrayList<>();

            for (String type : condition.getEmploymentStatus()) {
                switch (type) {
                    case "UNEMPLOYED": // 미취업
                        expandedCodes.add("0013003"); // 미취업자
                        break;

                    case "EMPLOYED": // 직장인 (정규/계약/아르바이트 통합)
                        expandedCodes.add("0013001"); // 재직자
                        expandedCodes.add("0013005"); // 일용근로자
                        expandedCodes.add("0013007"); // 단기근로자
                        break;

                    case "SELF_EMPLOYED": // 자영업
                        expandedCodes.add("0013002"); // 자영업자
                        expandedCodes.add("0013008"); // 영농종사자
                        break;

                    case "FREELANCER": // 프리랜서
                        expandedCodes.add("0013004"); // 프리랜서
                        break;

                    case "FOUNDER": // 창업
                        expandedCodes.add("0013006"); // (예비)창업자
                        break;
                }
            }
            // 변환된 코드 리스트로 교체하여 Mapper에 전달
            condition.setEmploymentStatus(expandedCodes);
        }

        return policyMapper.selectPoliciesByCondition(condition);
    }

    // 단일 조회 (String ID)
    public PolicyDto getPolicyById(String id) {
        return policyMapper.selectPolicyById(id);
    }
}