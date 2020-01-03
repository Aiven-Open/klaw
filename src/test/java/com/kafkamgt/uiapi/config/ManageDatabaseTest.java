package com.kafkamgt.uiapi.config;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ManageDatabaseTest {

    @Bean
    @Primary
    public ManageDatabase manageDatabase() {
        return Mockito.mock(ManageDatabase.class);
    }

    @Bean
    @Primary
    public SecurityConfig securityConfig() {
        return Mockito.mock(SecurityConfig.class);
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void loadDb() {
    }

    @Test
    public void getHandleDbRequests() {
    }

    @Test
    public void handleJdbc() {
    }

    @Test
    public void handleCassandra() {
    }

    @Test
    public void selectAllUsersInfo() {
    }
}