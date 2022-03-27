package com.xin.wechatpayment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * created by xin 2022-03-28
 */
@Configuration
@EnableTransactionManagement
public class DataSourceConfig {
    @Bean(name="dataSource")
    @ConfigurationProperties(prefix = "druid.datasource")
    public DataSource dataSource() {
        return new DruidDataSource();
    }
}
