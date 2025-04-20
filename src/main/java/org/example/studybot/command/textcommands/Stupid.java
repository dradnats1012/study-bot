package org.example.studybot.command.textcommands;

import org.example.studybot.command.TextCommands;
import org.springframework.stereotype.Component;

@Component
public class Stupid implements TextCommands {
    @Override
    public String getName() {
        return "멍청이";
    }

    @Override
    public String getDescription() {
        return "바보멍청이";
    }

    @Override
    public String execute(String displayName, String userName) {
        return displayName + " 바보멍청이";
    }
}

