package com.example.nacl.clickhouse.config;

 import com.clickhouse.jdbc.ClickHouseDataSource;
 import com.zaxxer.hikari.HikariConfig;
 import com.zaxxer.hikari.HikariDataSource;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.boot.context.properties.ConfigurationProperties;
 import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 import ru.yandex.clickhouse.settings.ClickHouseProperties;

 import javax.annotation.Resource;
import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;

/**
 * @author Mr.NaCl
 * @since 2024/5/16
 */
// @Configuration
public class DruidConfig {

  @Resource
  private JdbcParamConfig  jdbcParamConfig ;

  @Value("${spring.datasource.url}")
  private String url;

  @Value("${spring.datasource.username}")
  private String username;

  @Value("${spring.datasource.password}")
  private String password;

  @Value("${spring.datasource.driver-class-name}")
  private String driverClassName;

  /*@Bean
  @ConfigurationProperties(prefix = "spring.datasource")
  public DataSource clickHouseDataSource() throws SQLException {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(url);
    dataSource.setUsername(username);
    dataSource.setPassword(password);
    dataSource.setDriverClassName(driverClassName);
    return dataSource;
  }*/

  @Bean
  public DataSource clickHouseDataSource() throws SQLException {
    // 创建 ClickHouseProperties 实例并设置连接属性
    ClickHouseProperties properties = new ClickHouseProperties();
    properties.setUser("default");
    properties.setPassword("");
    properties.setSocketTimeout(10000);
    properties.setConnectionTimeout(10000);
    properties.setCompress(true);

    // 创建 ClickHouseDataSource 实例
    ClickHouseDataSource clickhouseDataSource = new ClickHouseDataSource(
      "jdbc:clickhouse://192.168.58.130:8123/default", properties.asProperties()
      //"jdbc:clickhouse://192.168.58.130:8123,192.168.58.131:8123,192.168.58.132:8123/default", properties.asProperties()
    );

    // 配置 HikariCP
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setDataSource(clickhouseDataSource);
    hikariConfig.setMaximumPoolSize(10);  // 设置最大连接数
    hikariConfig.setMinimumIdle(5);       // 设置最小空闲连接数
    hikariConfig.setIdleTimeout(30000);   // 设置空闲连接超时时间
    hikariConfig.setConnectionTimeout(10000); // 设置连接超时时间

    // 创建 HikariDataSource 实例
    HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);

    try (Connection connection = hikariDataSource.getConnection();
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery("SELECT * FROM system.tables")) {

          while (resultSet.next()) {
            System.out.println(resultSet.getString(1));
          }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      // 关闭 HikariDataSource
      hikariDataSource.close();
    }
    return hikariDataSource;
  }


}
