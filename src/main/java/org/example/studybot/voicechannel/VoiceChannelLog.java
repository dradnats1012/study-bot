package org.example.studybot.voicechannel;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "voice_channel_logs")
@Getter
@Setter
public class VoiceChannelLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String nickName;
    private Long channelId;
    private String channelName;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private String userName;

    public Long getDuration() {
        if (leftAt == null) {
            return null;
        }
        return ChronoUnit.SECONDS.between(joinedAt, leftAt);
    }
}
