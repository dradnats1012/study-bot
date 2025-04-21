package org.example.studybot.command.recordcommands;

import org.example.studybot.command.RecordCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AllDaily implements RecordCommands {

    @Autowired
    private RecordManager manager;

    @Override
    public String getName() {
        return "전체일간기록";
    }

    @Override
    public String getDescription() {
        return "모두의 일간기록을 확인합니다";
    }

    @Override
    public String execute(String displayName, String userName) {
        return manager.getAllDailyLogs();
    }
}

