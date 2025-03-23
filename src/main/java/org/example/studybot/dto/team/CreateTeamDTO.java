package org.example.studybot.dto.team;

public record CreateTeamDTO(
    String teamName,
    String channelId,
    String channelName
) {
}
