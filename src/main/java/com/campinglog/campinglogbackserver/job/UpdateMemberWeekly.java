// job/PromotionJob.java
package com.campinglog.campinglogbackserver.job;

import com.campinglog.campinglogbackserver.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateMemberWeekly {

    private final MemberService memberService;

    // 매주 목요일 오전 9시 (KST)
    @Scheduled(cron = "0 0 9 ? * THU", zone = "Asia/Seoul")
    public void promoteEveryThursday9am() {
        int changed = memberService.updateGradeWeekly();
        log.info("[CRON] Thursday 09:00 promotion done. changed={}", changed);

        memberService.updateRankWeekly(5);
        log.info("[CRON] weekly ranking snapshot done.");
    }
}
