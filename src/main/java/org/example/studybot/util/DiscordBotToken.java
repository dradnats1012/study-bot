package org.example.studybot.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DiscordBotToken {

    @Value("${discord.bot.token}")
    private String discordBotToken;

    public String getDiscordBotToken() {
        return discordBotToken;
    }
}
