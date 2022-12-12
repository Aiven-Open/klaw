package io.aiven.klaw.clusterapi.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.common.KafkaFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
public class GetAdminClientTest {

  public static final String LOCALHOST_9092 = "localhost:9092";
  public static final String LOCALHOST_9093 = "localhost:9093";
  public static final String LOCALHOST = "localhost";
  @Mock Environment env;

  @Mock AdminClient adminClient;
  ClusterApiUtils getAdminClient;
  @Mock private ListTopicsResult listTopicsResult;
  @Mock private KafkaFuture<Set<String>> kafkaFuture;
  @Mock private HashMap<String, AdminClient> adminClientsMap;
  @Mock private AdminClientProperties adminClientProperties;

  @BeforeEach
  public void setUp() throws Exception {
    getAdminClient = new ClusterApiUtils(env, adminClientProperties, adminClientsMap);
    when(adminClientProperties.getRetriesConfig()).thenReturn("3");
    when(adminClientProperties.getRequestTimeOutMs()).thenReturn("15000");
    when(adminClientProperties.getRetryBackOffMsConfig()).thenReturn("15000");
  }

  @Test
  public void getAdminClient1() throws Exception {
    try (MockedStatic<AdminClient> mocked = mockStatic(AdminClient.class)) {
      mocked.when(() -> AdminClient.create(any(Properties.class))).thenReturn(adminClient);
      // Commented out to avoid UnnecessaryStubbingException
      // when(env.getProperty(any())).thenReturn("null");
      when(adminClient.listTopics()).thenReturn(listTopicsResult);
      when(listTopicsResult.names()).thenReturn(kafkaFuture);
      Set<String> setStr = new HashSet<>();
      when(kafkaFuture.get()).thenReturn(setStr);

      AdminClient result =
          getAdminClient.getAdminClient(LOCALHOST_9092, KafkaSupportedProtocol.PLAINTEXT, "");
      assertThat(result).isNotNull();
    }
  }

  @Test
  @Disabled
  public void getAdminClient2() throws Exception {
    try (MockedStatic<AdminClient> mocked = mockStatic(AdminClient.class)) {
      mocked.when(() -> AdminClient.create(any(Properties.class))).thenReturn(adminClient);
      // Commented out to avoid UnnecessaryStubbingException
      // when(env.getProperty(any())).thenReturn("true");
      when(adminClient.listTopics()).thenReturn(listTopicsResult);
      when(listTopicsResult.names()).thenReturn(kafkaFuture);
      Set<String> setStr = new HashSet<>();
      when(kafkaFuture.get()).thenReturn(setStr);

      AdminClient result =
          getAdminClient.getAdminClient(LOCALHOST_9092, KafkaSupportedProtocol.PLAINTEXT, "");
      assertThat(result).isNotNull();
    }
  }

  @Test
  @Disabled
  public void getAdminClient3() throws Exception {
    try (MockedStatic<AdminClient> mocked = mockStatic(AdminClient.class)) {
      mocked.when(() -> AdminClient.create(any(Properties.class))).thenReturn(adminClient);
      // Commented out to avoid UnnecessaryStubbingException
      when(env.getProperty(any())).thenReturn("false");
      when(adminClient.listTopics()).thenReturn(listTopicsResult);
      when(listTopicsResult.names()).thenReturn(kafkaFuture);
      Set<String> setStr = new HashSet<>();
      when(kafkaFuture.get()).thenReturn(setStr);

      AdminClient result =
          getAdminClient.getAdminClient(LOCALHOST_9092, KafkaSupportedProtocol.PLAINTEXT, "");
      assertThat(result).isNotNull();
    }
  }

  @Test
  public void getPlainProperties() {
    Properties props = getAdminClient.getPlainProperties(LOCALHOST);
    assertThat(props.getProperty("bootstrap.servers")).isEqualTo(LOCALHOST);
  }

  @Test
  public void getSslProperties() {
    Properties props = getAdminClient.getSslProperties(LOCALHOST_9093, "");
    assertThat(props.getProperty("bootstrap.servers")).isEqualTo(LOCALHOST_9093);
  }
}
