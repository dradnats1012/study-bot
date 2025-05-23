package org.example.studybot.repository;

import java.util.Optional;

import org.example.studybot.model.Channel;
import org.example.studybot.model.TeamMember;
import org.example.studybot.model.Record;
import org.springframework.data.repository.Repository;

public interface RecordRepository extends Repository<Record, Long> {
    Record save(Record record);

    Optional<Record> findTopByTeamMemberAndChannelAndDurationIsNullOrderByRecordedAtDesc(TeamMember teamMember, Channel channel);
}
