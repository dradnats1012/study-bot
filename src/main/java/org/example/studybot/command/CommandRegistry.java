package org.example.studybot.command;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CommandRegistry {
    private final List<Commands> commandList;
    private final Map<String, Commands> commandMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (Commands cmd : commandList) {
            commandMap.put(cmd.getName(), cmd);
        }
    }

    public Commands getCommand(String name) {
        return commandMap.get(name);
    }

    public List<TextCommands> getTextCommands() {
        return commandList.stream()
            .filter(c -> c instanceof TextCommands)
            .map(c -> (TextCommands)c)
            .toList();
    }

    public List<RecordCommands> getRecordCommands() {
        return commandList.stream()
            .filter(c -> c instanceof RecordCommands)
            .map(c -> (RecordCommands)c)
            .toList();
    }
}