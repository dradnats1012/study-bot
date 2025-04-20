package org.example.studybot.service;

import java.util.List;

import org.example.studybot.dto.teammember.PutTeamMemberDTO;
import org.example.studybot.dto.team.CreateTeamDTO;
import org.example.studybot.model.Channel;
import org.example.studybot.model.TeamMember;
import org.example.studybot.model.Team;
import org.example.studybot.repository.TeamRepository;
import org.example.studybot.repository.TeamMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.dv8tion.jda.api.JDA;

@Service
@Transactional(readOnly = true)
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private JDA jda;

    @Transactional
    public void createTeam(CreateTeamDTO createTeamDTO) {
        List<Channel> channels = channelService.createChannel(createTeamDTO);

        Team team = Team.builder()
            .name(createTeamDTO.teamName())
            .voiceChannel(channels.get(0))
            .chatChannel(channels.get(1))
            .build();

        teamRepository.save(team);
    }

    @Transactional
    public void deleteTeam(Long teamId) {
        Team team = teamRepository.getById(teamId);

        teamRepository.delete(team);
    }

    public void putTeamMemberInTeam(PutTeamMemberDTO putTeamMemberDTO) {
        Team team = teamRepository.getById(putTeamMemberDTO.teamId());

        putTeamMemberDTO.members().stream()
            .map(member -> {
                String discordId = member.getId();
                String name = member.getUser().getName();
                String nickName = member.getNickname();

                TeamMember teamMember = TeamMember.builder()
                    .discordId(discordId)
                    .nickName(nickName != null ? nickName : name)
                    .build();

                teamMember.setTeam(team);
                return teamMemberRepository.save(teamMember);
            });
    }

    public void deleteTeamMemberInTeam() {

    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }
}
