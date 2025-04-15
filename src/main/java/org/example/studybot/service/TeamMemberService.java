package org.example.studybot.service;

import org.example.studybot.dto.teammember.CreateTeamMemberDTO;
import org.example.studybot.model.TeamMember;
import org.example.studybot.repository.TeamMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeamMemberService {

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    public TeamMember createTeamMember(CreateTeamMemberDTO createTeamMemberDTO){
        TeamMember teamMember = TeamMember.builder()
            .discordId(createTeamMemberDTO.discordId())
            .nickName(createTeamMemberDTO.nickName())
            .build();

        teamMemberRepository.save(teamMember);

        return teamMember;
    }

    public void deleteTeamMember(Long id){
        teamMemberRepository.deleteById(id);
    }
}
