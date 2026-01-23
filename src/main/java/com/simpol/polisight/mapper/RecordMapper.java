package com.simpol.polisight.mapper;

import com.simpol.polisight.dto.RecordDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecordMapper {

    // 기록 저장 (SimulationController에서 사용)
    void insertRecord(RecordDto recordDto);

    // 목록 조회 (페이징 + 검색)
    List<RecordDto> selectRecords(@Param("memberIdx") Long memberIdx,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit,
                                  @Param("keyword") String keyword);

    // 전체 개수 조회
    int countRecords(@Param("memberIdx") Long memberIdx,
                     @Param("keyword") String keyword);

    // 기록 삭제
    void deleteRecords(@Param("simIdxList") List<Long> simIdxList);
}