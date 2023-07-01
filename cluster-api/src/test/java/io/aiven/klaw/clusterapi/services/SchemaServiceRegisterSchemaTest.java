package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.clusterapi.UtilMethods;
import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterSchemaRequest;
import io.aiven.klaw.clusterapi.models.RegisterSchemaCustomResponse;
import io.aiven.klaw.clusterapi.models.RegisterSchemaResponse;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SchemaServiceRegisterSchemaTest {

  public static final String CUSTOM_COMP = "CustomComp";
  public static final String FULL_SCHEMA = "Nonsense Schema";
  public static final String ENV = "unittest";
  public static final String TOPIC_NAME = "topic-1";
  public static final String CLUSTER_IDENTIFICATION = "1";
  public static final String REGISTRY_URL = "https:registryEtc";
  public static final String COMPATIBILITY_NODE_KEY = "compatibility";
  SchemaService schemaService;

  @Mock private RestTemplate restTemplate;
  @Mock private ClusterApiUtils clusterApiUtil;
  @Captor ArgumentCaptor<HttpEntity<Map<String, String>>> schemaCompatibility;
  private UtilMethods utilMethods;

  @BeforeEach
  public void setUp() {
    schemaService = new SchemaService(clusterApiUtil);
    utilMethods = new UtilMethods();
  }

  @Test
  @Order(1)
  public void givenSchemaWithForceRegisterEnabled_setAndResetSchemaCompatability() {

    ClusterSchemaRequest schemaReq =
        ClusterSchemaRequest.builder()
            .forceRegister(true)
            .fullSchema(FULL_SCHEMA)
            .clusterIdentification(CLUSTER_IDENTIFICATION)
            .protocol(KafkaSupportedProtocol.SSL)
            .env(ENV)
            .topicName(TOPIC_NAME)
            .build();
    mockGetSchemaCompatibility();

    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of(REGISTRY_URL, restTemplate));

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(createSchemaRegisterResponseEntity(HttpStatus.OK));
    schemaService.registerSchema(schemaReq);
    // change schema compatibility called twice once to change to null second to change it back.
    verify(restTemplate, times(2)).put(any(), schemaCompatibility.capture(), eq(String.class));
    // 1 call to get the current SchemaCompatibility
    verify(restTemplate, times(1))
        .exchange(
            eq(REGISTRY_URL),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class));
    // Sets it to None.
    assertThat(schemaCompatibility.getAllValues().get(0).getBody().get(COMPATIBILITY_NODE_KEY))
        .isEqualTo("NONE");
    // resets it to previous compatability
    assertThat(schemaCompatibility.getAllValues().get(1).getBody().get(COMPATIBILITY_NODE_KEY))
        .isEqualTo(CUSTOM_COMP);
  }

  @Test
  @Order(2)
  public void givenSchemaWithForceRegisterEnabled_404ReturnedAndResetSchemaCompatability() {

    ClusterSchemaRequest schemaReq =
        ClusterSchemaRequest.builder()
            .forceRegister(true)
            .fullSchema(FULL_SCHEMA)
            .clusterIdentification(CLUSTER_IDENTIFICATION)
            .protocol(KafkaSupportedProtocol.SSL)
            .env(ENV)
            .topicName(TOPIC_NAME)
            .build();
    mockGetSchemaCompatibility();

    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of(REGISTRY_URL, restTemplate));

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(createSchemaRegisterResponseEntity(HttpStatus.NOT_FOUND));
    schemaService.registerSchema(schemaReq);
    // change schema compatibility called twice once to change to null second to change it back.
    verify(restTemplate, times(2)).put(any(), schemaCompatibility.capture(), eq(String.class));
    // 1 call to get the current SchemaCompatibility
    verify(restTemplate, times(1))
        .exchange(
            eq(REGISTRY_URL),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class));
    // Sets it to None.
    assertThat(schemaCompatibility.getAllValues().get(0).getBody().get(COMPATIBILITY_NODE_KEY))
        .isEqualTo("NONE");
    // resets it to previous compatability
    assertThat(schemaCompatibility.getAllValues().get(1).getBody().get(COMPATIBILITY_NODE_KEY))
        .isEqualTo(CUSTOM_COMP);
  }

  @Test
  @Order(3)
  public void givenSchemaWithForceRegisterEnabled_ConflictReturnedAndResetSchemaCompatability() {

    ClusterSchemaRequest schemaReq =
        ClusterSchemaRequest.builder()
            .forceRegister(true)
            .fullSchema(FULL_SCHEMA)
            .clusterIdentification(CLUSTER_IDENTIFICATION)
            .protocol(KafkaSupportedProtocol.SSL)
            .env(ENV)
            .topicName(TOPIC_NAME)
            .build();
    mockGetSchemaCompatibility();

    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of(REGISTRY_URL, restTemplate));

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(createSchemaRegisterResponseEntity(HttpStatus.CONFLICT));
    schemaService.registerSchema(schemaReq);
    // change schema compatibility called twice once to change to null second to change it back.
    verify(restTemplate, times(2)).put(any(), schemaCompatibility.capture(), eq(String.class));
    // 1 call to get the current SchemaCompatibility
    verify(restTemplate, times(1))
        .exchange(
            eq(REGISTRY_URL),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class));
    // Sets it to None.
    assertThat(schemaCompatibility.getAllValues().get(0).getBody().get(COMPATIBILITY_NODE_KEY))
        .isEqualTo("NONE");
    // resets it to previous compatability
    assertThat(schemaCompatibility.getAllValues().get(1).getBody().get(COMPATIBILITY_NODE_KEY))
        .isEqualTo(CUSTOM_COMP);
  }

  @Test
  @Order(4)
  public void givenSchemaWithoutForceRegisterEnabled_setAndResetSchemaCompatability() {

    ClusterSchemaRequest schemaReq =
        ClusterSchemaRequest.builder()
            .forceRegister(false)
            .fullSchema(FULL_SCHEMA)
            .clusterIdentification(CLUSTER_IDENTIFICATION)
            .protocol(KafkaSupportedProtocol.SSL)
            .env(ENV)
            .topicName(TOPIC_NAME)
            .build();
    mockGetSchemaCompatibility();

    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of(REGISTRY_URL, restTemplate));

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(createSchemaRegisterResponseEntity(HttpStatus.OK));
    schemaService.registerSchema(schemaReq);
    // change schema compatibility called twice once to change to null second to change it back.
    verify(restTemplate, times(0)).put(any(), any(), eq(String.class));
    // 1 call to get the current SchemaCompatibility as isForce==false
    verify(restTemplate, times(1))
        .exchange(
            eq(REGISTRY_URL),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class));
  }

  @Test
  @Order(5)
  public void givenSchemaWithoutForceRegisterEnabled_404ReturnedAndResetSchemaCompatability() {

    ClusterSchemaRequest schemaReq =
        ClusterSchemaRequest.builder()
            .forceRegister(false)
            .fullSchema(FULL_SCHEMA)
            .clusterIdentification(CLUSTER_IDENTIFICATION)
            .protocol(KafkaSupportedProtocol.SSL)
            .env(ENV)
            .topicName(TOPIC_NAME)
            .build();
    mockGetSchemaCompatibility();

    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of(REGISTRY_URL, restTemplate));

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(createSchemaRegisterResponseEntity(HttpStatus.NOT_FOUND));
    schemaService.registerSchema(schemaReq);
    // change schema compatibility called twice once to change to null second to change it back.
    verify(restTemplate, times(0)).put(any(), any(), eq(String.class));
    // 1 call to get the current SchemaCompatibility as isForce==false
    verify(restTemplate, times(1))
        .exchange(
            eq(REGISTRY_URL),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class));
  }

  @Test
  @Order(6)
  public void givenSchemaWithoutForceRegisterEnabled_ConflictReturnedAndResetSchemaCompatability() {

    ClusterSchemaRequest schemaReq =
        ClusterSchemaRequest.builder()
            .forceRegister(false)
            .fullSchema(FULL_SCHEMA)
            .clusterIdentification(CLUSTER_IDENTIFICATION)
            .protocol(KafkaSupportedProtocol.SSL)
            .env(ENV)
            .topicName(TOPIC_NAME)
            .build();
    mockGetSchemaCompatibility();

    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of(REGISTRY_URL, restTemplate));

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(createSchemaRegisterResponseEntity(HttpStatus.CONFLICT));
    schemaService.registerSchema(schemaReq);
    // change schema compatibility called twice once to change to null second to change it back.
    verify(restTemplate, times(0)).put(any(), any(), eq(String.class));
    // 1 call to get the current SchemaCompatibility as isForce==false
    verify(restTemplate, times(1))
        .exchange(
            eq(REGISTRY_URL),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class));
  }

  @Test
  @Order(7)
  public void
      givenSchemaWithForceRegisterEnabled_GetSchemaCompatibilityException_applyGlobalCompatibility() {

    ClusterSchemaRequest schemaReq =
        ClusterSchemaRequest.builder()
            .forceRegister(true)
            .fullSchema(FULL_SCHEMA)
            .clusterIdentification(CLUSTER_IDENTIFICATION)
            .protocol(KafkaSupportedProtocol.SSL)
            .env(ENV)
            .topicName(TOPIC_NAME)
            .build();
    mockGetSchemaCompatibility();

    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of(REGISTRY_URL, restTemplate));

    when(restTemplate.exchange(
            eq(REGISTRY_URL),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class)))
        .thenReturn(createErrorCompatibilityResponseEntity(HttpStatus.UNAUTHORIZED));
    schemaService.registerSchema(schemaReq);
    // change schema compatibility called by setting global compatibility.
    verify(restTemplate, times(1)).put(any(), schemaCompatibility.capture(), eq(String.class));
    // 1 call to get the current SchemaCompatibility
    verify(restTemplate, times(2))
        .exchange(
            eq(REGISTRY_URL),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class));
  }

  @Test
  @Order(8)
  public void
      givenSchemaWithForceRegisterEnabled_ExistingCompatibilityIsFailValidationAndGetGlobalCompatibility() {

    ClusterSchemaRequest schemaReq =
        ClusterSchemaRequest.builder()
            .forceRegister(true)
            .fullSchema(FULL_SCHEMA)
            .clusterIdentification(CLUSTER_IDENTIFICATION)
            .protocol(KafkaSupportedProtocol.SSL)
            .env(ENV)
            .topicName(TOPIC_NAME)
            .build();
    mockGetSchemaCompatibility();
    // If null returns default schema should be reverted to.
    when(restTemplate.exchange(
            eq(REGISTRY_URL),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class)))
        .thenReturn(createCompatibilityResponseEntity(null));
    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of(REGISTRY_URL, restTemplate));

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(createSchemaRegisterResponseEntity(HttpStatus.OK));
    schemaService.registerSchema(schemaReq);
    // change schema compatibility called once to change to none.
    verify(restTemplate, times(1)).put(any(), schemaCompatibility.capture(), eq(String.class));
    // 2 calls to get the SchemaCompatibility subject, global
    verify(restTemplate, times(2))
        .exchange(
            eq(REGISTRY_URL),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class));
  }

  @Test
  @Order(9)
  public void givenSchemaWithForceRegisterEnabled_NotFoundFallBackToGlobalSchemaSettingReturned() {

    ClusterSchemaRequest schemaReq =
        ClusterSchemaRequest.builder()
            .forceRegister(true)
            .fullSchema(FULL_SCHEMA)
            .clusterIdentification(CLUSTER_IDENTIFICATION)
            .protocol(KafkaSupportedProtocol.SSL)
            .env(ENV)
            .topicName(TOPIC_NAME)
            .build();
    mockGetSchemaCompatibility();

    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of(REGISTRY_URL, restTemplate));

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(createSchemaRegisterResponseEntity(HttpStatus.OK));

    when(restTemplate.exchange(
            eq(REGISTRY_URL),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class)))
        .thenReturn(createErrorCompatibilityResponseEntity(HttpStatus.NOT_FOUND));

    schemaService.registerSchema(schemaReq);
    verify(restTemplate, times(1)).put(any(), schemaCompatibility.capture(), eq(String.class));
  }

  @Test
  @Order(10)
  public void givenSchemaWithForceRegisterEnabled_NotFoundGetGlobalCompatibility() {

    ClusterSchemaRequest schemaReq =
        ClusterSchemaRequest.builder()
            .forceRegister(true)
            .fullSchema(FULL_SCHEMA)
            .clusterIdentification(CLUSTER_IDENTIFICATION)
            .protocol(KafkaSupportedProtocol.SSL)
            .env(ENV)
            .topicName(TOPIC_NAME)
            .build();
    mockGetSchemaCompatibility();

    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of(REGISTRY_URL, restTemplate));

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(createSchemaRegisterResponseEntity(HttpStatus.OK));
    when(restTemplate.exchange(
            eq(REGISTRY_URL),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class)))
        .thenReturn(createErrorCompatibilityResponseEntity(HttpStatus.NOT_FOUND))
        .thenReturn(createErrorCompatibilityResponseEntity(HttpStatus.NOT_FOUND));
    when(restTemplate.exchange(
            eq(REGISTRY_URL),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class)))
        .thenReturn(createErrorCompatibilityResponseEntity(HttpStatus.NOT_FOUND))
        .thenReturn(createErrorCompatibilityResponseEntity(HttpStatus.NOT_FOUND));
    schemaService.registerSchema(schemaReq);
    // change schema compatibility to NONE, once
    verify(restTemplate, times(1)).put(any(), any(), eq(String.class));

    // 2 calls to get the current SchemaCompatibility (subject, global) as isForce==false
    verify(restTemplate, times(2))
        .exchange(
            eq(REGISTRY_URL),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class));
  }

  @Test
  public void registerNewSchema() {
    ClusterSchemaRequest clusterSchemaRequest = utilMethods.getSchema();
    RegisterSchemaResponse registerSchemaResponse = new RegisterSchemaResponse();
    registerSchemaResponse.setId(1);

    ResponseEntity<Set<Integer>> response2 = new ResponseEntity<>(Set.of(1), HttpStatus.OK);
    when(clusterApiUtil.getRequestDetails(any(), any())).thenReturn(Pair.of("", restTemplate));
    when(clusterApiUtil.createHeaders(anyString(), any())).thenReturn(new HttpHeaders());
    when(restTemplate.postForEntity(anyString(), any(), eq(RegisterSchemaResponse.class)))
            .thenReturn(new ResponseEntity<>(registerSchemaResponse, HttpStatus.OK));
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(new ParameterizedTypeReference<Set<Integer>>() {}),
            anyMap()))
            .thenReturn(response2);

    ApiResponse resultResp = schemaService.registerSchema(clusterSchemaRequest);
    assertThat(resultResp.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
    RegisterSchemaCustomResponse registerSchemaCustomResponse =
            (RegisterSchemaCustomResponse) resultResp.getData();
    assertThat(registerSchemaCustomResponse.isSchemaRegistered()).isTrue();
    assertThat(registerSchemaCustomResponse.getVersion()).isEqualTo(1);
    assertThat(registerSchemaCustomResponse.getId()).isEqualTo(1);
  }

  @Test
  public void registerSchemaFailure() {
    ClusterSchemaRequest clusterSchemaRequest = utilMethods.getSchema();
    ResponseEntity<List<Integer>> response = new ResponseEntity<>(List.of(1, 2), HttpStatus.OK);
    when(clusterApiUtil.getRequestDetails(any(), any())).thenReturn(Pair.of("", restTemplate));
    when(clusterApiUtil.createHeaders(anyString(), any())).thenReturn(new HttpHeaders());

    when(restTemplate.postForEntity(anyString(), any(), eq(RegisterSchemaResponse.class)))
            .thenThrow(new RuntimeException("Unable to connect"));
    ApiResponse resultResp = schemaService.registerSchema(clusterSchemaRequest);
    assertThat(resultResp.getMessage())
            .contains("Failure in registering schema.")
            .contains("Unable to connect");
  }

  private ResponseEntity<Map<String, String>> createErrorCompatibilityResponseEntity(
      HttpStatus status) {
    return ResponseEntity.status(status).build();
  }

  private ResponseEntity<String> createSchemaRegisterResponseEntity(HttpStatus status) {

    return ResponseEntity.status(status).body("Completed API Call.");
  }

  private void mockGetSchemaCompatibility() {
    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of(REGISTRY_URL, restTemplate));
    when(clusterApiUtil.createHeaders(any(), any())).thenReturn(new HttpHeaders());
    Map<String, String> params = new HashMap<>();

    when(restTemplate.exchange(
            eq(REGISTRY_URL),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class)))
        .thenReturn(createCompatibilityResponseEntity(CUSTOM_COMP));
  }

  private ResponseEntity<Map<String, String>> createCompatibilityResponseEntity(
      String compatiblityType) {
    Map<String, String> map = new HashMap<>();
    map.put("compatibilityLevel", compatiblityType);
    return ResponseEntity.ok(map);
  }
}
