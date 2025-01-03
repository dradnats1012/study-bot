package org.example.studybot.voicechannel;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "voice-channel")
public class VoiceChannelProperties {

    private String targetChannelName;

    public String getTargetChannelName() {
        return targetChannelName;
    }

    public void setTargetChannelName(String targetChannelName) {
        this.targetChannelName = targetChannelName;
    }
}