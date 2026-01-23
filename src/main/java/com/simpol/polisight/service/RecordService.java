package com.simpol.polisight.service;

import com.simpol.polisight.dto.RecordDto;
import com.simpol.polisight.mapper.RecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordMapper recordMapper;

    /**
     * 기록 저장
     */
    @Transactional
    public void saveRecord(RecordDto recordDto) {
        recordMapper.insertRecord(recordDto);
    }

    /**
     * 기록 조회 및 가공
     */
    public List<RecordDto> getRecords(Long memberIdx, int page, int pageSize, String keyword) {
        int offset = (page - 1) * pageSize;
        List<RecordDto> list = recordMapper.selectRecords(memberIdx, offset, pageSize, keyword);

        // 데이터 가공 (화면 표시용)
        for (RecordDto dto : list) {
            // 1. 나이 계산
            dto.setUserAge(calculateAge(dto.getBirthDate()));

            // 2. 지역 텍스트
            String region = (dto.getProvince() != null ? dto.getProvince() : "") + " " +
                    (dto.getCity() != null ? dto.getCity() : "");
            dto.setRegionText(region.trim());

            // 3. 직업명 변환
            dto.setJobName(convertEmpCode(dto.getEmpStatusCode()));

            // 4. 점수 (임시 로직: 98점 고정, 실제론 AI 결과 파싱 필요)
            dto.setResultScore(98);
        }
        return list;
    }

    public int getTotalCount(Long memberIdx, String keyword) {
        return recordMapper.countRecords(memberIdx, keyword);
    }

    @Transactional
    public void deleteRecords(List<Long> simIdxList) {
        if (simIdxList != null && !simIdxList.isEmpty()) {
            recordMapper.deleteRecords(simIdxList);
        }
    }

    // --- Helper Methods ---
    private int calculateAge(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private String convertEmpCode(Integer code) {
        if (code == null) return "-";
        // DB에 저장된 코드 값에 따라 수정 (예시)
        switch (code) {
            case 1: return "미취업";
            case 2: return "직장인";
            case 3: return "자영업";
            case 4: return "프리랜서";
            case 5: return "창업자";
            default: return "기타";
        }
    }
}