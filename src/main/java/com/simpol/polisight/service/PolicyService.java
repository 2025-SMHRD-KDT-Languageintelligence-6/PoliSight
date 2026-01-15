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

    public List<PolicyDto> searchPolicies(PolicySearchCondition condition) {

        // 1. 나이 계산 (생년월일 YYYYMMDD -> 만/연 나이)
        if (condition.getBirthDate() != null && condition.getBirthDate().length() == 8) {
            try {
                int birthYear = Integer.parseInt(condition.getBirthDate().substring(0, 4));
                int currentYear = LocalDate.now().getYear();
                condition.setAge(currentYear - birthYear); // 연 나이 계산
            } catch (NumberFormatException e) {
                // 날짜 형식이 잘못되었을 경우 무시
            }
        }

        // 2. 소득 단위 변환 (만원 -> 원)
        // HTML에서는 '만원' 단위로 입력받지만, DB는 '원' 단위일 확률이 높음
        if (condition.getIncome() != null) {
            condition.setIncome(condition.getIncome() * 10000);
        }
        if (condition.getHouseholdIncome() != null) {
            condition.setHouseholdIncome(condition.getHouseholdIncome() * 10000);
        }

        // 3. 코드 매핑 (필요시 구현)
        // HTML의 "UNIV_GRAD" 같은 문자열을 DB 코드(예: "004")로 바꿔야 한다면 여기서 처리
        // condition.setEducationLevel( convertEduToDbCode(condition.getEducationLevel()) );

        return policyMapper.selectPoliciesByCondition(condition);
    }
}