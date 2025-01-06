package org.example.studybot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.studybot.voicechannel.VoiceChannelLog;
import org.example.studybot.voicechannel.VoiceChannelLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StudyBotDiscordListener extends ListenerAdapter {

    @Autowired
    private VoiceChannelLogRepository repository;

    @Autowired
    private DailySummaryService dailySummaryService;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User user = event.getAuthor();
        Member member = event.getMember();

        TextChannel textChannel = event.getChannel().asTextChannel();
        Message message = event.getMessage();

        log.info("get message : " + message.getContentDisplay());

        if (user.isBot()) {
            return;
        } else if (message.getContentDisplay().equals("")) {
            log.info("문자열 비어있음");
        }

        String[] messageArray = message.getContentDisplay().split(" ");

        if (messageArray.length > 1 && messageArray[0].equalsIgnoreCase("!")) {
            String[] messageArgs = Arrays.copyOfRange(messageArray, 1, messageArray.length);

            String nickname = member.getNickname(); // 길드에서의 닉네임 (null일 수도 있음)
            String displayName = nickname != null ? nickname : user.getName(); // 닉네임 없으면 기본 이름

            for (String msg : messageArgs) {
                String returnMessage = sendMessage(msg, displayName, user.getName()); // 길드 이름 전달
                textChannel.sendMessage(returnMessage).queue();
            }
        } else{
            textChannel.sendMessage("잘못된 명령어입니다.");
        }
    }

    public String sendMessage(String message, String displayName, String userName) {
        String returnMessage = "잘못된 명령어입니다.";

        if (message.startsWith("기록-")) {
            String datePart = message.replace("기록-", "").trim();
            System.out.println(datePart);
            returnMessage = getLogsForSpecificDate(datePart);
        } else {
            switch (message) {
                case "안녕":
                    returnMessage = displayName + " 얼른 공부좀해!";
                    break;
                case "하기싫어":
                    returnMessage = displayName + " 그냥 좀 해";
                    break;
                case "오늘만쉴까":
                    returnMessage = displayName + " 그럼 평생 쉬겠지";
                    break;
                case "김민선바보":
                    returnMessage = "김민선 바보멍청이";
                    break;
                case "오주영바보":
                    returnMessage = "오주영 바보멍청이";
                    break;
                case "한승희바보":
                    returnMessage = "한승희 바보멍청이";
                    break;
                case "허준기바보":
                    returnMessage = "허준기 바보멍청이";
                    break;
                case "전체월간기록":
                    returnMessage = getAllMonthlyLogs();
                    break;
                case "전체주간기록":
                    returnMessage = getAllWeeklyLogs();
                    break;
                case "전체일간기록":
                    returnMessage = getAllDailyLogs();
                    break;
                case "월간기록":
                    returnMessage = getMonthlyLogs(userName);
                    break;
                case "주간기록":
                    returnMessage = getWeeklyLogs(userName);
                    break;
                case "일간기록":
                    returnMessage = getDailyLogs(userName);
                    break;
                case "명령어":
                    returnMessage = getHelpMessage();
                    break;
                default:
                    returnMessage = "잘못된 명령어입니다.";
            }
        }

        return returnMessage;
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

    private String getAllMonthlyLogs() {
        LocalDate startOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        LocalDateTime start = startOfMonth.atStartOfDay();
        LocalDateTime end = endOfMonth.atTime(23, 59, 59);
        List<VoiceChannelLog> logs = repository.findAllLogsBetween(start, end);
        return formatLogsSummed(logs, "월간");
    }

    private String getAllWeeklyLogs() {
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        LocalDateTime start = startOfWeek.atStartOfDay();
        LocalDateTime end = endOfWeek.atTime(23, 59, 59);
        List<VoiceChannelLog> logs = repository.findAllLogsBetween(start, end);
        return formatLogsSummed(logs, "주간");
    }

    private String getAllDailyLogs() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        List<VoiceChannelLog> logs = repository.findAllLogsBetween(startOfDay, endOfDay);
        return formatLogsSummed(logs, "일간");
    }

    private String getMonthlyLogs(String userName) {
        LocalDate startOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        LocalDateTime start = startOfMonth.atStartOfDay();
        LocalDateTime end = endOfMonth.atTime(23, 59, 59);
        List<VoiceChannelLog> logs = repository.findLogsBetween(start, end, userName);
        System.out.println(userName);
        return formatLogsSummed(logs, "월간");
    }

    private String getWeeklyLogs(String userName) {
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        LocalDateTime start = startOfWeek.atStartOfDay();
        LocalDateTime end = endOfWeek.atTime(23, 59, 59);
        List<VoiceChannelLog> logs = repository.findLogsBetween(start, end, userName);
        return formatLogsSummed(logs, "주간");
    }

    private String getDailyLogs(String userName) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        List<VoiceChannelLog> logs = repository.findLogsBetween(startOfDay, endOfDay, userName);
        System.out.println(userName);
        return formatLogsSummed(logs, "일간");
    }

    private String getLogsForSpecificDate(String datePart) {
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

    private String getHelpMessage() {
        return """
            **StudyBot 명령어 모음집**
            \uD83D\uDD17 **일반 명령어**
            `안녕` - 봇이 인사를 합니다.
            `하기싫어` - 공부 동기를 부여합니다.
            `오늘만쉴까` - 오늘의 결심을 확인합니다.

            \uD83D\uDCCA **기록 명령어**
            `전체월간기록` - 모든 사용자의 월간 기록을 확인합니다.
            `전체주간기록` - 모든 사용자의 주간 기록을 확인합니다.
            `전체일간기록` - 모든 사용자의 일간 기록을 확인합니다.
            `월간기록` - 본인의 월간 기록을 확인합니다.
            `주간기록` - 본인의 주간 기록을 확인합니다.
            `일간기록` - 본인의 일간 기록을 확인합니다.

            \uD83D\uDE09 **기타**
            `명령어` - 이 명령어 목록을 확인합니다.

            **Tip:** 명령어는 `!+공백`로 시작해야 합니다.
            """;
    }
}
