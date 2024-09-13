package com.example.nacl.clickhouse;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.nacl.clickhouse.mapper")
public class ClickHouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClickHouseApplication.class, args);
    }

}
