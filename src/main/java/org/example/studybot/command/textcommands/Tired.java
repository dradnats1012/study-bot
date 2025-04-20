package org.example.studybot.command.textcommands;

import org.example.studybot.command.TextCommands;
import org.springframework.stereotype.Component;

@Component
public class Tired implements TextCommands {
    @Override
    public String getName() {
        return "피곤해";
    }

    @Override
    public String getDescription() {
        return "공부하라고 다그칩니다.";
    }

    @Override
    public String execute(String displayName, String userName) {
        return displayName + " 가짜 피곤함이야.";
    }
}