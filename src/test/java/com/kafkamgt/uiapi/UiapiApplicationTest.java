package com.kafkamgt.uiapi;

import com.kafkamgt.uiapi.config.ManageDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes={UiapiApplication.class})
@ActiveProfiles("integrationtest")
@TestPropertySource(locations="classpath:test-application.properties")
public class UiapiApplicationTest {

    static {
        //UtilMethods.startEmbeddedCassandraServer();
        //UtilMethods.startEmbeddedJdbcDatabase();
    }

    @Before
    public void setup() throws Exception {
    }

    @Test
    public void contextLoads() throws Exception {
    }



}