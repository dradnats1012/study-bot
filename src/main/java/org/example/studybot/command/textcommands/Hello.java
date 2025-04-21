package org.example.studybot.command.textcommands;

import org.example.studybot.command.TextCommands;
import org.springframework.stereotype.Component;

@Component
public class Hello implements TextCommands {
    @Override
    public String getName() {
        return "안녕";
    }

    @Override
    public String getDescription() {
        return "봇이 인사합니다.";
    }

    @Override
    public String execute(String displayName, String userName) {
        return displayName + " 얼른 공부좀해!";
    }
}

