package io.aiven.klaw.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.*;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.AclIPPrincipleType;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClusterApiServiceTest {

  private UtilMethods utilMethods;

  @Mock HandleDbRequests handleDbRequests;

  @Mock MailUtils mailService;

  @Mock ManageDatabase manageDatabase;

  @Mock RestTemplate restTemplate;

  @Mock private Map<Integer, KwClusters> clustersHashMap;

  @Mock private KwClusters kwClusters;

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
    String result = clusterApiService.getClusterApiStatus("/topics/getApiStatus", false, 1);
    assertEquals("success", result);

    result = clusterApiService.getSchemaClusterStatus("", 1);
    assertEquals("success", result);

    result = clusterApiService.getKafkaClusterStatus("", "PLAINTEXT", "", "", 1);
    assertEquals("success", result);
  }

  @Test
  @Order(2)
  public void getStatusFailure() {

    when(restTemplate.exchange(
            Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(), eq(String.class)))
        .thenThrow(new RuntimeException("error"));

    String result = clusterApiService.getClusterApiStatus("", false, 1);
    assertEquals("OFFLINE", result);

    result = clusterApiService.getSchemaClusterStatus("", 1);
    assertEquals("OFFLINE", result);

    result = clusterApiService.getKafkaClusterStatus("", "PLAINTEXT", "", "", 1);
    assertEquals("NOT_KNOWN", result);
  }

  @Test
  @Order(3)
  public void getAclsSuccess() throws KlawException {
    Set<Map<String, String>> aclListOriginal = utilMethods.getAclsMock();
    ResponseEntity<Set> response = new ResponseEntity<>(aclListOriginal, HttpStatus.OK);

    when(manageDatabase.getClusters(anyString(), anyInt())).thenReturn(clustersHashMap);
    when(clustersHashMap.get(any())).thenReturn(kwClusters);
    when(kwClusters.getKafkaFlavor()).thenReturn("Apache Kafka");

    when(restTemplate.exchange(
            Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(), eq(Set.class)))
        .thenReturn(response);

    List<Map<String, String>> result = clusterApiService.getAcls("", env, "PLAINTEXT", "", 1);
    assertEquals(result, new ArrayList<>(aclListOriginal));
  }

  @Test
  @Order(4)
  public void getAclsFailure() {

    when(restTemplate.exchange(
            Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(), eq(Set.class)))
        .thenThrow(new RuntimeException("error"));

    assertThrows(KlawException.class, () -> clusterApiService.getAcls("", env, "PLAINTEXT", "", 1));
  }

  @Test
  @Order(5)
  public void getAllTopicsSuccess() throws Exception {
    Set<String> topicsList = getTopics();
    ResponseEntity<Set> response = new ResponseEntity<>(topicsList, HttpStatus.OK);

    when(restTemplate.exchange(
            Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(), eq(Set.class)))
        .thenReturn(response);

    List<Map<String, String>> result = clusterApiService.getAllTopics("", "PLAINTEXT", "", 1);
    assertEquals(result, new ArrayList<>(topicsList));
  }

  @Test
  @Order(6)
  public void getAllTopicsFailure() throws Exception {

    when(restTemplate.exchange(
            Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(), eq(Set.class)))
        .thenThrow(new RuntimeException("error"));

    assertThrows(KlawException.class, () -> clusterApiService.getAllTopics("", "PLAINTEXT", "", 1));
  }

  @Test
  @Order(7)
  public void approveTopicRequestsSuccess() throws KlawException {
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
    when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), eq(String.class)))
        .thenReturn(response);

    ResponseEntity<String> response =
        clusterApiService.approveTopicRequests(topicName, "Create", 1, "1", "", 1);
    assertEquals(response.getBody(), "success");
  }

  @Test
  @Order(8)
  public void approveTopicRequestsFailure() throws KlawException {
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
    when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), eq(String.class)))
        .thenThrow(new RuntimeException("error"));

    assertThrows(
        KlawException.class,
        () -> clusterApiService.approveTopicRequests(topicName, "Create", 1, "1", "", 1));
  }

  @Test
  @Order(9)
  public void approveAclRequestsSuccess1() throws KlawException {
    AclRequests aclRequests = new AclRequests();
    aclRequests.setReq_no(1001);
    aclRequests.setEnvironment("DEV");
    aclRequests.setTopicname("testtopic");
    aclRequests.setAclType("Create");
    aclRequests.setAclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS);

    Map<String, String> resultMap = new HashMap<>();
    resultMap.put("result", "success");
    ResponseEntity<Map<String, String>> responseEntity =
        new ResponseEntity<>(resultMap, HttpStatus.OK);

    when(handleDbRequests.selectEnvDetails(anyString(), anyInt())).thenReturn(this.env);
    when(manageDatabase.getClusters(anyString(), anyInt())).thenReturn(clustersHashMap);
    when(clustersHashMap.get(any())).thenReturn(kwClusters);
    when(kwClusters.getBootstrapServers()).thenReturn("clusters");
    when(kwClusters.getProtocol()).thenReturn("PLAINTEXT");
    when(kwClusters.getClusterName()).thenReturn("cluster");
    when(kwClusters.getKafkaFlavor()).thenReturn("Apache Kafka");
    when(restTemplate.exchange(
            Mockito.anyString(),
            any(),
            Mockito.any(),
            (ParameterizedTypeReference<Map<String, String>>) any()))
        .thenReturn(responseEntity);

    ResponseEntity<Map<String, String>> response =
        clusterApiService.approveAclRequests(aclRequests, 1);
    assertEquals(response.getBody().get("result"), "success");
  }

  @Test
  @Order(10)
  public void approveAclRequestsSuccess2() throws KlawException {
    AclRequests aclRequests = new AclRequests();
    aclRequests.setReq_no(1001);
    aclRequests.setEnvironment("DEV");
    aclRequests.setTopicname("testtopic");
    aclRequests.setAclType("Delete");
    aclRequests.setAclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS);

    Map<String, String> resultMap = new HashMap<>();
    resultMap.put("result", "success");
    ResponseEntity<Map<String, String>> responseEntity =
        new ResponseEntity<>(resultMap, HttpStatus.OK);

    when(handleDbRequests.selectEnvDetails(anyString(), anyInt())).thenReturn(this.env);
    when(manageDatabase.getClusters(anyString(), anyInt())).thenReturn(clustersHashMap);
    when(clustersHashMap.get(any())).thenReturn(kwClusters);
    when(kwClusters.getBootstrapServers()).thenReturn("clusters");
    when(kwClusters.getProtocol()).thenReturn("PLAINTEXT");
    when(kwClusters.getClusterName()).thenReturn("cluster");
    when(kwClusters.getKafkaFlavor()).thenReturn("Apache Kafka");
    when(restTemplate.exchange(
            Mockito.anyString(),
            any(),
            Mockito.any(),
            (ParameterizedTypeReference<Map<String, String>>) any()))
        .thenReturn(responseEntity);

    ResponseEntity<Map<String, String>> response =
        clusterApiService.approveAclRequests(aclRequests, 1);
    assertEquals(response.getBody().get("result"), "success");
  }

  @Test
  @Order(11)
  public void approveAclRequestsFailure() throws KlawException {
    AclRequests aclRequests = new AclRequests();
    aclRequests.setReq_no(1001);
    aclRequests.setEnvironment("DEV");
    aclRequests.setTopicname("testtopic");
    aclRequests.setAclType("Create");

    assertThrows(KlawException.class, () -> clusterApiService.approveAclRequests(aclRequests, 1));
  }

  @Test
  @Order(12)
  public void postSchemaSucess() throws KlawException {
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
    when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), eq(String.class)))
        .thenReturn(response);

    ResponseEntity<String> result =
        clusterApiService.postSchema(schemaRequest, envSel, topicName, 1);
    assertEquals(result.getBody(), "success");
  }

  @Test
  @Order(13)
  public void postSchemaFailure() throws KlawException {
    SchemaRequest schemaRequest = new SchemaRequest();
    schemaRequest.setSchemafull("{schema}");
    String envSel = "DEV";
    String topicName = "testtopic";

    when(handleDbRequests.selectEnvDetails("DEV", 1)).thenReturn(this.env);
    when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), eq(String.class)))
        .thenThrow(new RuntimeException("error"));

    assertThrows(
        KlawException.class,
        () -> clusterApiService.postSchema(schemaRequest, envSel, topicName, 1));
  }

  private Set<String> getTopics() {
    Set<String> topicsList = new HashSet<>();
    topicsList.add("topic1");
    topicsList.add("topic2");

    return topicsList;
  }
}
