package org.example.studybot.voicechannel;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface VoiceChannelLogRepository extends Repository<VoiceChannelLog, Long> {
    VoiceChannelLog save(VoiceChannelLog log);

    @Query("SELECT v FROM VoiceChannelLog v WHERE v.userId = :userId AND v.joinedAt IS NOT NULL AND v.leftAt IS NULL ORDER BY v.joinedAt DESC")
    List<VoiceChannelLog> findByUserIdAndLeftAtIsNull(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE VoiceChannelLog v SET v.leftAt = :leftAt WHERE v.userId = :userId AND v.joinedAt IS NOT NULL AND v.leftAt IS NULL")
    int updateLeftAt(@Param("userId") Long userId, @Param("leftAt") LocalDateTime leftAt);

    @Query("SELECT v FROM VoiceChannelLog v WHERE v.leftAt IS NOT NULL AND v.joinedAt >= :start AND v.joinedAt < :end")
    List<VoiceChannelLog> findAllLogsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT v FROM VoiceChannelLog v WHERE v.leftAt IS NOT NULL AND v.joinedAt >= :start AND v.joinedAt < :end AND v.userName = :userName")
    List<VoiceChannelLog> findLogsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, String userName);

    @Query("SELECT v FROM VoiceChannelLog v WHERE v.leftAt IS NOT NULL AND v.joinedAt >= :startOfWeek AND v.joinedAt < :endOfWeek")
    List<VoiceChannelLog> findWeeklyLogs(@Param("startOfWeek") String startOfWeek, @Param("endOfWeek") String endOfWeek);

    @Query("SELECT v FROM VoiceChannelLog v WHERE v.leftAt IS NOT NULL AND v.joinedAt >= :startOfDay AND v.joinedAt < :endOfDay")
    List<VoiceChannelLog> findDailyLogs(@Param("startOfDay") String startOfDay, @Param("endOfDay") String endOfDay);
}
