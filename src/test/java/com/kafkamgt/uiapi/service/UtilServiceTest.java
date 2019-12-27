package com.kafkamgt.uiapi.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class UtilServiceTest {

    @Mock
    UserDetails userDetails;

    UtilService utilService;

    @Before
    public void setUp() throws Exception {
        utilService = new UtilService();
        utilService.setUserDetails(userDetails);
    }

    @Test
    public void getUserDetails() {
        ReflectionTestUtils.setField(utilService, "licenseKey", "fjsda423h");
        ReflectionTestUtils.setField(utilService, "orgName", "organization");
        UserDetails ud = utilService.getUserDetails();
        assertEquals(userDetails, ud);
    }

}