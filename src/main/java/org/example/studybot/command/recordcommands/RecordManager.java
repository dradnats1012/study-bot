package org.example.studybot.command.recordcommands;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        return formatLogsByRange("월간", getMonthRange(), Optional.empty());
    }

    public String getAllWeeklyLogs() {
        return formatLogsByRange("주간", getWeekRange(), Optional.empty());
    }

    public String getAllDailyLogs() {
        return formatLogsByRange("일간", getDayRange(), Optional.empty());
    }

    public String getMonthlyLogs(String userName) {
        return formatLogsByRange("월간", getMonthRange(), Optional.of(userName));
    }

    public String getWeeklyLogs(String userName) {
        return formatLogsByRange("주간", getWeekRange(), Optional.of(userName));
    }

    public String getDailyLogs(String userName) {
        return formatLogsByRange("일간", getDayRange(), Optional.of(userName));
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

        Map<String, Long> userDurations = logs.stream()
            .collect(Collectors.groupingBy(
                VoiceChannelLog::getNickName,
                Collectors.summingLong(VoiceChannelLog::getDuration)
            ));

        return userDurations.entrySet().stream()
            .map(entry -> formatDuration(entry.getKey(), entry.getValue()))
            .collect(Collectors.joining(
                "\n", periodName + " 기간 내 기록:\n", ""
            ));
    }

    private String formatDuration(String user, long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%s님이 총 %d시간 %d분 %d초 동안 머물렀습니다.", user, hours, minutes, seconds);
    }


    private String formatLogsByRange(String label, List<LocalDateTime> range, Optional<String> userNameOpt) {
        LocalDateTime start = range.get(0);
        LocalDateTime end = range.get(1);

        List<VoiceChannelLog> logs = userNameOpt
            .map(userName -> repository.findLogsBetween(start, end, userName))
            .orElseGet(() -> repository.findAllLogsBetween(start, end));

        return formatLogsSummed(logs, label);
    }

    private List<LocalDateTime> getMonthRange() {
        LocalDate now = LocalDate.now();
        return List.of(
            now.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay(),
            now.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59)
        );
    }

    private List<LocalDateTime> getWeekRange() {
        LocalDate start = LocalDate.now().with(DayOfWeek.MONDAY);
        return List.of(
            start.atStartOfDay(),
            start.plusDays(6).atTime(23, 59, 59)
        );
    }

    private List<LocalDateTime> getDayRange() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        return List.of(
            start,
            start.plusDays(1).minusSeconds(1)
        );
    }
}
