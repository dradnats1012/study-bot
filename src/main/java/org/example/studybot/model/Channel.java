package org.example.studybot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "channel")
@Getter
@Setter
@NoArgsConstructor
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "discord_channel_id", nullable = false)
    private String discordChannelId;

    @Column(name = "channel_name", nullable = false)
    private String channelName;

    @Builder
    public Channel(
        String discordChannelId,
        String channelName
    ) {
        this.discordChannelId = discordChannelId;
        this.channelName = channelName;
    }
}
