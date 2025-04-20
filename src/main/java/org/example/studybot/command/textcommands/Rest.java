package org.example.studybot.command.textcommands;

import org.example.studybot.command.TextCommands;
import org.springframework.stereotype.Component;

@Component
public class Rest implements TextCommands {
    @Override
    public String getName() {
        return "오늘만쉴까";
    }

    @Override
    public String getDescription() {
        return "공부하라고 다그칩니다.";
    }

    @Override
    public String execute(String displayName, String userName) {
        return displayName + " 그럼 평생 쉬겠지";
    }
}

