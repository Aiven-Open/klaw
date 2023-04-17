package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.SchemaRequest;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.cluster.ClusterSchemaRequest;
import io.aiven.klaw.model.enums.AclIPPrincipleType;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.ClusterStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.KafkaSupportedProtocol;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.response.TopicConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@Slf4j
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClusterApiServiceTest {

  public static final String SCHEMAFULL = "{schema}";
  public static final String BOOTSRAP_SERVERS = "clusters";
  private UtilMethods utilMethods;

  @Mock HandleDbRequestsJdbc handleDbRequests;

  @Mock ManageDatabase manageDatabase;

  @Mock RestTemplate restTemplate;

  @Mock private Map<Integer, KwClusters> clustersHashMap;

  @Mock private KwClusters kwClusters;

  @Captor private ArgumentCaptor<HttpEntity<ClusterSchemaRequest>> clusterSchemaRequestCaptor;
  ClusterApiService clusterApiService;

  ResponseEntity<String> response;
  Env env;

  @BeforeEach
  public void setUp() {
    utilMethods = new UtilMethods();
    clusterApiService = new ClusterApiService(manageDatabase);
    response = new ResponseEntity<>(ApiResultStatus.SUCCESS.value, HttpStatus.OK);

    this.env = new Env();
    env.setName("DEV");
    env.setClusterId(1);
    ReflectionTestUtils.setField(clusterApiService, "httpRestTemplate", restTemplate);
    ReflectionTestUtils.setField(clusterApiService, "clusterApiUser", "testuser");
    ReflectionTestUtils.setField(
        clusterApiService,
        "clusterApiAccessBase64Secret",
        "dGhpcyBpcyBhIHNlY3JldCB0byBhY2Nlc3MgY2x1c3RlcmFwaQ=="); // any base64 string

    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    when(manageDatabase.getKwPropertyValue(anyString(), anyInt())).thenReturn("http://cluster");
  }

  @Test
  @Order(1)
  public void getStatusSuccess() {

    ResponseEntity<ClusterStatus> response =
        new ResponseEntity<>(ClusterStatus.ONLINE, HttpStatus.OK);
    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(ClusterStatus.class)))
        .thenReturn(response);
    String result = clusterApiService.getClusterApiStatus("/topics/getApiStatus", false, 1);
    assertThat(result).isEqualTo(ClusterStatus.ONLINE.value);

    result =
        clusterApiService.getKafkaClusterStatus(
            "", KafkaSupportedProtocol.PLAINTEXT, "", "", "", 1);
    assertThat(result).isEqualTo(ClusterStatus.ONLINE.value);
  }

  @Test
  @Order(2)
  public void getStatusFailure() {

    when(restTemplate.exchange(
            Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(), eq(String.class)))
        .thenThrow(new RuntimeException("error"));

    String result = clusterApiService.getClusterApiStatus("", false, 1);
    assertThat(result).isEqualTo("OFFLINE");

    result =
        clusterApiService.getKafkaClusterStatus(
            "", KafkaSupportedProtocol.PLAINTEXT, "", "", "", 1);
    assertThat(result).isEqualTo("NOT_KNOWN");
  }

  @Test
  @Order(3)
  public void getAclsSuccess() throws KlawException {
    Set<Map<String, String>> aclListOriginal = utilMethods.getAclsMock();
    ResponseEntity response = new ResponseEntity<>(aclListOriginal, HttpStatus.OK);

    when(manageDatabase.getClusters(any(KafkaClustersType.class), anyInt()))
        .thenReturn(clustersHashMap);
    when(clustersHashMap.get(any())).thenReturn(kwClusters);
    when(kwClusters.getKafkaFlavor()).thenReturn("Apache Kafka");

    when(restTemplate.exchange(
            Mockito.anyString(),
            eq(HttpMethod.GET),
            Mockito.any(),
            (ParameterizedTypeReference<Object>) any()))
        .thenReturn(response);

    List<Map<String, String>> result =
        clusterApiService.getAcls("", env, KafkaSupportedProtocol.PLAINTEXT, 1);
    assertThat(result).isEqualTo(new ArrayList<>(aclListOriginal));
  }

  @Test
  @Order(4)
  public void getAclsFailure() {

    when(restTemplate.exchange(
            Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(), eq(Set.class)))
        .thenThrow(new RuntimeException("error"));

    assertThatThrownBy(
            () -> clusterApiService.getAcls("", env, KafkaSupportedProtocol.PLAINTEXT, 1))
        .isInstanceOf(KlawException.class);
  }

  @Test
  @Order(5)
  public void getAllTopicsSuccess() throws Exception {
    Set<String> topicsList = getTopics();
    ResponseEntity response = new ResponseEntity<>(topicsList, HttpStatus.OK);

    when(restTemplate.exchange(
            Mockito.anyString(),
            eq(HttpMethod.GET),
            Mockito.any(),
            (ParameterizedTypeReference<Object>) any()))
        .thenReturn(response);

    List<TopicConfig> result =
        clusterApiService.getAllTopics("", KafkaSupportedProtocol.PLAINTEXT, "", "", 1);
    assertThat(result).isEqualTo(new ArrayList<>(topicsList));
  }

  @Test
  @Order(6)
  public void getAllTopicsFailure() throws Exception {

    when(restTemplate.exchange(
            Mockito.anyString(), eq(HttpMethod.GET), Mockito.any(), eq(Set.class)))
        .thenThrow(new RuntimeException("error"));

    assertThatThrownBy(
            () -> clusterApiService.getAllTopics("", KafkaSupportedProtocol.PLAINTEXT, "", "", 1))
        .isInstanceOf(KlawException.class);
  }

  @Test
  @Order(7)
  public void approveTopicRequestsSuccess() throws KlawException {
    ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build();
    ResponseEntity<ApiResponse> response =
        new ResponseEntity<>(
            ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build(), HttpStatus.OK);

    String topicName = "testtopic";
    TopicRequest topicRequest = new TopicRequest();
    topicRequest.setTopicname("testtopic");
    topicRequest.setEnvironment("DEV");
    topicRequest.setRequestOperationType(RequestOperationType.CREATE.value);

    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(this.env);
    when(manageDatabase.getClusters(any(KafkaClustersType.class), anyInt()))
        .thenReturn(clustersHashMap);
    when(clustersHashMap.get(any())).thenReturn(kwClusters);
    when(kwClusters.getBootstrapServers()).thenReturn(BOOTSRAP_SERVERS);
    when(kwClusters.getProtocol()).thenReturn(KafkaSupportedProtocol.PLAINTEXT);
    when(kwClusters.getClusterName()).thenReturn("cluster");
    when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), eq(ApiResponse.class)))
        .thenReturn(response);

    ResponseEntity<ApiResponse> response1 =
        clusterApiService.approveTopicRequests(
            topicName, RequestOperationType.CREATE.value, 1, "1", "", null, 1, false);
    assertThat(Objects.requireNonNull(response1.getBody()).getMessage())
        .isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(8)
  public void approveTopicRequestsFailure() {
    String topicName = "testtopic";
    TopicRequest topicRequest = new TopicRequest();
    topicRequest.setTopicname("testtopic");
    topicRequest.setEnvironment("DEV");
    topicRequest.setRequestOperationType(RequestOperationType.CREATE.value);

    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(this.env);
    when(manageDatabase.getClusters(any(KafkaClustersType.class), anyInt()))
        .thenReturn(clustersHashMap);
    when(clustersHashMap.get(any())).thenReturn(kwClusters);
    when(kwClusters.getBootstrapServers()).thenReturn(BOOTSRAP_SERVERS);
    when(kwClusters.getProtocol()).thenReturn(KafkaSupportedProtocol.PLAINTEXT);
    when(kwClusters.getClusterName()).thenReturn("cluster");
    when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), eq(ApiResponse.class)))
        .thenThrow(new RuntimeException("error"));

    assertThatThrownBy(
            () ->
                clusterApiService.approveTopicRequests(
                    topicName, RequestOperationType.CREATE.value, 1, "1", "", null, 1, false))
        .isInstanceOf(KlawException.class);
  }

  @Test
  @Order(9)
  public void approveAclRequestsSuccess1() throws KlawException {
    AclRequests aclRequests = new AclRequests();
    aclRequests.setReq_no(1001);
    aclRequests.setEnvironment("DEV");
    aclRequests.setTopicname("testtopic");
    aclRequests.setRequestOperationType(RequestOperationType.CREATE.value);
    aclRequests.setAclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS);

    ApiResponse apiResponse = ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build();
    ResponseEntity<ApiResponse> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(this.env);
    when(manageDatabase.getClusters(any(KafkaClustersType.class), anyInt()))
        .thenReturn(clustersHashMap);
    when(clustersHashMap.get(any())).thenReturn(kwClusters);
    when(kwClusters.getBootstrapServers()).thenReturn(BOOTSRAP_SERVERS);
    when(kwClusters.getProtocol()).thenReturn(KafkaSupportedProtocol.PLAINTEXT);
    when(kwClusters.getClusterName()).thenReturn("cluster");
    when(kwClusters.getKafkaFlavor()).thenReturn("Apache Kafka");
    when(restTemplate.exchange(
            Mockito.anyString(),
            any(),
            Mockito.any(),
            (ParameterizedTypeReference<ApiResponse>) any()))
        .thenReturn(responseEntity);

    ResponseEntity<ApiResponse> response = clusterApiService.approveAclRequests(aclRequests, 1);
    assertThat(response.getBody().getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(10)
  public void approveAclRequestsSuccess2() throws KlawException {
    AclRequests aclRequests = new AclRequests();
    aclRequests.setReq_no(1001);
    aclRequests.setEnvironment("DEV");
    aclRequests.setTopicname("testtopic");
    aclRequests.setRequestOperationType(RequestOperationType.DELETE.value);
    aclRequests.setAclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS);

    ApiResponse apiResponse = ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build();
    ResponseEntity<ApiResponse> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(this.env);
    when(manageDatabase.getClusters(any(KafkaClustersType.class), anyInt()))
        .thenReturn(clustersHashMap);
    when(clustersHashMap.get(any())).thenReturn(kwClusters);
    when(kwClusters.getBootstrapServers()).thenReturn(BOOTSRAP_SERVERS);
    when(kwClusters.getProtocol()).thenReturn(KafkaSupportedProtocol.PLAINTEXT);
    when(kwClusters.getClusterName()).thenReturn("cluster");
    when(kwClusters.getKafkaFlavor()).thenReturn("Apache Kafka");
    when(restTemplate.exchange(
            Mockito.anyString(),
            any(),
            Mockito.any(),
            (ParameterizedTypeReference<ApiResponse>) any()))
        .thenReturn(responseEntity);

    ResponseEntity<ApiResponse> response = clusterApiService.approveAclRequests(aclRequests, 1);
    assertThat(response.getBody().getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(11)
  public void approveAclRequestsFailure() throws KlawException {
    AclRequests aclRequests = new AclRequests();
    aclRequests.setReq_no(1001);
    aclRequests.setEnvironment("DEV");
    aclRequests.setTopicname("testtopic");
    aclRequests.setRequestOperationType(RequestOperationType.CREATE.value);

    assertThatThrownBy(() -> clusterApiService.approveAclRequests(aclRequests, 1))
        .isInstanceOf(KlawException.class);
  }

  @Test
  @Order(12)
  public void postSchemaSucess() throws KlawException {
    SchemaRequest schemaRequest = new SchemaRequest();
    schemaRequest.setSchemafull(SCHEMAFULL);
    String envSel = "DEV";
    String topicName = "testtopic";

    ApiResponse apiResponse = ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build();
    ResponseEntity<ApiResponse> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);

    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(this.env);
    when(manageDatabase.getClusters(any(KafkaClustersType.class), anyInt()))
        .thenReturn(clustersHashMap);
    when(clustersHashMap.get(any())).thenReturn(kwClusters);
    when(kwClusters.getBootstrapServers()).thenReturn(BOOTSRAP_SERVERS);
    when(kwClusters.getProtocol()).thenReturn(KafkaSupportedProtocol.PLAINTEXT);
    when(kwClusters.getClusterName()).thenReturn("cluster");
    when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), eq(ApiResponse.class)))
        .thenReturn(response);

    ResponseEntity<ApiResponse> result =
        clusterApiService.postSchema(schemaRequest, envSel, topicName, 1);
    assertThat(result.getBody().getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @ParameterizedTest
  @ValueSource(strings = {"null", "false"})
  @Order(12)
  public void postSchemaSucessForceRegisterNullOrFalse(String value) throws KlawException {
    SchemaRequest schemaRequest = new SchemaRequest();
    schemaRequest.setSchemafull(SCHEMAFULL);
    schemaRequest.setForceRegister(value.equals("false") ? Boolean.valueOf(value) : null);
    String envSel = "DEV";
    String topicName = "testtopic";

    ApiResponse apiResponse = ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build();
    ResponseEntity<ApiResponse> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);

    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(this.env);
    when(manageDatabase.getClusters(any(KafkaClustersType.class), anyInt()))
        .thenReturn(clustersHashMap);
    when(clustersHashMap.get(anyInt())).thenReturn(kwClusters);
    when(kwClusters.getBootstrapServers()).thenReturn(BOOTSRAP_SERVERS);
    when(kwClusters.getProtocol()).thenReturn(KafkaSupportedProtocol.PLAINTEXT);
    when(kwClusters.getClusterName()).thenReturn("cluster");
    when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), eq(ApiResponse.class)))
        .thenReturn(response);

    ResponseEntity<ApiResponse> result =
        clusterApiService.postSchema(schemaRequest, envSel, topicName, 1);
    verify(restTemplate, times(1))
        .postForEntity(anyString(), clusterSchemaRequestCaptor.capture(), eq(ApiResponse.class));
    assertThat(result.getBody().getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
    ClusterSchemaRequest clusterSchemaRequest = clusterSchemaRequestCaptor.getValue().getBody();
    assertThat(clusterSchemaRequest.isForceRegister()).isFalse();
    assertThat(clusterSchemaRequest.getFullSchema()).isEqualTo(SCHEMAFULL);
  }

  @Test
  @Order(12)
  public void postSchemaSucessForceRegisterTrue() throws KlawException {
    SchemaRequest schemaRequest = new SchemaRequest();
    schemaRequest.setSchemafull(SCHEMAFULL);
    schemaRequest.setForceRegister(true);
    String envSel = "DEV";
    String topicName = "testtopic";

    ApiResponse apiResponse = ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build();
    ResponseEntity<ApiResponse> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);

    when(handleDbRequests.getEnvDetails(anyString(), anyInt())).thenReturn(this.env);
    when(manageDatabase.getClusters(any(KafkaClustersType.class), anyInt()))
        .thenReturn(clustersHashMap);
    when(clustersHashMap.get(anyInt())).thenReturn(kwClusters);
    when(kwClusters.getBootstrapServers()).thenReturn(BOOTSRAP_SERVERS);
    when(kwClusters.getProtocol()).thenReturn(KafkaSupportedProtocol.PLAINTEXT);
    when(kwClusters.getClusterName()).thenReturn("cluster");
    when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), eq(ApiResponse.class)))
        .thenReturn(response);

    ResponseEntity<ApiResponse> result =
        clusterApiService.postSchema(schemaRequest, envSel, topicName, 1);

    verify(restTemplate, times(1))
        .postForEntity(anyString(), clusterSchemaRequestCaptor.capture(), eq(ApiResponse.class));
    assertThat(result.getBody().getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
    ClusterSchemaRequest clusterSchemaRequest = clusterSchemaRequestCaptor.getValue().getBody();
    assertThat(clusterSchemaRequest.isForceRegister()).isTrue();
    assertThat(clusterSchemaRequest.getFullSchema()).isEqualTo(SCHEMAFULL);
  }

  @Test
  @Order(13)
  public void postSchemaFailure() throws KlawException {
    SchemaRequest schemaRequest = new SchemaRequest();
    schemaRequest.setSchemafull(SCHEMAFULL);
    String envSel = "DEV";
    String topicName = "testtopic";

    when(handleDbRequests.getEnvDetails("DEV", 1)).thenReturn(this.env);
    when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), eq(String.class)))
        .thenThrow(new RuntimeException("error"));

    assertThatThrownBy(() -> clusterApiService.postSchema(schemaRequest, envSel, topicName, 1))
        .isInstanceOf(KlawException.class);
  }

  private Set<String> getTopics() {
    Set<String> topicsList = new HashSet<>();
    topicsList.add("topic1");
    topicsList.add("topic2");

    return topicsList;
  }
}
