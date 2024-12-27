package org.example.studybot.voicechannel;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface VoiceChannelLogRepository extends Repository<VoiceChannelLog, Long> {
    VoiceChannelLog save(VoiceChannelLog log);

    @Query("SELECT v FROM VoiceChannelLog v WHERE v.recordedAt >= :start AND v.recordedAt < :end")
    List<VoiceChannelLog> findAllLogsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


    @Query("SELECT v FROM VoiceChannelLog v WHERE v.recordedAt >= :start AND v.recordedAt < :end AND v.userName = :userName")
    List<VoiceChannelLog> findLogsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, String userName);

    // 주간 기록 (이번 주)
    @Query("SELECT v FROM VoiceChannelLog v WHERE v.recordedAt >= :startOfWeek AND v.recordedAt < :endOfWeek")
    List<VoiceChannelLog> findWeeklyLogs(@Param("startOfWeek") String startOfWeek, @Param("endOfWeek") String endOfWeek);

    // 일간 기록 (오늘)
    @Query("SELECT v FROM VoiceChannelLog v WHERE v.recordedAt >= :startOfDay AND v.recordedAt < :endOfDay")
    List<VoiceChannelLog> findDailyLogs(@Param("startOfDay") String startOfDay, @Param("endOfDay") String endOfDay);

}
