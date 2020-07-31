package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.model.ServerConfigProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ServerConfigServiceTest {

    ServerConfigService serverConfigService;

    private Environment env;

    @Before
    public void setUp() {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext();
        this.env = context.getEnvironment();

        serverConfigService = new ServerConfigService(env);

    }

    @Test
    public void getAllProps() {
        serverConfigService.getAllProperties();
        List<ServerConfigProperties> list =  serverConfigService.getAllProps();
        assertEquals(list.size() > 0, true);
    }

}