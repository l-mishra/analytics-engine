package com.poc.analytics.analyticsservice.config;

import com.clickhouse.jdbc.ClickHouseDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
public class ClickHouseConfig {

    @Value("${clickhouse.url}")
    private String clickhouseUrl;

    @Value("${clickhouse.username}")
    private String username;

    @Value("${clickhouse.password}")
    private String password;

    @Bean
    public DataSource clickHouseDataSource() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", password);
        return new ClickHouseDataSource(clickhouseUrl, properties);
    }

    @Bean
    public JdbcTemplate clickHouseJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}