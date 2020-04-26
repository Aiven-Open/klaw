package com.kafkamgt.uiapi.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Principal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UtilServiceTest {

    @Mock
    UserDetails userDetails;

    private UtilService utilService;

    @Before
    public void setUp() throws Exception {
        utilService = new UtilService();
    }

    @Test
    public void getUserDetails() {
        assertTrue(true);
    }
}