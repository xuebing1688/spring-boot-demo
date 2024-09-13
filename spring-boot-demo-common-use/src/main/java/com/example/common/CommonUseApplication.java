package com.example.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CommonUseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonUseApplication.class, args);
    }

}
