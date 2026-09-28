package com.dogfood.submission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@ComponentScan(basePackages = {"com.dogfood.submission", "com.dogfood.common"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
