package com.simpol.polisight.service;

import com.simpol.polisight.dto.PolicyDto;
import com.simpol.polisight.mapper.FavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;

    @Transactional
    public boolean toggleFavorite(Long memberIdx, String plcyNo) {
        // 1. 이미 존재하는지 확인
        int count = favoriteMapper.countByMemberIdxAndPlcyNo(memberIdx, plcyNo);

        if (count > 0) {
            // 2. 존재하면 -> 삭제 (DELETE)
            favoriteMapper.deleteFavorite(memberIdx, plcyNo);
            return false; // 즐겨찾기 해제됨 (결과: false)
        } else {
            // 3. 없으면 -> 추가 (INSERT)
            favoriteMapper.insertFavorite(memberIdx, plcyNo);
            return true; // 즐겨찾기 설정됨 (결과: true)
        }
    }

    // 메서드 추가
    public List<PolicyDto> getFavoritePolicies(Long memberIdx) {
        return favoriteMapper.selectFavoritePoliciesDetails(memberIdx);
    }

    public boolean updateNotify(Long memberIdx, String plcyNo, int notify) {
        int result = favoriteMapper.updateNotify(memberIdx, plcyNo, notify);
        return result > 0;
    }
}