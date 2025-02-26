package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ServletComponentScan
@MapperScan("com.example.nacl.clickhouse.mapper")
public class CommonUseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonUseApplication.class, args);
    }

}
