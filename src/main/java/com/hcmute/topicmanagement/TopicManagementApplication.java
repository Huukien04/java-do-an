package com.hcmute.topicmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TopicManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TopicManagementApplication.class, args);
    }
}
