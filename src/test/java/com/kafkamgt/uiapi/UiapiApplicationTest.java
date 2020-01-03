package com.kafkamgt.uiapi;

import com.kafkamgt.uiapi.config.*;
import com.kafkamgt.uiapi.controller.UiConfigController;
import com.kafkamgt.uiapi.service.UiConfigControllerService;
import com.kafkamgt.uiapi.service.UtilService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//@RunWith(SpringRunner.class)
//@SpringBootTest
//@AutoConfigureMockMvc
public class UiapiApplicationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    ManageDatabase manageDatabase;

    @Autowired
    SecurityConfig securityConfig;

    @Mock
    UtilService utils;

    @Before
    public void setup() throws Exception {
//        ReflectionTestUtils.setField(manageDatabase, "utils", utils);
//        ReflectionTestUtils.setField(securityConfig, "utils", utils);
    }

    @Test
    public void contextLoads() throws Exception {
//        when(utils.validateLicense(anyString(), any())).thenReturn(true);
    }

}