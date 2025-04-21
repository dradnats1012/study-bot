package org.example.studybot.command.recordcommands;

import org.example.studybot.command.RecordCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyMonthly implements RecordCommands {

    @Autowired
    private RecordManager manager;

    @Override
    public String getName() {
        return "월간기록";
    }

    @Override
    public String getDescription() {
        return "나의 월간기록을 확인합니다";
    }

    @Override
    public String execute(String displayName, String userName) {
        return manager.getMonthlyLogs(userName);
    }
}

