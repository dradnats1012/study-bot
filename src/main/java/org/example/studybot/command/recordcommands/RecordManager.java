package org.example.studybot.command.recordcommands;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.studybot.voicechannel.VoiceChannelLog;
import org.example.studybot.voicechannel.VoiceChannelLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RecordManager {

    @Autowired
    private VoiceChannelLogRepository repository;

    public String getAllMonthlyLogs() {
        LocalDate startOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        LocalDateTime start = startOfMonth.atStartOfDay();
        LocalDateTime end = endOfMonth.atTime(23, 59, 59);
        List<VoiceChannelLog> logs = repository.findAllLogsBetween(start, end);
        return formatLogsSummed(logs, "월간");
    }

    public String getAllWeeklyLogs() {
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        LocalDateTime start = startOfWeek.atStartOfDay();
        LocalDateTime end = endOfWeek.atTime(23, 59, 59);
        List<VoiceChannelLog> logs = repository.findAllLogsBetween(start, end);
        return formatLogsSummed(logs, "주간");
    }

    public String getAllDailyLogs() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        List<VoiceChannelLog> logs = repository.findAllLogsBetween(startOfDay, endOfDay);
        return formatLogsSummed(logs, "일간");
    }

    public String getMonthlyLogs(String userName) {
        LocalDate startOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        LocalDateTime start = startOfMonth.atStartOfDay();
        LocalDateTime end = endOfMonth.atTime(23, 59, 59);
        List<VoiceChannelLog> logs = repository.findLogsBetween(start, end, userName);
        System.out.println(userName);
        return formatLogsSummed(logs, "월간");
    }

    public String getWeeklyLogs(String userName) {
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        LocalDateTime start = startOfWeek.atStartOfDay();
        LocalDateTime end = endOfWeek.atTime(23, 59, 59);
        List<VoiceChannelLog> logs = repository.findLogsBetween(start, end, userName);
        return formatLogsSummed(logs, "주간");
    }

    public String getDailyLogs(String userName) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        List<VoiceChannelLog> logs = repository.findLogsBetween(startOfDay, endOfDay, userName);
        return formatLogsSummed(logs, "일간");
    }

    public String getLogsForSpecificDate(String datePart) {
        LocalDate targetDate;
        try {
            String[] parts = datePart.split("/");

            int month = Integer.parseInt(parts[0]);
            int day = Integer.parseInt(parts[1]);

            int currentYear = LocalDate.now().getYear();
            targetDate = LocalDate.of(currentYear, month, day);
        } catch (Exception e) {
            return "날짜 형식이 잘못되었습니다. 올바른 형식: MM/dd 또는 M/d (예: 12/25 또는 1/3)";
        }

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        List<VoiceChannelLog> logs = repository.findAllLogsBetween(startOfDay, endOfDay);
        if (logs.isEmpty()) {
            return targetDate.format(DateTimeFormatter.ofPattern("MM/dd")) + "에 기록이 없습니다.";
        }

        return formatLogsSummed(logs, targetDate.format(DateTimeFormatter.ofPattern("MM/dd")));
    }

    private String formatLogsSummed(List<VoiceChannelLog> logs, String periodName) {
        if (logs.isEmpty()) {
            return periodName + " 기간 동안 기록이 없습니다.";
        }

        // 사용자별로 총 머문 시간을 계산
        Map<String, Long> userDurationMap = new HashMap<>();
        logs.forEach(log -> userDurationMap.merge(
            log.getNickName(), log.getDuration(), Long::sum
        ));

        // 결과 메시지 작성
        StringBuilder response = new StringBuilder(periodName + " 기간 내 기록:\n");
        userDurationMap.forEach((username, totalDuration) -> {
            long hours = totalDuration / 3600;
            long minutes = (totalDuration % 3600) / 60;
            long seconds = totalDuration % 60;

            response.append(String.format(
                "%s님이 총 %d시간 %d분 %d초 동안 머물렀습니다.\n",
                username, hours, minutes, seconds
            ));
        });

        return response.toString();
    }
}
