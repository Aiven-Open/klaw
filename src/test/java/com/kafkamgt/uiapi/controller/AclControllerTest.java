package com.kafkamgt.uiapi.controller;

import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.service.AclControllerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AclControllerTest {

    @Mock
    private AclControllerService aclControllerService;

    private AclController aclController;

    @Before
    public void setUp() throws Exception {
        aclController = new AclController(aclControllerService);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void createAcl() {
        AclRequests aclRequest = new AclRequests();
        ResponseEntity<String> response = aclController.createAcl(aclRequest);
        assertEquals(HttpStatus.OK.value(),response.getStatusCodeValue());
    }

    @Test
    public void updateSyncAcls() {
    }

    @Test
    public void getAclRequests() {
    }

    @Test
    public void getCreatedAclRequests() {
    }

    @Test
    public void deleteAclRequests() {
    }

    @Test
    public void approveAclRequests() {
    }

    @Test
    public void getAcls() {
    }

    @Test
    public void getSyncAcls() {
    }
}