package org.example.studybot.repository;

import java.util.Optional;

import org.example.studybot.model.TeamMember;
import org.springframework.data.repository.Repository;

public interface TeamMemberRepository extends Repository<TeamMember, Long> {

    Optional<TeamMember> findById(Long id);

    default TeamMember getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));
    }

    TeamMember save(TeamMember teamMember);

    void deleteById(Long id);

    Optional<TeamMember> findByDiscordId(String id);

    default TeamMember getByDiscordId(String id) {
        return findByDiscordId(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));
    }
}
