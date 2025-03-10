package org.example.studybot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "channel")
@Getter
@Setter
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long channelId;

    @Column(name = "discord_channel_id", nullable = false)
    private String discordChannelId;

    @Column(name = "channel_name", nullable = false)
    private String channelName;

    @OneToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
}
