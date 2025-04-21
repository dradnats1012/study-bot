package org.example.studybot.command;

import lombok.RequiredArgsConstructor;

import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandHandler {

    private final CommandRegistry registry;

    public String handle(String commandName, String displayName, String userName) {
        Commands command = registry.getCommand(commandName);
        if (command == null)
            return "잘못된 명령어입니다.";
        return command.execute(displayName, userName);
    }

    public StringSelectMenu getTextCommandsDropdown() {
        var menu = StringSelectMenu.create("command_selector_text")
            .setPlaceholder("텍스트 명령어를 선택하세요!");
        registry.getTextCommands().forEach(cmd ->
            menu.addOption(cmd.getName(), cmd.getName(), cmd.getDescription()));
        return menu.build();
    }

    public StringSelectMenu getRecordCommandsDropdown() {
        var menu = StringSelectMenu.create("command_selector_log")
            .setPlaceholder("기록 명령어를 선택하세요!");
        registry.getRecordCommands().forEach(cmd ->
            menu.addOption(cmd.getName(), cmd.getName(), cmd.getDescription()));
        return menu.build();
    }
}