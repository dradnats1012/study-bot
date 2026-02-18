package org.example.studybot.util;

import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JDAConfig {

    @Value("${discord.bot.token}")
    private String token;

    @Bean
    public JDA jda(List<ListenerAdapter> listeners) {
        try {
            return JDABuilder.createDefault(token)
                .setActivity(Activity.playing("메시지 기다리는 중!"))
                .setMaxReconnectDelay(32)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(listeners.toArray())
                .build();
        } catch (Exception e) {
            throw new RuntimeException("JDA 초기화 중 오류 발생", e);
        }
    }
}
