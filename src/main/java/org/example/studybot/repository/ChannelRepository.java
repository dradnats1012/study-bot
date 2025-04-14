package org.example.studybot.repository;

import java.util.Optional;

import org.example.studybot.model.Channel;
import org.springframework.data.repository.Repository;

public interface ChannelRepository extends Repository<Channel, Long> {

    Channel save(Channel channel);

    void deleteById(Long id);

    Optional<Channel> findByDiscordChannelId(String discordChannelId);

    default Channel getByDiscordChannelId(String discordChannelId){
        return findByDiscordChannelId(discordChannelId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다"));
    }
}
