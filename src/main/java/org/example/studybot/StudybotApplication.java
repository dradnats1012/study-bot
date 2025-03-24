package org.example.studybot;


import org.example.studybot.listener.StudyBotDiscordListener;
import org.example.studybot.util.DiscordBotToken;
import org.example.studybot.listener.VoiceChannelTracker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

@SpringBootApplication
@EnableScheduling
public class StudybotApplication {

    public static void main(String[] args){
        ApplicationContext context = SpringApplication.run(StudybotApplication.class, args);
        DiscordBotToken discordBotTokenEntity = context.getBean(DiscordBotToken.class);
        String discordBotToken = discordBotTokenEntity.getDiscordBotToken();

        JDA jda = JDABuilder.createDefault(discordBotToken)
            .setActivity(Activity.playing("메시지 기다리는 중!"))
            .setMaxReconnectDelay(32)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES) // 필요한 인텐트 모두 활성화
            .addEventListeners(
                context.getBean(StudyBotDiscordListener.class),
                context.getBean(VoiceChannelTracker.class)
            )
            .build();

        /*JDA jda = JDABuilder.createDefault(discordBotToken)
            .setActivity(Activity.playing("메시지 기다리는 중!"))
            .setMaxReconnectDelay(32)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .addEventListeners(context.getBean(StudyBotDiscordListener.class))
            .build();

        JDA jdaVoice = JDABuilder.createDefault(discordBotToken)
            .setActivity(Activity.playing("메시지 기다리는 중!"))
            .setMaxReconnectDelay(32)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES)
            .addEventListeners(context.getBean(VoiceChannelTracker.class))
            .build();*/
    }
}
