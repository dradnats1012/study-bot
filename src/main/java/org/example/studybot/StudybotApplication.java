package org.example.studybot;

import javax.security.auth.login.LoginException;

import org.example.studybot.voicechannel.VoiceChannelTracker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

@SpringBootApplication
public class StudybotApplication {

    public static void main(String[] args){
        ApplicationContext context = SpringApplication.run(StudybotApplication.class, args);
        DiscordBotToken discordBotTokenEntity = context.getBean(DiscordBotToken.class);
        String discordBotToken = discordBotTokenEntity.getDiscordBotToken();

        JDA jda = JDABuilder.createDefault(discordBotToken)
            .setActivity(Activity.playing("메시지 기다리는 중!"))
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .addEventListeners(context.getBean(StudyBotDiscordListener.class))
            .build();

        JDA jdaVoice = JDABuilder.createDefault(discordBotToken)
            .setActivity(Activity.playing("메시지 기다리는 중!"))
            .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES)
            .addEventListeners(context.getBean(VoiceChannelTracker.class))
            .build();
    }
}
