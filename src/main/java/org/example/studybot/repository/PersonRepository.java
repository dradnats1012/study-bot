package org.example.studybot.repository;

import java.util.Optional;

import org.example.studybot.model.Person;
import org.springframework.data.repository.Repository;

public interface PersonRepository extends Repository<Person, Long> {

    Optional<Person> findById(Long id);

    default Person getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));
    }

    Person save(Person person);

    void deleteById(Long id);

    Optional<Person> findByDiscordId(String id);

    default Person getByDiscordId(String id) {
        return findByDiscordId(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));
    }
}
