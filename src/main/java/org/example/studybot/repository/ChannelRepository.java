package org.example.studybot.repository;

import org.example.studybot.model.Channel;
import org.springframework.data.repository.Repository;

public interface ChannelRepository extends Repository<Channel, Long> {

    void save(Channel channel);
}
