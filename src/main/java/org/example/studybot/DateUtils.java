package org.example.studybot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static String getStartOfDay() {
        return LocalDate.now().atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String getStartOfWeek() {
        return LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1)
            .atStartOfDay()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String getStartOfMonth() {
        return LocalDate.now().withDayOfMonth(1)
            .atStartOfDay()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String getNow() {
        return LocalDate.now().atTime(23, 59, 59).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
