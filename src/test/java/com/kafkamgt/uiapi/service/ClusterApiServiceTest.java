package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClusterApiServiceTest {

    private UtilMethods utilMethods;

    @Mock
    HandleDbRequests handleDbRequests;

    @Mock
    MailUtils mailService;

    @Mock
    ManageDatabase manageDatabase;

    @Mock
    RestTemplate restTemplate;

    @Mock
    private HashMap<Integer, KwClusters> clustersHashMap;

    @Mock
    private KwClusters kwClusters;

    ClusterApiService clusterApiService;

    ResponseEntity<String> response;
    Env env;

    @BeforeEach
    public void setUp() {
        utilMethods = new UtilMethods();
        clusterApiService = new ClusterApiService();
        response = new ResponseEntity<>("success", HttpStatus.OK);

        this.env = new Env();
        env.setName("DEV");
        ReflectionTestUtils.setField(clusterApiService, "manageDatabase", manageDatabase);
        ReflectionTestUtils.setField(clusterApiService, "httpRestTemplate", restTemplate);
        when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
        when(manageDatabase.getKwPropertyValue(anyString(), anyInt())).thenReturn("http://cluster");
    }

    @Test
    @Order(1)
    public void getStatusSuccess() {

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(response);
        String result = clusterApiService.getClusterApiStatus("/topics/getApiStatus", false,1);
        assertEquals("success", result);

        result = clusterApiService.getSchemaClusterStatus("",1);
        assertEquals("success", result);

        result = clusterApiService.getKafkaClusterStatus("", "PLAINTEXT","", "",1);
        assertEquals("success", result);
    }

    @Test
    @Order(2)
    public void getStatusFailure() {

        when(restTemplate.exchange
                (Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(),
                        eq(String.class)))
                .thenThrow(new RuntimeException("error"));

        String result = clusterApiService.getClusterApiStatus("", false,1);
        assertEquals("OFFLINE",result);

        result = clusterApiService.getSchemaClusterStatus("",1);
        assertEquals("OFFLINE",result);

        result = clusterApiService.getKafkaClusterStatus("", "PLAINTEXT","", "",1);
        assertEquals("NOT_KNOWN",result);
    }

    @Test
    @Order(3)
    public void getAclsSuccess() throws KafkawizeException {
        Set<HashMap<String, String>> aclListOriginal = utilMethods.getAclsMock();
        ResponseEntity<Set> response = new ResponseEntity<>(aclListOriginal, HttpStatus.OK);

        when(restTemplate.exchange
                (Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(),
                        eq(Set.class)))
                .thenReturn(response);

        List<HashMap<String, String>> result = clusterApiService.getAcls("", "PLAINTEXT","",1);
        assertEquals(result, new ArrayList<>(aclListOriginal));
    }

    @Test
    @Order(4)
    public void getAclsFailure() {

        when(restTemplate.exchange
                (Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(),
                        eq(Set.class)))
                .thenThrow(new RuntimeException("error"));

        assertThrows(KafkawizeException.class, () -> clusterApiService.getAcls("", "PLAINTEXT","",1));
    }

    @Test
    @Order(5)
    public void getAllTopicsSuccess() throws Exception {
        Set<String> topicsList = getTopics();
        ResponseEntity<Set> response = new ResponseEntity<>(topicsList, HttpStatus.OK);

        when(restTemplate.exchange
                (Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(),
                        eq(Set.class)))
                .thenReturn(response);

        List<HashMap<String, String>> result = clusterApiService.getAllTopics("", "PLAINTEXT","",1);
        assertEquals(result, new ArrayList<>(topicsList));
    }

    @Test
    @Order(6)
    public void getAllTopicsFailure() throws Exception {

        when(restTemplate.exchange
                (Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(),
                        eq(Set.class)))
                .thenThrow(new RuntimeException("error"));

        assertThrows(KafkawizeException.class, () ->
                clusterApiService.getAllTopics("", "PLAINTEXT","",1));
    }

    @Test
    @Order(7)
    public void approveTopicRequestsSuccess() throws KafkawizeException {
        String topicName = "testtopic";
        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setTopicname("testtopic");
        topicRequest.setEnvironment("DEV");
        topicRequest.setTopictype("Create");

        when(handleDbRequests.selectEnvDetails(anyString(), anyInt())).thenReturn(this.env);
        when(manageDatabase.getClusters(anyString(), anyInt())).thenReturn(clustersHashMap);
        when(clustersHashMap.get(any())).thenReturn(kwClusters);
        when(kwClusters.getBootstrapServers()).thenReturn("clusters");
        when(kwClusters.getProtocol()).thenReturn("PLAINTEXT");
        when(kwClusters.getClusterName()).thenReturn("cluster");
        when(restTemplate.postForEntity
                (Mockito.anyString(), Mockito.any(),
                        eq(String.class)))
                .thenReturn(response);

        ResponseEntity<String> response = clusterApiService
                .approveTopicRequests(topicName, "Create", 1, "1", "",1);
        assertEquals(response.getBody(), "success");
    }

    @Test
    @Order(8)
    public void approveTopicRequestsFailure() throws KafkawizeException {
        String topicName = "testtopic";
        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setTopicname("testtopic");
        topicRequest.setEnvironment("DEV");
        topicRequest.setTopictype("Create");

        when(handleDbRequests.selectEnvDetails(anyString(), anyInt())).thenReturn(this.env);
        when(manageDatabase.getClusters(anyString(), anyInt())).thenReturn(clustersHashMap);
        when(clustersHashMap.get(any())).thenReturn(kwClusters);
        when(kwClusters.getBootstrapServers()).thenReturn("clusters");
        when(kwClusters.getProtocol()).thenReturn("PLAINTEXT");
        when(kwClusters.getClusterName()).thenReturn("cluster");
        when(restTemplate.postForEntity
                (Mockito.anyString(), Mockito.any(),
                        eq(String.class)))
                .thenThrow(new RuntimeException("error"));

        assertThrows(KafkawizeException.class, () ->
                clusterApiService.approveTopicRequests(topicName, "Create", 1, "1", "",1));
    }

    @Test
    @Order(9)
    public void approveAclRequestsSuccess1() throws KafkawizeException {
        AclRequests aclRequests = new AclRequests();
        aclRequests.setReq_no(1001);
        aclRequests.setEnvironment("DEV");
        aclRequests.setTopicname("testtopic");
        aclRequests.setAclType("Create");

        when(handleDbRequests.selectEnvDetails(anyString(), anyInt())).thenReturn(this.env);
        when(manageDatabase.getClusters(anyString(), anyInt())).thenReturn(clustersHashMap);
        when(clustersHashMap.get(any())).thenReturn(kwClusters);
        when(kwClusters.getBootstrapServers()).thenReturn("clusters");
        when(kwClusters.getProtocol()).thenReturn("PLAINTEXT");
        when(kwClusters.getClusterName()).thenReturn("cluster");
        when(restTemplate.postForEntity
                (Mockito.anyString(), Mockito.any(),
                        eq(String.class)))
                .thenReturn(response);

        ResponseEntity<String> response = clusterApiService.approveAclRequests(aclRequests,1);
        assertEquals(response.getBody(), "success");
    }

    @Test
    @Order(10)
    public void approveAclRequestsSuccess2() throws KafkawizeException {
        AclRequests aclRequests = new AclRequests();
        aclRequests.setReq_no(1001);
        aclRequests.setEnvironment("DEV");
        aclRequests.setTopicname("testtopic");
        aclRequests.setAclType("Delete");

        when(handleDbRequests.selectEnvDetails(anyString(), anyInt())).thenReturn(this.env);
        when(manageDatabase.getClusters(anyString(), anyInt())).thenReturn(clustersHashMap);
        when(clustersHashMap.get(any())).thenReturn(kwClusters);
        when(kwClusters.getBootstrapServers()).thenReturn("clusters");
        when(kwClusters.getProtocol()).thenReturn("PLAINTEXT");
        when(kwClusters.getClusterName()).thenReturn("cluster");
        when(restTemplate.postForEntity
                (Mockito.anyString(), Mockito.any(),
                        eq(String.class)))
                .thenReturn(response);

        ResponseEntity<String> response = clusterApiService.approveAclRequests(aclRequests,1);
        assertEquals(response.getBody(), "success");
    }

    @Test
    @Order(11)
    public void approveAclRequestsFailure() throws KafkawizeException {
        AclRequests aclRequests = new AclRequests();
        aclRequests.setReq_no(1001);
        aclRequests.setEnvironment("DEV");
        aclRequests.setTopicname("testtopic");
        aclRequests.setAclType("Create");

        assertThrows(KafkawizeException.class, () -> clusterApiService.approveAclRequests(aclRequests,1));
    }

    @Test
    @Order(12)
    public void postSchemaSucess() throws KafkawizeException {
        SchemaRequest schemaRequest = new SchemaRequest();
        schemaRequest.setSchemafull("{schema}");
        String envSel = "DEV";
        String topicName = "testtopic";

        when(handleDbRequests.selectEnvDetails(anyString(), anyInt())).thenReturn(this.env);
        when(manageDatabase.getClusters(anyString(), anyInt())).thenReturn(clustersHashMap);
        when(clustersHashMap.get(any())).thenReturn(kwClusters);
        when(kwClusters.getBootstrapServers()).thenReturn("clusters");
        when(kwClusters.getProtocol()).thenReturn("PLAINTEXT");
        when(kwClusters.getClusterName()).thenReturn("cluster");
        when(restTemplate.postForEntity
                (Mockito.anyString(), Mockito.any(),
                        eq(String.class)))
                .thenReturn(response);

        ResponseEntity<String> result = clusterApiService.postSchema(schemaRequest, envSel, topicName,1);
        assertEquals(result.getBody(), "success");
    }

    @Test
    @Order(13)
    public void postSchemaFailure() throws KafkawizeException {
        SchemaRequest schemaRequest = new SchemaRequest();
        schemaRequest.setSchemafull("{schema}");
        String envSel = "DEV";
        String topicName = "testtopic";

        when(handleDbRequests.selectEnvDetails("DEV", 1)).thenReturn(this.env);
        when(restTemplate.postForEntity
                (Mockito.anyString(), Mockito.any(),
                        eq(String.class)))
                .thenThrow(new RuntimeException("error"));

        assertThrows(KafkawizeException.class, () -> clusterApiService.postSchema(schemaRequest, envSel, topicName, 1));
    }

    private Set<String> getTopics(){
        Set<String> topicsList = new HashSet<>();
        topicsList.add("topic1");
        topicsList.add("topic2");

        return topicsList;
    }



}