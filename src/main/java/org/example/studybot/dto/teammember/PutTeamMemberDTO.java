package org.example.studybot.dto.teammember;

import java.util.List;

import net.dv8tion.jda.api.entities.Member;

public record PutTeamMemberDTO(
    long teamId,
    List<Member> members
) {
}
