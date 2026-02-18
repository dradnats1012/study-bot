package org.example.studybot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StudybotApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudybotApplication.class, args);
    }
}
