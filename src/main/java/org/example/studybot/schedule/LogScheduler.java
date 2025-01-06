package org.example.studybot.schedule;

import org.example.studybot.DailySummaryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogScheduler {
    private final DailySummaryService dailySummaryService;

    @Scheduled(cron = "0 1 0 * * *")
    public void sendDailySummary() {
        dailySummaryService.generateAndSendDailySummary();
    }
}
