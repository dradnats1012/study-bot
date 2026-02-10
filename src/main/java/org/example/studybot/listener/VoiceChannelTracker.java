package org.example.studybot.listener;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.example.studybot.util.TextChannelProperties;
import org.example.studybot.voicechannel.VoiceChannelLog;
import org.example.studybot.voicechannel.VoiceChannelLogRepository;
import org.example.studybot.voicechannel.VoiceChannelProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VoiceChannelTracker extends ListenerAdapter {

    @Autowired
    private VoiceChannelLogRepository repository;

    @Autowired
    private VoiceChannelProperties voiceChannelProperties;

    @Autowired
    private TextChannelProperties textChannelProperties;

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        var member = event.getEntity();
        var userId = member.getIdLong();
        var nickName = member.getEffectiveName();
        var joinedChannel = event.getChannelJoined();
        var leftChannel = event.getChannelLeft();
        User user = member.getUser();

        String targetVoiceChannelName = voiceChannelProperties.getTargetChannelName();
        String targetTextChannelName = textChannelProperties.getTargetChannelName();

        var textChannels = event.getGuild().getTextChannelsByName(targetTextChannelName, true);
        TextChannel textChannel = textChannels != null && !textChannels.isEmpty() ? textChannels.get(0) : null;

        // 사용자가 새로운 채널에 입장했는지 확인
        if (joinedChannel != null && joinedChannel.getName().equals(targetVoiceChannelName)) {
            List<VoiceChannelLog> existingLogs = repository.findByUserIdAndLeftAtIsNull(userId);
            if (existingLogs.isEmpty()) {
                VoiceChannelLog voiceLog = new VoiceChannelLog();
                voiceLog.setUserId(userId);
                voiceLog.setNickName(nickName);
                voiceLog.setChannelId(joinedChannel.getIdLong());
                voiceLog.setChannelName(joinedChannel.getName());
                voiceLog.setJoinedAt(LocalDateTime.now());
                voiceLog.setUserName(user.getName());
                repository.save(voiceLog);

                if (textChannel != null) {
                    textChannel.sendMessage(
                        nickName + "님이 `" + joinedChannel.getName() + "` 채널에 입장했습니다."
                    ).queue();
                }
            }
        }

        // 사용자가 타겟 채널에서 나갔는지 확인 (완전 퇴장 또는 다른 채널로 이동)
        if (leftChannel != null && leftChannel.getName().equals(targetVoiceChannelName)) {
            List<VoiceChannelLog> logs = repository.findByUserIdAndLeftAtIsNull(userId);

            if (!logs.isEmpty()) {
                VoiceChannelLog voiceLog = logs.get(0); // 가장 최근 세션
                LocalDateTime leftAt = LocalDateTime.now();

                // @Modifying 쿼리로 직접 DB 업데이트 (모든 미완료 세션 일괄 종료)
                int updated = repository.updateLeftAt(userId, leftAt);
                log.info("퇴장 처리: userId={}, updated={}", userId, updated);

                if (updated > 0) {
                    long duration = ChronoUnit.SECONDS.between(voiceLog.getJoinedAt(), leftAt);
                    long hours = duration / 3600;
                    long minutes = (duration % 3600) / 60;
                    long seconds = duration % 60;

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
            } else {
                log.warn("퇴장 처리 실패: userId={}에 대한 진행 중인 세션을 찾을 수 없음", userId);
            }
        }
    }
}
