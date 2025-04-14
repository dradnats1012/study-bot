package org.example.studybot.dto.person;

import java.util.List;

import net.dv8tion.jda.api.entities.Member;

public record PutPersonDTO(
    long teamId,
    List<Member> members
) {
}
