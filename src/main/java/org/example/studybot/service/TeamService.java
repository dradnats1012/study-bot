package org.example.studybot.service;

import org.example.studybot.dto.team.CreateTeamDTO;
import org.example.studybot.model.Channel;
import org.example.studybot.model.Team;
import org.example.studybot.repository.ChannelRepository;
import org.example.studybot.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import jakarta.transaction.Transactional;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private JDA jda;

    public void createTeam(CreateTeamDTO createTeamDTO) {
        Channel channel = Channel.builder()
            .discordChannelId(createTeamDTO.channelId())
            .channelName(createTeamDTO.channelName())
            .build();

        Team team = Team.builder()
            .name(createTeamDTO.teamName())
            .channel(channel)
            .build();

        teamRepository.save(team);
    }

    @Transactional
    public void deleteTeam(Long teamId) {
        Team team = teamRepository.getById(teamId);

        Channel channel = team.getChannel();
        String discordChannelId = channel.getDiscordChannelId();

        // Discord 채널 삭제 요청 (비동기)
        deleteDiscordVoiceChannel(discordChannelId);

        // DB에서 Team 삭제 (orphanRemoval로 Channel도 자동 삭제)
        teamRepository.delete(team);
    }

    private void deleteDiscordVoiceChannel(String discordChannelId) {
        Guild guild = jda.getGuildById("디스코드_서버_ID"); // 실제 Guild ID로 변경!

        if (guild != null) {
            guild.getVoiceChannelById(discordChannelId)
                .delete()
                .queue(
                    success -> System.out.println("✅ Discord 음성채널 삭제 완료"),
                    error -> System.err.println("❌ Discord 음성채널 삭제 실패: " + error.getMessage())
                );
        }
    }

    public void putUserInTeam() {

    }

    public void deleteUserInTeam() {

    }
}
