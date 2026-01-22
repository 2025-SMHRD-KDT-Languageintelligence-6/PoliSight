package com.simpol.polisight.controller;

import com.simpol.polisight.dto.MemberDto;
import com.simpol.polisight.service.FavoriteService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{plcyNo}")
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @PathVariable String plcyNo,
            HttpSession session) {

        // 세션에서 로그인 사용자 확인
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            return ResponseEntity.status(401).build(); // 401 Unauthorized
        }

        // 서비스 호출 (true: 저장됨, false: 삭제됨)
        boolean isFavorited = favoriteService.toggleFavorite(loginMember.getMemberIdx(), plcyNo);

        // 결과 JSON 응답
        Map<String, Object> response = new HashMap<>();
        response.put("favorited", isFavorited);

        return ResponseEntity.ok(response);
    }
}