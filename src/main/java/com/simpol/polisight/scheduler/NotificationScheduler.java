package com.simpol.polisight.scheduler;

import com.simpol.polisight.mapper.FavoriteMapper;
import com.simpol.polisight.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final FavoriteMapper favoriteMapper;
    private final MailService mailService;

    // 매일 아침 9시 실행
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDeadlineAlerts() {
        log.info("⏰ 마감 임박 알림 스케줄러 시작...");

        // 1. 대상자 조회
        List<Map<String, Object>> targets = favoriteMapper.selectUsersForDeadlineNotify();

        if (targets.isEmpty()) {
            log.info("오늘은 발송할 마감 알림이 없습니다.");
            return;
        }

        // 2. 메일 발송 반복문 (성공 카운트 포함)
        int successCount = 0; // ★ 성공 횟수 세기 시작

        for (Map<String, Object> target : targets) {
            // [중요] XML에서 AS를 뺐으므로 DB 컬럼명 그대로 꺼냅니다.
            String email = (String) target.get("email");
            String pName = (String) target.get("plcyNm");
            String pNo   = (String) target.get("plcyNo");

            try {
                if (email != null && !email.isEmpty()) {
                    mailService.sendDeadlineNotification(email, pName, pNo);

                    successCount++; // ★ 성공 시 1 증가
                    log.info("발송 성공: {} -> {}", email, pName);
                }
            } catch (Exception e) {
                // 실패해도 멈추지 않고 로그만 남기고 다음 사람으로 넘어감
                log.error("발송 실패: {} (원인: {})", email, e.getMessage());
            }
        }

        // 3. 최종 결과 로그 출력
        log.info("✅ 마감 알림 작업 종료. (대상: {}건, 성공: {}건)", targets.size(), successCount);
    }
}