package org.example.studybot.listener;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.studybot.dto.team.CreateTeamDTO;
import org.example.studybot.model.Command;
import org.example.studybot.model.Team;
import org.example.studybot.service.TeamService;
import org.example.studybot.voicechannel.VoiceChannelLog;
import org.example.studybot.voicechannel.VoiceChannelLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StudyBotDiscordListener extends ListenerAdapter {

    @Autowired
    private VoiceChannelLogRepository repository;

    @Autowired
    private TeamService teamService;

    private final Map<String, Command> commandMap = new HashMap<>();

    // 초기화 (명령어 등록)
    @PostConstruct
    public void init() {
        commandMap.put("안녕", (displayName, userName) -> displayName + " 얼른 공부좀해!");
        commandMap.put("하기싫어", (displayName, userName) -> displayName + " 그냥 좀 해");
        commandMap.put("오늘만쉴까", (displayName, userName) -> displayName + " 그럼 평생 쉬겠지");
        commandMap.put("진짜하기싫다", (displayName, userName) -> displayName + " 책이라도 읽어.");
        commandMap.put("그냥잘까", (displayName, userName) -> displayName + " 평생 잠만 자고 싶어?");
        commandMap.put("피곤해", (displayName, userName) -> displayName + " 가짜 피곤함이야.");
        commandMap.put("김민선바보", (d, u) -> "김민선 바보멍청이");
        commandMap.put("오주영바보", (d, u) -> "오주영 바보멍청이");
        commandMap.put("한승희바보", (d, u) -> "한승희 바보멍청이");
        commandMap.put("허준기바보", (d, u) -> "허준기 바보멍청이");
        //commandMap.put("팀생성", )

        // 기록 관련
        commandMap.put("전체기록", (d, u) -> getAllMonthlyLogs() + "\n" + getAllWeeklyLogs() + "\n" + getAllDailyLogs());
        commandMap.put("전체월간기록", (d, u) -> getAllMonthlyLogs());
        commandMap.put("전체주간기록", (d, u) -> getAllWeeklyLogs());
        commandMap.put("전체일간기록", (d, u) -> getAllDailyLogs());
        commandMap.put("월간기록", (d, u) -> getMonthlyLogs(u));
        commandMap.put("주간기록", (d, u) -> getWeeklyLogs(u));
        commandMap.put("일간기록", (d, u) -> getDailyLogs(u));
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User user = event.getAuthor();
        Member member = event.getMember();
        TextChannel textChannel = event.getChannel().asTextChannel();
        Message message = event.getMessage();

        if (user.isBot())
            return;

        String content = message.getContentDisplay().trim();

        if (content.startsWith("!")) {
            String nickname = member.getNickname();
            String displayName = nickname != null ? nickname : user.getName();

            String cmd = content.substring(1).trim();

            if (cmd.equals("명령어")) {
                textChannel.sendMessage("명령어를 선택하거나 취소할 수 있습니다.")
                    .addActionRow(
                        getCommandDropdown() // 드롭다운
                    )
                    .addActionRow(
                        Button.danger("cancel_menu", "❌ 취소") // 버튼
                    )
                    .queue();
            }

            String returnMessage = handleCommand(cmd, displayName, user.getName());
            textChannel.sendMessage(returnMessage).queue();
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String componentId = event.getComponentId();

        // 2️⃣ 명령어 선택 드롭다운
        if (componentId.equals("command_selector")) {
            String selected = event.getValues().get(0);

            if (selected.equals("팀생성")) {
                event.replyModal(getTeamNameModal()).queue();
                return;
            }

            Member member = event.getMember();
            String nickname = member != null ? member.getNickname() : null;
            String displayName = nickname != null ? nickname : event.getUser().getName();

            String returnMessage = handleCommand(selected, displayName, event.getUser().getName());
            event.reply(returnMessage).queue();
        }

        if (componentId.equals("command_selector")) {
            // 선택된 팀 ID 가져오기
            long teamId = Long.parseLong(event.getValues().get(0));
            Guild guild = event.getGuild();

            // 전체 멤버 가져와서 드롭다운 생성
            guild.loadMembers().onSuccess(members -> {
                List<SelectOption> userOptions = members.stream()
                    .filter(member -> !member.getUser().isBot())
                    .limit(25)
                    .map(member -> SelectOption.of(member.getEffectiveName(), member.getId()))
                    .toList();

                StringSelectMenu userMenu = StringSelectMenu.create("add_team_user_" + teamId)
                    .setPlaceholder("추가할 유저 선택")
                    .setMaxValues(25)
                    .addOptions(userOptions)
                    .build();

                event.reply("✅ 팀을 선택했습니다. 이제 추가할 유저를 선택하세요:")
                    .addActionRow(userMenu)
                    .queue();
            });

            return;
        }

        if (componentId.startsWith("command_selector")) {
            long teamId = Long.parseLong(componentId.replace("add_team_user_", ""));

            List<Member> selectedMembers = event.getValues().stream()
                .map(userId -> event.getGuild().getMemberById(userId))
                .filter(member -> member != null)
                .toList();

            /*PutUserDTO dto = new PutUserDTO(teamId, selectedMembers);
            teamService.putUserInTeam(dto);
*/
            String addedNames = selectedMembers.stream()
                .map(Member::getEffectiveName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("선택된 사용자가 없습니다.");

            event.reply("✅ 다음 멤버들이 팀에 추가되었습니다: " + addedNames).queue();
            return;
        }
    }

    private String handleCommand(String message, String displayName, String userName) {
        if (message.startsWith("기록-")) {
            String datePart = message.replace("기록-", "").trim();
            return getLogsForSpecificDate(datePart);
        }

        Command cmd = commandMap.get(message);
        if (cmd != null) {
            return cmd.excute(displayName, userName);
        }

        return "잘못된 명령어입니다.";
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

    private StringSelectMenu getCommandDropdown() {
        return StringSelectMenu.create("command_selector")
            .setPlaceholder("실행할 명령어를 선택하세요!")
            .addOption("안녕", "안녕", "봇이 인사합니다.")
            .addOption("하기싫어", "하기싫어", "동기부여 멘트 출력")
            .addOption("오늘만쉴까", "오늘만쉴까", "오늘의 결심을 확인합니다.")
            .addOption("진짜하기싫다", "진짜하기싫다", "공부하라고 다그칩니다.")
            .addOption("그냥잘까", "그냥잘까", "공부하라고 다그칩니다.")
            .addOption("피곤해", "피곤해", "공부하라고 다그칩니다.")
            .addOption("월간기록", "월간기록", "본인의 월간 기록 확인")
            .addOption("주간기록", "주간기록", "본인의 주간 기록 확인")
            .addOption("일간기록", "일간기록", "본인의 일간 기록 확인")
            .addOption("전체월간기록", "전체월간기록", "전체 월간 기록 확인")
            .addOption("전체주간기록", "전체주간기록", "전체 주간 기록 확인")
            .addOption("전체일간기록", "전체일간기록", "전체 일간 기록 확인")
            .addOption("전체기록", "전체기록", "전체 월간, 주간, 일간 기록 확인")
            .addOption("명령어", "명령어", "명령어 목록을 다시 봅니다.")
            .addOption("팀생성", "팀생성", "팀을 생성합니다.")
            .addOption("팀원추가", "팀원추가", "팀원을 추가합니다.")
            .build();
    }

    private Modal getTeamNameModal() {
        return Modal.create("team_create_modal", "팀 이름 입력")
            .addActionRow(TextInput.create("team_name", "팀 이름을 입력하세요", TextInputStyle.SHORT)
                .setPlaceholder("예: 스터디1")
                .setRequired(true)
                .build()
            )
            .build();
    }

    @Override
    public void onModalInteraction(net.dv8tion.jda.api.events.interaction.ModalInteractionEvent event) {
        if (event.getModalId().equals("team_create_modal")) {
            String teamName = event.getValue("team_name").getAsString().trim();
            createVoiceChannel(teamName, event);
        }
    }

    private void createVoiceChannel(String teamName, ModalInteractionEvent event) {
        String teamCategoryName = teamName;
        String channelName = teamName + " 공부방";

        Guild guild = event.getGuild();

        guild.createCategory(teamCategoryName)
            .queue(category -> {
                category.createVoiceChannel(channelName)
                    .queue(vc -> {
                        String voiceChannelId = vc.getId();
                        CreateTeamDTO dto = new CreateTeamDTO(teamName, voiceChannelId, channelName);
                        teamService.createTeam(dto);

                        event.reply("✅ `" + teamCategoryName + "` 카테고리와 `" + vc.getName() + "` 채널이 생성되었습니다!").queue();
                    }, error -> event.reply("⚠️ 음성채널 생성 실패").queue());
            }, error -> event.reply("⚠️ 카테고리 생성 실패").queue());
    }

    @Override
    public void onButtonInteraction(net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent event) {
        if (event.getComponentId().equals("cancel_menu")) {
            event.reply("명령어 선택이 취소되었습니다!").setEphemeral(true).queue();
            event.getMessage().delete().queue();
        }
    }

    private void showUserSelectMenu(Guild guild, TextChannel channel, Long teamId) {
        // 멤버 비동기 로드
        guild.loadMembers().onSuccess(members -> {
            // 최대 25명까지만 드롭다운 가능 (Discord 제한)
            List<SelectOption> options = members.stream()
                .filter(member -> !member.getUser().isBot())
                .limit(25)
                .map(member -> SelectOption.of(member.getEffectiveName(), member.getId()))
                .toList();

            StringSelectMenu menu = StringSelectMenu.create("팀원추가" + teamId)
                .setPlaceholder("팀에 추가할 유저를 선택하세요")
                .setMaxValues(25)  // ✅ 최대 25명까지 다중 선택 허용
                .addOptions(options)
                .build();

            channel.sendMessage("팀에 추가할 유저를 선택해주세요:")
                .addActionRow(menu)
                .queue();
        });
    }

    private void showTeamSelectMenu(Guild guild, TextChannel channel) {
        List<Team> teams = teamService.getAllTeams();

        if (teams.isEmpty()) {
            channel.sendMessage("❌ 등록된 팀이 없습니다.").queue();
            return;
        }

        List<SelectOption> options = teams.stream()
            .map(team -> SelectOption.of(team.getName(), String.valueOf(team.getId())))
            .toList();

        StringSelectMenu menu = StringSelectMenu.create("team_selector")
            .setPlaceholder("팀을 선택하세요")
            .addOptions(options)
            .build();

        channel.sendMessage("👥 유저를 추가할 팀을 선택하세요:")
            .addActionRow(menu)
            .queue();
    }
}
