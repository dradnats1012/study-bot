package org.example.studybot.model;

@FunctionalInterface
public interface Command {

    String excute(String displayName, String userName);
}
