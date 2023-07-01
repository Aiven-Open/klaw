package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.aiven.klaw.clusterapi.UtilMethods;
import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.TopicConfig;
import io.aiven.klaw.clusterapi.models.confluentcloud.ListAclsResponse;
import io.aiven.klaw.clusterapi.models.confluentcloud.ListTopicsResponse;
import io.aiven.klaw.clusterapi.models.confluentcloud.TopicCreateRequest;
import io.aiven.klaw.clusterapi.models.enums.*;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
public class ConfluentCloudApiServiceTest {
  public static final String KAFKA_V_3_CLUSTERS_CLUSTER_ID_TOPICS =
      "/kafka/v3/clusters/cluster-id/topics";
  public static final String KAFKA_V_3_CLUSTERS_CLUSTER_ID_ACLS =
      "/kafka/v3/clusters/cluster-id/acls";
  public static final String CLUSTER_ID = "CC_DEV1";
  ConfluentCloudApiService confluentCloudApiService;
  @Mock RestTemplate restTemplate;
  @Mock ClusterApiUtils clusterApiUtils;
  @Mock private Environment env;
  private UtilMethods utilMethods;

  @BeforeEach
  public void setUp() {
    confluentCloudApiService = new ConfluentCloudApiService(env, clusterApiUtils);
    utilMethods = new UtilMethods();
  }

  @Test
  public void listTopics() throws Exception {
    ListTopicsResponse listTopicsResponse = utilMethods.getConfluentCloudListTopicsResponse();
    ResponseEntity<ListTopicsResponse> listTopicsResponseResponseEntity =
        new ResponseEntity<>(listTopicsResponse, HttpStatus.OK);
    stubTopics();
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            (ParameterizedTypeReference<ListTopicsResponse>) any()))
        .thenReturn(listTopicsResponseResponseEntity);
    Set<TopicConfig> listTopicsSet =
        confluentCloudApiService.listTopics(
            "localhost:443", KafkaSupportedProtocol.SSL, CLUSTER_ID);

    assertThat(listTopicsSet).hasSize(2); // two topics
    assertThat(listTopicsSet.stream().toList().get(0).getTopicName()).isNotNull();
    assertThat(listTopicsSet.stream().toList().get(0).getPartitions()).isNotNull();
    assertThat(listTopicsSet.stream().toList().get(0).getReplicationFactor()).isNotNull();
  }

  @Test
  public void listTopicsConnectivityFailure() {
    stubTopics();
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            (ParameterizedTypeReference<ListTopicsResponse>) any()))
        .thenThrow(new RestClientException("Cannot connect to confluent cluster"));
    assertThatThrownBy(
            () -> {
              confluentCloudApiService.listTopics(
                  "localhost:443", KafkaSupportedProtocol.SSL, CLUSTER_ID);
            })
        .hasMessage("Error in listing topics : " + "Cannot connect to confluent cluster");
  }

  @Test
  public void listAcls() throws Exception {
    ListAclsResponse listAclsResponse = utilMethods.getConfluentCloudListAclsResponse();
    ResponseEntity<ListAclsResponse> listAclsResponseResponseEntity =
        new ResponseEntity<>(listAclsResponse, HttpStatus.OK);

    stubAcls();
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            (ParameterizedTypeReference<ListAclsResponse>) any()))
        .thenReturn(listAclsResponseResponseEntity);

    Set<Map<String, String>> aclsSet =
        confluentCloudApiService.listAcls("localhost:443", KafkaSupportedProtocol.SSL, CLUSTER_ID);

    assertThat(aclsSet).hasSize(2); // two acls
    assertThat(aclsSet.stream().toList().get(0))
        .hasSize(6); // operation, resourceType, resourceName, principle, host, permissionType
    assertThat(aclsSet.stream().toList().get(0))
        .containsKeys(
            "operation", "resourceType", "resourceName", "principle", "host", "permissionType");
    assertThat(aclsSet.stream().toList().get(0)).containsEntry("resourceName", "testtopic");
    assertThat(aclsSet.stream().toList().get(0)).containsEntry("permissionType", "ALLOW");
    assertThat(aclsSet.stream().toList().get(0)).containsEntry("resourceType", "TOPIC");
    assertThat(aclsSet.stream().toList().get(0)).containsEntry("host", "12.12.43.123");
  }

  @Test
  public void listAclsConnectivityFailure() {
    stubAcls();
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            (ParameterizedTypeReference<ListAclsResponse>) any()))
        .thenThrow(new RestClientException("Cannot connect to confluent cluster"));

    assertThatThrownBy(
            () -> {
              confluentCloudApiService.listAcls(
                  "localhost:443", KafkaSupportedProtocol.SSL, CLUSTER_ID);
            })
        .hasMessage("Error in listing acls : " + "Cannot connect to confluent cluster");
  }

  @Test
  public void createAclsProducer() throws Exception {
    stubAcls();
    ClusterAclRequest clusterAclRequest = getClusterAclRequest(AclType.PRODUCER.value);
    Map<String, String> createAclsResponse = confluentCloudApiService.createAcls(clusterAclRequest);

    when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(null);
    assertThat(createAclsResponse.get("result")).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void createAclsConsumer() throws Exception {
    stubAcls();
    ClusterAclRequest clusterAclRequest = utilMethods.getConfluentCloudConsumerAclRequest();
    Map<String, String> createAclsResponse = confluentCloudApiService.createAcls(clusterAclRequest);

    when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(null);
    assertThat(createAclsResponse.get("result")).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void updateAclMapConsumer() {
    ClusterAclRequest clusterAclRequest = utilMethods.getConfluentCloudConsumerAclRequest();
    Map<String, String> aclMap = new HashMap<>();
    Map<String, String> resultMap =
        confluentCloudApiService.updateAclMap(clusterAclRequest, aclMap);
    assertThat(resultMap.get("pattern_type")).isEqualTo("LITERAL");
    assertThat(resultMap.get("principal")).isEqualTo("User:CN=host");
    assertThat(resultMap.get("host")).isEqualTo("*");
    assertThat(resultMap.get("permission")).isEqualTo("ALLOW");
  }

  @Test
  public void updateAclMapProducer() {
    ClusterAclRequest clusterAclRequest = getClusterAclRequest(AclType.PRODUCER.value);
    Map<String, String> aclMap = new HashMap<>();
    Map<String, String> resultMap =
        confluentCloudApiService.updateAclMap(clusterAclRequest, aclMap);
    assertThat(resultMap.get("pattern_type")).isEqualTo("LITERAL");
    assertThat(resultMap.get("principal")).isEqualTo("User:*");
    assertThat(resultMap.get("host")).isEqualTo("11.12.33.122");
    assertThat(resultMap.get("permission")).isEqualTo("ALLOW");
  }

  @Test
  public void updateAclMapProducerPrefixed() {
    ClusterAclRequest clusterAclRequest = utilMethods.getConfluentCloudProducerPrefixedAclRequest();
    Map<String, String> aclMap = new HashMap<>();
    Map<String, String> resultMap =
        confluentCloudApiService.updateAclMap(clusterAclRequest, aclMap);
    assertThat(resultMap.get("pattern_type")).isEqualTo("PREFIXED");
    assertThat(resultMap.get("principal")).isEqualTo("User:*");
    assertThat(resultMap.get("host")).isEqualTo("11.12.33.122");
    assertThat(resultMap.get("permission")).isEqualTo("ALLOW");
  }

  @Test
  public void deleteAcls_AclTypeProducer() throws Exception {
    ClusterAclRequest clusterAclRequest = getClusterAclRequest(AclType.PRODUCER.value);
    stubAcls();
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.DELETE),
            any(),
            (ParameterizedTypeReference<ListAclsResponse>) any()))
        .thenReturn(null);

    String response = confluentCloudApiService.deleteAcls(clusterAclRequest);
    assertThat(response).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void deleteAcls_AclTypeConsumer() throws Exception {
    ClusterAclRequest clusterAclRequest = getClusterAclRequest(AclType.CONSUMER.value);
    stubAcls();
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.DELETE),
            any(),
            (ParameterizedTypeReference<ListAclsResponse>) any()))
        .thenReturn(null);

    String response = confluentCloudApiService.deleteAcls(clusterAclRequest);
    assertThat(response).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void createTopic() throws Exception {
    stubTopics();
    ClusterTopicRequest clusterTopicRequest = utilMethods.getTopicRequest();
    when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(null);

    ApiResponse apiResponse = confluentCloudApiService.createTopic(clusterTopicRequest);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void createTopic_AlredyExistsFailure() throws Exception {
    ClusterTopicRequest clusterTopicRequest = utilMethods.getTopicRequest();

    stubTopics();
    when(restTemplate.postForEntity(anyString(), any(), any()))
        .thenThrow(new RestClientException("Topic already exists."));

    ApiResponse apiResponse = confluentCloudApiService.createTopic(clusterTopicRequest);

    assertThat(apiResponse.getMessage()).isEqualTo("Topic already exists.");
    assertThat(apiResponse.isSuccess()).isFalse();
  }

  @Test
  public void createTopic_Failure() {
    ClusterTopicRequest clusterTopicRequest = utilMethods.getTopicRequest();
    Exception exception = new RestClientException("Error occurred.");

    stubTopics();
    when(restTemplate.postForEntity(anyString(), any(), any())).thenThrow(exception);

    assertThatThrownBy(() -> confluentCloudApiService.createTopic(clusterTopicRequest))
        .isEqualTo(exception);
  }

  @Test
  public void deleteTopic() throws Exception {
    stubTopics();
    ClusterTopicRequest clusterTopicRequest = utilMethods.getTopicRequest();
    when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(null);

    ApiResponse apiResponse = confluentCloudApiService.deleteTopic(clusterTopicRequest);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void deleteTopic_AlredyExistsFailure() throws Exception {
    ClusterTopicRequest clusterTopicRequest = utilMethods.getTopicRequest();

    stubTopics();
    when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(String.class)))
        .thenThrow(new RestClientException("This server does not host this topic"));

    ApiResponse apiResponse = confluentCloudApiService.deleteTopic(clusterTopicRequest);

    assertThat(apiResponse.getMessage()).isEqualTo("This server does not host this topic");
    assertThat(apiResponse.isSuccess()).isFalse();
  }

  @Test
  public void deleteTopic_Failure() {
    ClusterTopicRequest clusterTopicRequest = utilMethods.getTopicRequest();
    Exception exception = new RestClientException("Error occurred.");

    stubTopics();
    when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(String.class)))
        .thenThrow(exception);

    assertThatThrownBy(() -> confluentCloudApiService.deleteTopic(clusterTopicRequest))
        .isEqualTo(exception);
  }

  @Test
  public void getTopicCreateObj() {
    ClusterTopicRequest clusterTopicRequest = utilMethods.getTopicRequest();
    TopicCreateRequest topicCreateRequest =
        confluentCloudApiService.getTopicCreateObj(clusterTopicRequest);
    assertThat(topicCreateRequest.getTopic_name()).isEqualTo(clusterTopicRequest.getTopicName());
    assertThat(topicCreateRequest.getPartitions_count())
        .isEqualTo(clusterTopicRequest.getPartitions());
    assertThat(topicCreateRequest.getReplication_factor())
        .isEqualTo(clusterTopicRequest.getReplicationFactor());
    assertThat(topicCreateRequest.getConfigs())
        .hasSize(clusterTopicRequest.getAdvancedTopicConfiguration().size());
  }

  @Test
  public void getQueryParamsHost() {
    ClusterAclRequest clusterAclRequest = getClusterAclRequest(AclType.PRODUCER.value);
    String resourceType = "TOPIC";
    String operation = "WRITE";
    String response =
        confluentCloudApiService.updateQueryParams(
            clusterAclRequest,
            resourceType,
            clusterAclRequest.getTopicName(),
            operation,
            "https://host");
    assertThat(response)
        .isEqualTo(
            "https://host?resource_type="
                + resourceType
                + "&resource_name="
                + clusterAclRequest.getTopicName()
                + "&pattern_type=LITERAL&principal=User:*&host="
                + clusterAclRequest.getAclIp()
                + "&operation="
                + operation
                + "&permission=ALLOW");
  }

  @Test
  public void getQueryParamsPrincipal() {
    ClusterAclRequest clusterAclRequest = utilMethods.getConfluentCloudConsumerAclRequest();
    String resourceType = "CONSUMER";
    String operation = "READ";
    String response =
        confluentCloudApiService.updateQueryParams(
            clusterAclRequest,
            resourceType,
            clusterAclRequest.getConsumerGroup(),
            operation,
            "https://host");
    assertThat(response)
        .isEqualTo(
            "https://host?resource_type="
                + resourceType
                + "&resource_name="
                + clusterAclRequest.getConsumerGroup()
                + "&pattern_type=LITERAL&principal=User:"
                + clusterAclRequest.getAclSsl()
                + "&host=*&operation="
                + operation
                + "&permission=ALLOW");
  }

  private void stubTopics() {
    when(clusterApiUtils.getRequestDetails(anyString(), eq(KafkaSupportedProtocol.SSL)))
        .thenReturn(Pair.of("", restTemplate));
    when(clusterApiUtils.createHeaders(anyString(), any())).thenReturn(new HttpHeaders());
    when(env.getProperty(any())).thenReturn(KAFKA_V_3_CLUSTERS_CLUSTER_ID_TOPICS);
  }

  private void stubAcls() {
    when(clusterApiUtils.getRequestDetails(anyString(), eq(KafkaSupportedProtocol.SSL)))
        .thenReturn(Pair.of("", restTemplate));
    when(clusterApiUtils.createHeaders(anyString(), any())).thenReturn(new HttpHeaders());
    when(env.getProperty(any())).thenReturn(KAFKA_V_3_CLUSTERS_CLUSTER_ID_ACLS);
  }

  public ClusterAclRequest getClusterAclRequest(String aclType) {
    return ClusterAclRequest.builder()
        .env("localhost")
        .topicName("testtopic")
        .protocol(KafkaSupportedProtocol.PLAINTEXT)
        .consumerGroup("congroup1")
        .clusterName("clusterName")
        .aclType(aclType)
        .aclIp("11.12.33.122")
        .aclSsl(null)
        .requestOperationType(RequestOperationType.CREATE)
        .aclNativeType(AclsNativeType.CONFLUENT_CLOUD.name())
        .aclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS.name())
        .transactionalId("transactionalId")
        .build();
  }
}
