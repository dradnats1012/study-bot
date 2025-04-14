package org.example.studybot.repository;

import java.util.List;
import java.util.Optional;

import org.example.studybot.model.Team;
import org.springframework.data.repository.Repository;

public interface TeamRepository extends Repository<Team, Long> {

    Team save(Team team);

    Optional<Team> findById(Long id);

    default Team getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다"));
    }

    void delete(Team team);

    List<Team> findAll();
}
