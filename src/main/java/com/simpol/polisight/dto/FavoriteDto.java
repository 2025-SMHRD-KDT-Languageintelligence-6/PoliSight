package com.simpol.polisight.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteDto {
    private Long memberIdx;     // 회원 고유 번호 (PK, FK)
    private String plcyNo;      // 정책 ID (PK, FK)
    private LocalDateTime createdAt;
}
