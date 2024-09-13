package com.example.nacl.clickhouse.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * @author Mr.NaCl
 * @since 2024/5/16
 */

@Data
@Component
@ConfigurationProperties(prefix = "spring.datasource.click")
public class JdbcParamConfig {

    private String driverClassName ;
    private String url ;
    private String username ;
    private String password ;
    private Integer initialSize ;
    private Integer maxActive ;
    private Integer minIdle ;
    private Integer maxWait ;

}
