package org.example.studybot.service;

import java.util.List;

import org.example.studybot.dto.team.CreateTeamDTO;
import org.example.studybot.model.Channel;
import org.example.studybot.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChannelService {

    @Autowired
    private ChannelRepository channelRepository;

    public List<Channel> createChannel(CreateTeamDTO createTeamDTO) {
        Channel chatChannel = Channel.builder()
            .discordChannelId(createTeamDTO.channelId())
            .channelName(createTeamDTO.channelName())
            .channelType(Channel.ChannelType.CHAT)
            .build();

        Channel voiceChannel = Channel.builder()
            .discordChannelId(createTeamDTO.channelId())
            .channelName(createTeamDTO.channelName())
            .channelType(Channel.ChannelType.VOICE)
            .build();

        channelRepository.save(chatChannel);
        channelRepository.save(voiceChannel);

        return List.of(chatChannel, voiceChannel);
    }

    public void deleteChannel(Long channelId){
        channelRepository.deleteById(channelId);
    }
}
