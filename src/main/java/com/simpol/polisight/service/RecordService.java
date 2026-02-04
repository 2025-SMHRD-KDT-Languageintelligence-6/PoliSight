package com.simpol.polisight.service;

import com.simpol.polisight.dto.RecordDto;
import com.simpol.polisight.mapper.RecordMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
     * 기록 저장 (AiSimulationService에서 직접 호출하므로 여기서는 단순 래퍼)
     */
    @Transactional
    public void saveRecord(RecordDto recordDto) {
        recordMapper.insertRecord(recordDto);
    }

    /**
     * 기록 조회 및 가공 (리스트 화면용)
     */
    public List<RecordDto> getRecords(Long memberIdx, int page, int pageSize, String keyword) {
        int offset = (page - 1) * pageSize;
        List<RecordDto> list = recordMapper.selectRecords(memberIdx, offset, pageSize, keyword);

        for (RecordDto dto : list) {
            // 1. 나이 계산
            dto.setUserAge(calculateAge(dto.getBirthDate()));

            // 2. 지역 텍스트 가공
            String region = (dto.getProvince() != null ? dto.getProvince() : "") + " " +
                    (dto.getCity() != null ? dto.getCity() : "");
            dto.setRegionText(region.trim());

            // 3. 직업명 변환
            dto.setJobName(convertEmpCode(dto.getEmpStatusCode()));

            // 4. ★ JSON 파싱하여 중요 정보(Y/N, 점수)만 DTO에 세팅
            extractSummaryFromJson(dto);
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

    private void extractSummaryFromJson(RecordDto dto) {
        String content = dto.getContent();
        // 기본값 세팅
        dto.setSuitability("N");
        dto.setResultScore(0);

        if (content != null && content.trim().startsWith("{")) {
            try {
                // JSON 파싱
                JsonObject json = JsonParser.parseString(content).getAsJsonObject();

                // suitability (Y/N) 추출
                if (json.has("suitability")) {
                    dto.setSuitability(json.get("suitability").getAsString());
                }

                // score (점수) 추출
                if (json.has("score")) {
                    dto.setResultScore(json.get("score").getAsInt());
                }

                // 나머지(시나리오 등)는 content 문자열 안에 그대로 있으므로 프론트엔드가 처리함

            } catch (Exception e) {
                // 파싱 에러 시 기본값 유지
            }
        }
    }

    private int calculateAge(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private String convertEmpCode(Integer code) {
        if (code == null) return "-";
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