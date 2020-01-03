package com.kafkamgt.uiapi.config;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.junit.Assert.*;

@Configuration
public class DataSourceConfigTest {

    @Bean
    @Primary
    public DataSourceConfig dataSourceConfig() {
        return Mockito.mock(DataSourceConfig.class);
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void dataSource() {
    }

    @Test
    public void jpaTransactionManager() {
    }

    @Test
    public void entityManagerFactoryBean() {
    }
}