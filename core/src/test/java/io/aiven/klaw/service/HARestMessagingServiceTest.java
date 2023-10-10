package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.model.ApiResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HARestMessagingServiceTest {
  public static final String KLAW_PROJECT_IO_9097 = "https://klaw-project.io:9097";
  private UtilMethods utilMethods;
  @Mock private Environment environment;

  @Mock HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory;

  @Mock RestTemplate rest;

  private HARestMessagingService restMessagingService;

  @BeforeEach
  void setUp() {
    utilMethods = new UtilMethods();
    restMessagingService = new HARestMessagingService();
    ReflectionTestUtils.setField(restMessagingService, "environment", environment);
    ReflectionTestUtils.setField(restMessagingService, "apiUser", "MyApp2AppUser");
    ReflectionTestUtils.setField(
        restMessagingService,
        "clusterUrlsAsString",
        "https://localhost:0,https://klaw-project.io:9097");
    ReflectionTestUtils.setField(
        restMessagingService,
        "app2AppApiKey",
        "TXlCaWdnZXN0U2VjcmV0SXNUaGF0S2xhd0lzUmVhbGx5RXhjZWxsZW50=");
    // ensure that the base urls static map is reset after every test
    ReflectionTestUtils.setField(restMessagingService, "baseUrlsMap", null);
    ReflectionTestUtils.setField(restMessagingService, "rest", null);
    ClusterApiService.requestFactory = httpComponentsClientHttpRequestFactory;
  }

  @Test
  public void test_ClusterUrls_FilterLocalUrl() {
    when(environment.getProperty("server.port")).thenReturn("0");
    List<String> clusterUrls = restMessagingService.getHAClusterUrls();
    // Should only contain the serverUrls that arent the current server.
    assertThat(clusterUrls).containsOnly(KLAW_PROJECT_IO_9097);
  }

  @Test
  public void test_ClusterUrls_NoLocalUrlToFilter() {

    when(environment.getProperty("server.port")).thenReturn("9197");
    List<String> clusterUrls = restMessagingService.getHAClusterUrls();
    // Now with the environment returning port 9197 as the server.port we expect to see both
    assertThat(clusterUrls).containsOnly("https://localhost:0", KLAW_PROJECT_IO_9097);
  }

  @Test
  public void testWithHttps_ClusterUrlsAsStringRestTemplateIsReturned() {

    RestTemplate restTemplate = restMessagingService.getRestTemplate();
    assertThat(restTemplate).isNotNull();
  }

  @Test
  public void testWithHttp_ClusterUrlsAsStringRestTemplateIsReturned() {
    ReflectionTestUtils.setField(
        restMessagingService,
        "clusterUrlsAsString",
        "http://localhost:0,http://klaw-project.io:9097");
    RestTemplate restTemplate = restMessagingService.getRestTemplate();
    assertThat(restTemplate).isNotNull();
  }

  @Test
  public void testRemoveEntryFromTheCache() {
    ReflectionTestUtils.setField(restMessagingService, "rest", rest);
    ReflectionTestUtils.setField(
        restMessagingService, "clusterUrls", List.of(KLAW_PROJECT_IO_9097));

    restMessagingService.sendRemove("environment", 101, 99000);
    verify(rest, times(1))
        .exchange(
            eq(KLAW_PROJECT_IO_9097 + "/cache/tenant/101/entityType/environment/id/99000"),
            eq(HttpMethod.DELETE),
            any(),
            eq(Void.class));
  }

  @Test
  public void testAddEntryFromTheCache() {
    ReflectionTestUtils.setField(restMessagingService, "rest", rest);
    ReflectionTestUtils.setField(
        restMessagingService, "clusterUrls", List.of(KLAW_PROJECT_IO_9097));

    Env env = new Env();
    env.setName("Dev1");
    env.setId("123");
    restMessagingService.sendUpdate("environment", 101, env);
    verify(rest, times(1))
        .postForObject(
            eq(KLAW_PROJECT_IO_9097 + "/cache/tenant/101/entityType/environment"),
            any(),
            eq(ApiResponse.class));
  }
}
