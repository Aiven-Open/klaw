package com.kafkamgt.uiapi.controller;

import com.kafkamgt.uiapi.dao.SchemaRequest;
import com.kafkamgt.uiapi.service.SchemaRegstryControllerService;
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
public class SchemaRegstryControllerTest {

    @Mock
    private SchemaRegstryControllerService schemaRegstryControllerService;

    private SchemaRegstryController schemaRegstryController;

    @Before
    public void setUp() throws Exception {
        schemaRegstryController = new SchemaRegstryController(schemaRegstryControllerService);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getSchemaRequests() {
    }

    @Test
    public void getCreatedSchemaRequests() {
    }

    @Test
    public void deleteSchemaRequests() {
    }

    @Test
    public void execSchemaRequests() {
    }

    @Test
    public void uploadSchema() {
        SchemaRequest schemaRequest = new SchemaRequest();
        ResponseEntity<String> response = schemaRegstryController.uploadSchema(schemaRequest);
        assertEquals(HttpStatus.OK.value(),response.getStatusCodeValue());
    }
}