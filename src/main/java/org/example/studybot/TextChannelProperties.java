package org.example.studybot;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "text-channel")
public class TextChannelProperties {

    private String targetChannelName;

    public String getTargetChannelName() {
        return targetChannelName;
    }

    public void setTargetChannelName(String targetChannelName) {
        this.targetChannelName = targetChannelName;
    }
}
