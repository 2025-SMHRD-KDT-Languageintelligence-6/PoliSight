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

    @PostMapping("/notify")
    public ResponseEntity<Map<String, Object>> updateNotify(
            @RequestBody Map<String, Object> payload,
            HttpSession session) {

        // 1. 로그인 체크
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(401).build();
        }

        // 2. JS에서 보낸 데이터 받기 { "plcyNo": "...", "notify": 1 }
        String plcyNo = (String) payload.get("plcyNo");

        // JSON으로 넘어온 숫자는 Integer 변환이 필요할 수 있음
        int notify = Integer.parseInt(String.valueOf(payload.get("notify")));

        // 3. 서비스 호출 (DB 업데이트)
        // loginMember.getMemberIdx()는 사용자 고유 번호입니다.
        boolean success = favoriteService.updateNotify(loginMember.getMemberIdx(), plcyNo, notify);

        // 4. 응답
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);

        return ResponseEntity.ok(response);
    }
}