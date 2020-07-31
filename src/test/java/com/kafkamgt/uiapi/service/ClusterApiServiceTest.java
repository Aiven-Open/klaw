package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.SchemaRequest;
import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClusterApiServiceTest {

    private UtilMethods utilMethods;

    @Mock
    HandleDbRequests handleDbRequests;

    @Mock
    UtilService utilService;

    @Mock
    ManageDatabase manageDatabase;

    @Mock
    RestTemplate restTemplate;

    ClusterApiService clusterApiService;

    ResponseEntity<String> response;
    Env env;

    @Before
    public void setUp() {
        utilMethods = new UtilMethods();
        clusterApiService = new ClusterApiService(utilService);
        response = new ResponseEntity<>("success", HttpStatus.OK);

        this.env = new Env();
        env.setHost("101.10.11.11");
        env.setPort("9092");
        env.setName("DEV");
        ReflectionTestUtils.setField(clusterApiService, "manageDatabase", manageDatabase);
        when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void getStatusSuccess() {

        when(utilService.getRestTemplate()).thenReturn(restTemplate);

        when(restTemplate.exchange
                (Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(),
                        eq(String.class)))
                .thenReturn(response);
        String result = clusterApiService.getClusterApiStatus();
        assertEquals("success",result);

        result = clusterApiService.getSchemaClusterStatus("");
        assertEquals("success",result);

        result = clusterApiService.getKafkaClusterStatus("");
        assertEquals("success",result);
    }

    @Test
    public void getStatusFailure() {

        when(utilService.getRestTemplate()).thenReturn(restTemplate);

        when(restTemplate.exchange
                (Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(),
                        eq(String.class)))
                .thenThrow(new RuntimeException("error"));

        String result = clusterApiService.getClusterApiStatus();
        assertEquals("OFFLINE",result);

        result = clusterApiService.getSchemaClusterStatus("");
        assertEquals("OFFLINE",result);

        result = clusterApiService.getKafkaClusterStatus("");
        assertEquals("NOT_KNOWN",result);
    }

    @Test
    public void getAclsSuccess() throws KafkawizeException {
        Set<HashMap<String, String>> aclListOriginal = utilMethods.getAclsMock();
        when(utilService.getRestTemplate()).thenReturn(restTemplate);

        ResponseEntity<Set> response = new ResponseEntity<>(aclListOriginal, HttpStatus.OK);

        when(restTemplate.exchange
                (Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(),
                        eq(Set.class)))
                .thenReturn(response);

        List<HashMap<String, String>> result = clusterApiService.getAcls("");
        assertEquals(result, new ArrayList<>(aclListOriginal));
    }

    @Test (expected = KafkawizeException.class)
    public void getAclsFailure() throws KafkawizeException {
        when(utilService.getRestTemplate()).thenReturn(restTemplate);

        when(restTemplate.exchange
                (Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(),
                        eq(Set.class)))
                .thenThrow(new RuntimeException("error"));

        clusterApiService.getAcls("");
    }

    @Test
    public void getAllTopicsSuccess() throws Exception {
        Set<String> topicsList = getTopics();
        ResponseEntity<Set> response = new ResponseEntity<>(topicsList, HttpStatus.OK);
        when(utilService.getRestTemplate()).thenReturn(restTemplate);

        when(restTemplate.exchange
                (Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(),
                        eq(Set.class)))
                .thenReturn(response);

        List<String> result = clusterApiService.getAllTopics("");
        assertEquals(result, new ArrayList<>(topicsList));
    }

    @Test(expected = KafkawizeException.class)
    public void getAllTopicsFailure() throws Exception {
        when(utilService.getRestTemplate()).thenReturn(restTemplate);

        when(restTemplate.exchange
                (Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(),
                        eq(Set.class)))
                .thenThrow(new RuntimeException("error"));

        clusterApiService.getAllTopics("");
    }

    @Test
    public void approveTopicRequestsSuccess() throws KafkawizeException {
        String topicName = "testtopic";
        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setTopicname("testtopic");
        topicRequest.setEnvironment("DEV");

        when(utilService.getRestTemplate()).thenReturn(restTemplate);
        when(handleDbRequests.selectEnvDetails("DEV")).thenReturn(this.env);
        when(restTemplate.postForEntity
                (Mockito.anyString(), Mockito.any(),
                        eq(String.class)))
                .thenReturn(response);

        ResponseEntity<String> response = clusterApiService.approveTopicRequests(topicName, topicRequest);
        assertEquals(response.getBody(), "success");
    }

    @Test(expected = KafkawizeException.class)
    public void approveTopicRequestsFailure() throws KafkawizeException {
        String topicName = "testtopic";
        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setTopicname("testtopic");
        topicRequest.setEnvironment("DEV");

        when(utilService.getRestTemplate()).thenReturn(restTemplate);
        when(handleDbRequests.selectEnvDetails("DEV")).thenReturn(this.env);
        when(restTemplate.postForEntity
                (Mockito.anyString(), Mockito.any(),
                        eq(String.class)))
                .thenThrow(new RuntimeException("error"));

        clusterApiService.approveTopicRequests(topicName, topicRequest);
    }

    @Test
    public void approveAclRequestsSuccess1() throws KafkawizeException {
        AclRequests aclRequests = new AclRequests();
        aclRequests.setReq_no("fsd3D213");
        aclRequests.setEnvironment("DEV");
        aclRequests.setTopicname("testtopic");
        aclRequests.setAclType("Create");

        when(utilService.getRestTemplate()).thenReturn(restTemplate);
        when(handleDbRequests.selectEnvDetails("DEV")).thenReturn(this.env);
        when(restTemplate.postForEntity
                (Mockito.anyString(), Mockito.any(),
                        eq(String.class)))
                .thenReturn(response);

        ResponseEntity<String> response = clusterApiService.approveAclRequests(aclRequests);
        assertEquals(response.getBody(), "success");
    }

    @Test
    public void approveAclRequestsSuccess2() throws KafkawizeException {
        AclRequests aclRequests = new AclRequests();
        aclRequests.setReq_no("fsd3D213");
        aclRequests.setEnvironment("DEV");
        aclRequests.setTopicname("testtopic");
        aclRequests.setAclType("Delete");

        when(utilService.getRestTemplate()).thenReturn(restTemplate);
        when(handleDbRequests.selectEnvDetails("DEV")).thenReturn(this.env);
        when(restTemplate.postForEntity
                (Mockito.anyString(), Mockito.any(),
                        eq(String.class)))
                .thenReturn(response);

        ResponseEntity<String> response = clusterApiService.approveAclRequests(aclRequests);
        assertEquals(response.getBody(), "success");
    }

    @Test(expected = KafkawizeException.class)
    public void approveAclRequestsFailure() throws KafkawizeException {
        AclRequests aclRequests = new AclRequests();
        aclRequests.setReq_no("fsd3D213");
        aclRequests.setEnvironment("DEV");
        aclRequests.setTopicname("testtopic");
        aclRequests.setAclType("Create");

        clusterApiService.approveAclRequests(aclRequests);
    }

    @Test
    public void postSchemaSucess() throws KafkawizeException {
        SchemaRequest schemaRequest = new SchemaRequest();
        schemaRequest.setSchemafull("{schema}");
        String envSel = "DEV";
        String topicName = "testtopic";

        when(utilService.getRestTemplate()).thenReturn(restTemplate);
        when(handleDbRequests.selectEnvDetails("DEV")).thenReturn(this.env);
        when(restTemplate.postForEntity
                (Mockito.anyString(), Mockito.any(),
                        eq(String.class)))
                .thenReturn(response);

        ResponseEntity<String> result = clusterApiService.postSchema(schemaRequest, envSel, topicName);
        assertEquals(result.getBody(), "success");
    }

    @Test(expected = KafkawizeException.class)
    public void postSchemaFailure() throws KafkawizeException {
        SchemaRequest schemaRequest = new SchemaRequest();
        schemaRequest.setSchemafull("{schema}");
        String envSel = "DEV";
        String topicName = "testtopic";

        when(utilService.getRestTemplate()).thenReturn(restTemplate);
        when(handleDbRequests.selectEnvDetails("DEV")).thenReturn(this.env);
        when(restTemplate.postForEntity
                (Mockito.anyString(), Mockito.any(),
                        eq(String.class)))
                .thenThrow(new RuntimeException("error"));

        clusterApiService.postSchema(schemaRequest, envSel, topicName);
    }

    private Set<String> getTopics(){
        Set<String> topicsList = new HashSet<>();
        topicsList.add("topic1");
        topicsList.add("topic2");

        return topicsList;
    }



}