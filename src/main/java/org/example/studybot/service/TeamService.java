package org.example.studybot.service;

import java.util.List;
import java.util.stream.Collectors;

import org.example.studybot.dto.person.PutPersonDTO;
import org.example.studybot.dto.team.CreateTeamDTO;
import org.example.studybot.model.Channel;
import org.example.studybot.model.Person;
import org.example.studybot.model.Team;
import org.example.studybot.repository.TeamRepository;
import org.example.studybot.repository.PersonRepository;
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
    private PersonRepository personRepository;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private JDA jda;

    @Transactional
    public void createTeam(CreateTeamDTO createTeamDTO) {
        Channel channel = channelService.createChannel(createTeamDTO);

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

        teamRepository.delete(team);
    }

    public void putUserInTeam(PutPersonDTO putPersonDTO) {
        Team team = teamRepository.getById(putPersonDTO.teamId());

        putPersonDTO.members().stream()
            .map(member -> {
                String discordId = member.getId();
                String name = member.getUser().getName();
                String nickName = member.getNickname();

                Person person = Person.builder()
                    .discordId(discordId)
                    .nickName(nickName != null ? nickName : name)
                    .build();

                person.setTeam(team);
                return personRepository.save(person);
            });
    }

    public void deletePersonInTeam() {

    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }
}
