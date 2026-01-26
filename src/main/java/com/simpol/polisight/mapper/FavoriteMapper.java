package com.simpol.polisight.mapper;

import com.simpol.polisight.dto.PolicyDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface FavoriteMapper {

    // 1. 즐겨찾기 존재 여부 확인 (1: 있음, 0: 없음)
    int countByMemberIdxAndPlcyNo(@Param("memberIdx") Long memberIdx, @Param("plcyNo") String plcyNo);

    // 2. 즐겨찾기 추가 (INSERT)
    void insertFavorite(@Param("memberIdx") Long memberIdx, @Param("plcyNo") String plcyNo);

    // 3. 즐겨찾기 취소 (물리적 삭제 - DELETE)
    void deleteFavorite(@Param("memberIdx") Long memberIdx, @Param("plcyNo") String plcyNo);

    // 4. 내가 즐겨찾기한 정책 ID 목록 조회
    List<String> selectPlcyNosByMemberIdx(Long memberIdx);

    // 기존 메서드 아래에 추가
    List<PolicyDto> selectFavoritePoliciesDetails(Long memberIdx);

    int updateNotify(Long memberIdx, String plcyNo, int notify);

    // [추가] 마감 임박 알림 대상 조회 (결과가 여러 개일 수 있으니 List)
    List<Map<String, Object>> selectUsersForDeadlineNotify();
}
