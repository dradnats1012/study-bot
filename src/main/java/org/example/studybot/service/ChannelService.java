package org.example.studybot.service;

import org.example.studybot.dto.team.CreateTeamDTO;
import org.example.studybot.model.Channel;
import org.example.studybot.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChannelService {

    @Autowired
    private ChannelRepository channelRepository;

    public Channel createChannel(CreateTeamDTO createTeamDTO) {
        Channel channel = Channel.builder()
            .discordChannelId(createTeamDTO.channelId())
            .channelName(createTeamDTO.channelName())
            .build();

        channelRepository.save(channel);

        return channel;
    }

    public void deleteChannel(Long channelId){
        channelRepository.deleteById(channelId);
    }
}
