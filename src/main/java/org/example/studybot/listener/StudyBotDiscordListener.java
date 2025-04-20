package org.example.studybot.listener;

import java.util.List;

import org.example.studybot.command.CommandHandler;
import org.example.studybot.dto.team.CreateTeamDTO;
import org.example.studybot.model.Team;
import org.example.studybot.service.TeamService;
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

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StudyBotDiscordListener extends ListenerAdapter {

    @Autowired
    private TeamService teamService;

    @Autowired
    private CommandHandler commandHandler;

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
            String nickname = member != null ? member.getNickname() : null;
            String displayName = nickname != null ? nickname : user.getName();
            String cmd = content.substring(1).trim();

            if (cmd.equals("명령어")) {
                textChannel.sendMessage("명령어를 선택하거나 취소할 수 있습니다.")
                    .addActionRow(commandHandler.getTextCommandsDropdown())
                    .addActionRow(Button.danger("cancel_menu", "취소"))
                    .queue();
                return;
            }

            if (cmd.equals("기록")) {
                textChannel.sendMessage("기록 관련 명령어를 선택하세요.")
                    .addActionRow(commandHandler.getRecordCommandsDropdown())
                    .addActionRow(Button.danger("cancel_menu", "취소"))
                    .queue();
                return;
            }

            String returnMessage = commandHandler.handle(cmd, displayName, user.getName());
            textChannel.sendMessage(returnMessage).queue();
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String componentId = event.getComponentId();

        if (componentId.startsWith("command_selector")) {
            String selected = event.getValues().get(0);

            Member member = event.getMember();
            String nickname = member != null ? member.getNickname() : null;
            String displayName = nickname != null ? nickname : event.getUser().getName();

            String returnMessage = commandHandler.handle(selected, displayName, event.getUser().getName());

            event.reply(returnMessage).queue(); // 답장 먼저
            event.getMessage().delete().queue(); // 드롭다운 포함된 원본 메시지 삭제
        }
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
