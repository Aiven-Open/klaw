package com.kafkamgt.uiapi.config;


import com.kafkamgt.uiapi.helpers.db.rdbms.JdbcDataSourceCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@Configuration
@PropertySource(value= {"classpath:application.properties"})
public class DataSourceConfig {

    private static Logger LOG = LoggerFactory.getLogger(DataSourceConfig.class);

    @Autowired
    Environment environment;

    @Bean
    @Conditional(JdbcDataSourceCondition.class)
    public DataSource datasource() throws PropertyVetoException {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(environment.getProperty("spring.datasource.driver.class"));
        dataSource.setUrl(environment.getProperty("spring.datasource.url"));
        dataSource.setUsername(environment.getProperty("spring.datasource.username"));
        dataSource.setPassword(environment.getProperty("spring.datasource.password"));
        LOG.info("Connecting RDBMS datasource..");
        return dataSource;
    }
}