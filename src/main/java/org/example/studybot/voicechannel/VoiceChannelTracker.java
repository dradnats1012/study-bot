package org.example.studybot.voicechannel;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.example.studybot.TextChannelProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VoiceChannelTracker extends ListenerAdapter {

    @Autowired
    private VoiceChannelLogRepository repository;

    @Autowired
    private VoiceChannelProperties voiceChannelProperties;

    @Autowired
    private TextChannelProperties textChannelProperties;

    private final Map<Long, LocalDateTime> userJoinTimes = new HashMap<>();

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        var member = event.getEntity();
        var userId = member.getIdLong();
        var nickName = member.getNickname();
        var joinedChannel = event.getChannelJoined();
        var leftChannel = event.getChannelLeft();
        User user = member.getUser();

        String targetVoiceChannelName = voiceChannelProperties.getTargetChannelName();
        String targetTextChannelName = textChannelProperties.getTargetChannelName();

        var textChannels = event.getGuild().getTextChannelsByName(targetTextChannelName, true);
        TextChannel textChannel = textChannels != null && !textChannels.isEmpty() ? textChannels.get(0) : null;

        // 사용자가 새로운 채널에 입장했는지 확인
        if (joinedChannel != null && joinedChannel.getName().equals(targetVoiceChannelName)) {
            if (!userJoinTimes.containsKey(userId)) { // 이미 기록된 사용자가 아닌 경우만 처리
                userJoinTimes.put(userId, LocalDateTime.now());
                System.out.println(nickName + "님이 `" + joinedChannel.getName() + "` 채널에 입장했습니다.");

                if (textChannel != null) {
                    textChannel.sendMessage(
                        nickName + "님이 `" + joinedChannel.getName() + "` 채널에 입장했습니다."
                    ).queue();
                }
            }
        }

        // 사용자가 채널에서 완전히 나갔는지 확인
        if (leftChannel != null && joinedChannel == null) {
            LocalDateTime joinTime = userJoinTimes.remove(userId);

            if (joinTime != null) {
                long duration = ChronoUnit.SECONDS.between(joinTime, LocalDateTime.now());
                long hours = duration / 3600;
                long minutes = (duration % 3600) / 60;
                long seconds = duration % 60;

                VoiceChannelLog log = new VoiceChannelLog();
                log.setUserId(userId);
                log.setNickName(nickName);
                log.setChannelId(leftChannel.getIdLong());
                log.setChannelName(leftChannel.getName());
                log.setDuration(duration);
                log.setRecordedAt(LocalDateTime.now());
                log.setUserName(user.getName());

                repository.save(log);

                if (textChannel != null) {
                    textChannel.sendMessage(
                        nickName + "님이 `" + leftChannel.getName() + "` 채널에서 퇴장했습니다.\n" +
                            "머문 시간: " +
                            (hours > 0 ? hours + "시간 " : "") +
                            (minutes > 0 ? minutes + "분 " : "") +
                            seconds + "초"
                    ).queue();
                }
            }
        }
    }
}