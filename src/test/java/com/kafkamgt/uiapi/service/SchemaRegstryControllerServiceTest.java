package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.SchemaRequest;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SchemaRegstryControllerServiceTest {

    @Mock
    HandleDbRequests handleDbRequests;

    @Mock
    private UtilService utilService;

    @Mock
    ManageDatabase manageDatabase;

    @Mock
    ClusterApiService clusterApiService;

    SchemaRegstryControllerService schemaRegstryControllerService;

    @Before
    public void setUp() throws Exception {
        schemaRegstryControllerService = new SchemaRegstryControllerService(clusterApiService, utilService);
        ReflectionTestUtils.setField(schemaRegstryControllerService, "manageDatabase", manageDatabase);
        when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    }

    @Test
    public void getSchemaRequests() {
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.getAllSchemaRequests(anyString())).thenReturn(getSchemasReqs());

        List<SchemaRequest> listReqs = schemaRegstryControllerService.getSchemaRequests();
        assertEquals(listReqs.size(), 2);
    }

    @Test
    public void getCreatedSchemaRequests() {
        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.getCreatedSchemaRequests(anyString())).thenReturn(getSchemasReqs());

        List<SchemaRequest> listReqs = schemaRegstryControllerService.getCreatedSchemaRequests();
        assertEquals(listReqs.size(), 2);
    }

    @Test
    public void deleteSchemaRequestsSuccess() {
        String topicName = "testtopic";
        String version = "1.0";
        String envSel = "DEV";

        String input = topicName +"-----"+version+"-----"+envSel;

        when(handleDbRequests.deleteSchemaRequest(topicName, version, envSel))
                .thenReturn("success");
        String result = schemaRegstryControllerService.deleteSchemaRequests(input);
        assertEquals(result,"success");
    }

    @Test
    public void deleteSchemaRequestsFailure() {
        String topicName = "testtopic";
        String version = "1.0";
        String envSel = "DEV";

        String input = topicName +"-----"+version+"-----"+envSel;

        when(handleDbRequests.deleteSchemaRequest(topicName, version, envSel))
                .thenThrow(new RuntimeException("Error"));
        String result = schemaRegstryControllerService.deleteSchemaRequests(input);
        assertThat(result, CoreMatchers.containsString("failure"));
    }

    @Test
    public void execSchemaRequestsSuccess() throws KafkawizeException {
        String topicName = "testtopic";
        String version = "1.0";
        String envSel = "DEV";

        ResponseEntity<String> response = new ResponseEntity<>("Schema registered id\": 215",HttpStatus.OK);
        SchemaRequest schemaRequest = new SchemaRequest();
        schemaRequest.setSchemafull("schema..");

        String input = topicName +"-----"+version+"-----"+envSel;

        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.selectSchemaRequest(topicName, version, envSel))
                .thenReturn(schemaRequest);
        when(clusterApiService.postSchema(schemaRequest, envSel, topicName))
                .thenReturn(response);
        when(handleDbRequests.updateSchemaRequest(schemaRequest, "uiuser1"))
                .thenReturn("success");

        String result = schemaRegstryControllerService.execSchemaRequests(input, envSel);
        assertEquals(result,"success");
    }

    @Test
    public void execSchemaRequestsFailure1() throws KafkawizeException {
        String topicName = "testtopic";
        String version = "1.0";
        String envSel = "DEV";

        ResponseEntity<String> response = new ResponseEntity<>("Schema not registered",HttpStatus.OK);
        SchemaRequest schemaRequest = new SchemaRequest();
        schemaRequest.setSchemafull("schema..");

        String input = topicName +"-----"+version+"-----"+envSel;

        when(handleDbRequests.selectSchemaRequest(topicName, version, envSel))
                .thenReturn(schemaRequest);
        when(clusterApiService.postSchema(schemaRequest, envSel, topicName))
                .thenReturn(response);

        String result = schemaRegstryControllerService.execSchemaRequests(input, envSel);
        assertThat(result, CoreMatchers.containsString("Failure"));
    }

    @Test
    public void execSchemaRequestsFailure2() throws KafkawizeException {
        String topicName = "testtopic";
        String version = "1.0";
        String envSel = "DEV";

        ResponseEntity<String> response = new ResponseEntity<>("Schema registered id\": 215",HttpStatus.OK);
        SchemaRequest schemaRequest = new SchemaRequest();
        schemaRequest.setSchemafull("schema..");

        String input = topicName +"-----"+version+"-----"+envSel;

        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.selectSchemaRequest(topicName, version, envSel))
                .thenReturn(schemaRequest);
        when(clusterApiService.postSchema(schemaRequest, envSel, topicName))
                .thenReturn(response);
        when(handleDbRequests.updateSchemaRequest(schemaRequest, "uiuser1"))
                .thenThrow(new RuntimeException("Error"));

        String result = schemaRegstryControllerService.execSchemaRequests(input, envSel);
        assertThat(result, CoreMatchers.containsString("failure"));
    }

    @Test
    public void uploadSchemaSuccess() {
        SchemaRequest schemaRequest = new SchemaRequest();
        schemaRequest.setSchemafull("");

        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.requestForSchema(schemaRequest)).thenReturn("success");

        String result = schemaRegstryControllerService.uploadSchema(schemaRequest);
        assertEquals(result,"success");
    }

    @Test
    public void uploadSchemaFailure() {
        SchemaRequest schemaRequest = new SchemaRequest();
        schemaRequest.setSchemafull("");

        when(utilService.getUserName()).thenReturn("uiuser1");
        when(handleDbRequests.requestForSchema(schemaRequest)).thenThrow(new RuntimeException("Error"));

        String result = schemaRegstryControllerService.uploadSchema(schemaRequest);
        assertThat(result, CoreMatchers.containsString("failure"));
    }

    private List<SchemaRequest> getSchemasReqs(){
        List<SchemaRequest> schList = new ArrayList<>();
        SchemaRequest schReq = new SchemaRequest();
        schReq.setSchemafull("<Schema>");
        schList.add(schReq);

        schReq = new SchemaRequest();
        schReq.setSchemafull("<Schema1>");
        schList.add(schReq);

        return schList;

    }
}