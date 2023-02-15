package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.aiven.klaw.clusterapi.UtilMethods;
import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.confluentcloud.ListAclsResponse;
import io.aiven.klaw.clusterapi.models.confluentcloud.ListTopicsResponse;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
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
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
public class ConfluentCloudApiServiceTest {
  public static final String KAFKA_V_3_CLUSTERS_CLUSTER_ID_TOPICS =
      "/kafka/v3/clusters/cluster-id/topics";
  public static final String KAFKA_V_3_CLUSTERS_CLUSTER_ID_ACLS =
      "/kafka/v3/clusters/cluster-id/acls";
  public static final String CLUSTER_ID = "CC_DEV1";
  ConfluentCloudApiService confluentCloudApiService;
  @Mock private Environment env;

  @Mock RestTemplate restTemplate;
  @Mock ClusterApiUtils clusterApiUtils;
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
    Set<Map<String, String>> listTopicsSet =
        confluentCloudApiService.listTopics(
            "localhost:443", KafkaSupportedProtocol.SSL, CLUSTER_ID);

    assertThat(listTopicsSet).hasSize(2); // two topics
    assertThat(listTopicsSet.stream().toList().get(0))
        .hasSize(3); // topicName, partitions, replication factor
    assertThat(listTopicsSet.stream().toList().get(0))
        .containsKeys("topicName", "partitions", "replicationFactor");
  }

  @Test
  public void listTopicsConnectivityFailure() {
    stubTopics();
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            (ParameterizedTypeReference<ListTopicsResponse>) any()))
        .thenThrow(new RuntimeException("Cannot connect to confluent cluster"));
    assertThatThrownBy(
            () -> {
              confluentCloudApiService.listTopics(
                  "localhost:443", KafkaSupportedProtocol.SSL, CLUSTER_ID);
            })
        .isInstanceOf(Exception.class);
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
  public void createAclsProducer() throws Exception {
    stubAcls();
    ClusterAclRequest clusterAclRequest = utilMethods.getConfluentCloudProducerAclRequest();
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
    ClusterAclRequest clusterAclRequest = utilMethods.getConfluentCloudProducerAclRequest();
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
  public void deleteAcls() throws Exception {
    ClusterAclRequest clusterAclRequest = utilMethods.getConfluentCloudProducerAclRequest();
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
  public void getQueryParamsHost() {
    ClusterAclRequest clusterAclRequest = utilMethods.getConfluentCloudProducerAclRequest();
    String resourceType = "TOPIC";
    String operation = "WRITE";
    String response =
        confluentCloudApiService.getQueryParams(
            clusterAclRequest, resourceType, clusterAclRequest.getTopicName(), operation);
    assertThat(response)
        .isEqualTo(
            "resource_type="
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
        confluentCloudApiService.getQueryParams(
            clusterAclRequest, resourceType, clusterAclRequest.getConsumerGroup(), operation);
    assertThat(response)
        .isEqualTo(
            "resource_type="
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
}
