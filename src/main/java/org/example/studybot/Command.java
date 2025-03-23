package org.example.studybot;

@FunctionalInterface
public interface Command {

    String excute(String displayName, String userName);
}
