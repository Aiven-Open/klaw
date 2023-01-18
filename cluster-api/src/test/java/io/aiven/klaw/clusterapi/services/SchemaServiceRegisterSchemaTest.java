package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.aiven.klaw.clusterapi.models.ClusterSchemaRequest;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
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
  SchemaService schemaService;

  @Mock private RestTemplate restTemplate;
  @Mock private ClusterApiUtils clusterApiUtil;
  @Captor ArgumentCaptor<HttpEntity<Map<String, String>>> schemaCompatibility;

  @BeforeEach
  public void setUp() {

    schemaService = new SchemaService(clusterApiUtil);
  }

  @Test
  @Order(1)
  public void givenSchemaWithForceRegisterEnabled_setAndResetSchemaCompatability() {

    ClusterSchemaRequest schemaReq =
        ClusterSchemaRequest.builder()
            .forceRegister(true)
            .fullSchema("Nonsense Schema")
            .clusterIdentification("1")
            .protocol(KafkaSupportedProtocol.SSL)
            .env("unittest")
            .topicName("topic-1")
            .build();
    mockGetSchemaCompatibility();

    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of("https:registryEtc", restTemplate));

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(createSchemaRegisterResponseEntity(HttpStatus.OK));
    schemaService.registerSchema(schemaReq);
    // change schema compatibility called twice once to change to null second to change it back.
    verify(restTemplate, times(2)).put(any(), schemaCompatibility.capture(), eq(String.class));
    // 1 call to get the current SchemaCompatibility
    verify(restTemplate, times(1))
        .exchange(
            eq("https:registryEtc"),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class));
    // Sets it to None.
    assertThat(schemaCompatibility.getAllValues().get(0).getBody().get("compatibility"))
        .isEqualTo("NONE");
    // resets it to previous compatability
    assertThat(schemaCompatibility.getAllValues().get(1).getBody().get("compatibility"))
        .isEqualTo(CUSTOM_COMP);
  }

  @Test
  @Order(2)
  public void givenSchemaWithForceRegisterEnabled_404ReturnedAndResetSchemaCompatability() {

    ClusterSchemaRequest schemaReq =
        ClusterSchemaRequest.builder()
            .forceRegister(true)
            .fullSchema("Nonsense Schema")
            .clusterIdentification("1")
            .protocol(KafkaSupportedProtocol.SSL)
            .env("unittest")
            .topicName("topic-1")
            .build();
    mockGetSchemaCompatibility();

    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of("https:registryEtc", restTemplate));

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(createSchemaRegisterResponseEntity(HttpStatus.NOT_FOUND));
    schemaService.registerSchema(schemaReq);
    // change schema compatibility called twice once to change to null second to change it back.
    verify(restTemplate, times(2)).put(any(), schemaCompatibility.capture(), eq(String.class));
    // 1 call to get the current SchemaCompatibility
    verify(restTemplate, times(1))
        .exchange(
            eq("https:registryEtc"),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class));
    // Sets it to None.
    assertThat(schemaCompatibility.getAllValues().get(0).getBody().get("compatibility"))
        .isEqualTo("NONE");
    // resets it to previous compatability
    assertThat(schemaCompatibility.getAllValues().get(1).getBody().get("compatibility"))
        .isEqualTo(CUSTOM_COMP);
  }

  @Test
  @Order(3)
  public void givenSchemaWithForceRegisterEnabled_ConflictReturnedAndResetSchemaCompatability() {

    ClusterSchemaRequest schemaReq =
        ClusterSchemaRequest.builder()
            .forceRegister(true)
            .fullSchema("Nonsense Schema")
            .clusterIdentification("1")
            .protocol(KafkaSupportedProtocol.SSL)
            .env("unittest")
            .topicName("topic-1")
            .build();
    mockGetSchemaCompatibility();

    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of("https:registryEtc", restTemplate));

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(createSchemaRegisterResponseEntity(HttpStatus.CONFLICT));
    schemaService.registerSchema(schemaReq);
    // change schema compatibility called twice once to change to null second to change it back.
    verify(restTemplate, times(2)).put(any(), schemaCompatibility.capture(), eq(String.class));
    // 1 call to get the current SchemaCompatibility
    verify(restTemplate, times(1))
        .exchange(
            eq("https:registryEtc"),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class));
    // Sets it to None.
    assertThat(schemaCompatibility.getAllValues().get(0).getBody().get("compatibility"))
        .isEqualTo("NONE");
    // resets it to previous compatability
    assertThat(schemaCompatibility.getAllValues().get(1).getBody().get("compatibility"))
        .isEqualTo(CUSTOM_COMP);
  }

  @Test
  @Order(4)
  public void givenSchemaWithoutForceRegisterEnabled_setAndResetSchemaCompatability() {

    ClusterSchemaRequest schemaReq =
        ClusterSchemaRequest.builder()
            .forceRegister(false)
            .fullSchema("Nonsense Schema")
            .clusterIdentification("1")
            .protocol(KafkaSupportedProtocol.SSL)
            .env("unittest")
            .topicName("topic-1")
            .build();
    mockGetSchemaCompatibility();

    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of("https:registryEtc", restTemplate));

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(createSchemaRegisterResponseEntity(HttpStatus.OK));
    schemaService.registerSchema(schemaReq);
    // change schema compatibility called twice once to change to null second to change it back.
    verify(restTemplate, times(0)).put(any(), any(), eq(String.class));
    // 0 calls to get the current SchemaCompatibility as isForce==false
    verify(restTemplate, times(0))
        .exchange(
            eq("https:registryEtc"),
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
            .fullSchema("Nonsense Schema")
            .clusterIdentification("1")
            .protocol(KafkaSupportedProtocol.SSL)
            .env("unittest")
            .topicName("topic-1")
            .build();
    mockGetSchemaCompatibility();

    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of("https:registryEtc", restTemplate));

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(createSchemaRegisterResponseEntity(HttpStatus.NOT_FOUND));
    schemaService.registerSchema(schemaReq);
    // change schema compatibility called twice once to change to null second to change it back.
    verify(restTemplate, times(0)).put(any(), any(), eq(String.class));
    // 0 calls to get the current SchemaCompatibility as isForce==false
    verify(restTemplate, times(0))
        .exchange(
            eq("https:registryEtc"),
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
            .fullSchema("Nonsense Schema")
            .clusterIdentification("1")
            .protocol(KafkaSupportedProtocol.SSL)
            .env("unittest")
            .topicName("topic-1")
            .build();
    mockGetSchemaCompatibility();

    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of("https:registryEtc", restTemplate));

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(createSchemaRegisterResponseEntity(HttpStatus.CONFLICT));
    schemaService.registerSchema(schemaReq);
    // change schema compatibility called twice once to change to null second to change it back.
    verify(restTemplate, times(0)).put(any(), any(), eq(String.class));
    // 0 calls to get the current SchemaCompatibility as isForce==false
    verify(restTemplate, times(0))
        .exchange(
            eq("https:registryEtc"),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class));
  }

  @Test
  @Order(7)
  public void givenSchemaWithForceRegisterEnabled_GetSchemaCompatibilityException_doNotRevert() {

    ClusterSchemaRequest schemaReq =
        ClusterSchemaRequest.builder()
            .forceRegister(true)
            .fullSchema("Nonsense Schema")
            .clusterIdentification("1")
            .protocol(KafkaSupportedProtocol.SSL)
            .env("unittest")
            .topicName("topic-1")
            .build();
    mockGetSchemaCompatibility();

    when(clusterApiUtil.getRequestDetails(any(), any()))
        .thenReturn(Pair.of("https:registryEtc", restTemplate));

    when(restTemplate.exchange(
            eq("https:registryEtc"),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class)))
        .thenReturn(createErrorCompatibilityResponseEntity(HttpStatus.UNAUTHORIZED));
    schemaService.registerSchema(schemaReq);
    // change schema compatibility never called as exception occurs.
    verify(restTemplate, times(0)).put(any(), schemaCompatibility.capture(), eq(String.class));
    // 1 call to get the current SchemaCompatibility
    verify(restTemplate, times(1))
        .exchange(
            eq("https:registryEtc"),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<Map<String, String>>() {}),
            any(HashMap.class));
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
        .thenReturn(Pair.of("https:registryEtc", restTemplate));
    when(clusterApiUtil.createHeaders(any(), any())).thenReturn(new HttpHeaders());
    Map<String, String> params = new HashMap<>();

    when(restTemplate.exchange(
            eq("https:registryEtc"),
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
