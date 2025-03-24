package org.example.studybot.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JDAConfig {

    @Value("${discord.bot.token}")
    private String token;

    @Bean
    public JDA jda() {
        try {
            return JDABuilder.createDefault(token).build();
        } catch (Exception e) {
            throw new RuntimeException("JDA 초기화 중 오류 발생", e);
        }
    }
}
