package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import io.aiven.klaw.clusterapi.UtilMethods;
import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.ClusterSchemaRequest;
import io.aiven.klaw.clusterapi.models.RegisterSchemaCustomResponse;
import io.aiven.klaw.clusterapi.models.RegisterSchemaResponse;
import io.aiven.klaw.clusterapi.models.enums.AclType;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.enums.ClusterStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateAclsResult;
import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

// TODO From my humble point of view more work is require dto fix the test setup.
// Maybe it is also worth considering redesigning some classes involved to better adhere to shallow
// depth of test principle.
@ExtendWith(MockitoExtension.class)
public class UtilComponentsServiceTest {

  @Mock private ClusterApiUtils clusterApiUtils;

  @Mock private Environment env;

  @Mock private AdminClient adminClient;

  @Mock private KafkaFuture<Collection<AclBinding>> kafkaFutureCollection;

  @Mock private DescribeAclsResult describeAclsResult;

  @Mock private AccessControlEntry accessControlEntry;

  @Mock private CreateAclsResult createAclsResult;

  @Mock private KafkaFuture<Void> kFutureVoid;

  @Mock private RestTemplate restTemplate;

  private UtilMethods utilMethods;

  private UtilComponentsService utilComponentsService;

  private ApacheKafkaAclService apacheKafkaAclService;

  private SchemaService schemaService;

  @BeforeEach
  public void setUp() {
    utilComponentsService = new UtilComponentsService(env, clusterApiUtils);
    apacheKafkaAclService = new ApacheKafkaAclService(clusterApiUtils);
    schemaService = new SchemaService(clusterApiUtils);
    utilMethods = new UtilMethods();
  }

  // If no cluster type is defined, return
  @Test
  public void getStatusOnline() throws Exception {
    when(clusterApiUtils.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    ClusterStatus result =
        utilComponentsService.getStatus(
            "localhost", KafkaSupportedProtocol.PLAINTEXT, "", "kafka", "Apache Kafka");
    assertThat(result).isSameAs(ClusterStatus.ONLINE);
  }

  @Test
  public void getStatusOffline1() {

    ClusterStatus result =
        utilComponentsService.getStatus(
            "localhost", KafkaSupportedProtocol.PLAINTEXT, "", "", "Apache Kafka");
    assertThat(result).isSameAs(ClusterStatus.OFFLINE);
  }

  @Test
  public void getStatusOffline2() {

    ClusterStatus result =
        utilComponentsService.getStatus(
            "localhost", KafkaSupportedProtocol.PLAINTEXT, "", "", "Apache Kafka");
    assertThat(result).isSameAs(ClusterStatus.OFFLINE);
  }

  @Test
  public void loadAcls1() throws Exception {
    List<AclBinding> listAclBindings = utilMethods.getListAclBindings(accessControlEntry);

    when(clusterApiUtils.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    when(adminClient.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
    when(describeAclsResult.values()).thenReturn(kafkaFutureCollection);
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class))).thenReturn(listAclBindings);
    when(accessControlEntry.host()).thenReturn("11.12.33.456");
    when(accessControlEntry.operation()).thenReturn(AclOperation.READ);
    when(accessControlEntry.permissionType()).thenReturn(AclPermissionType.ALLOW);

    Set<Map<String, String>> result =
        apacheKafkaAclService.loadAcls("localhost", KafkaSupportedProtocol.PLAINTEXT, "");
    assertThat(result).hasSize(1);
  }

  @Test
  public void loadAcls2() throws Exception {
    List<AclBinding> listAclBindings = utilMethods.getListAclBindings(accessControlEntry);

    when(clusterApiUtils.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    when(adminClient.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
    when(describeAclsResult.values()).thenReturn(kafkaFutureCollection);
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class))).thenReturn(listAclBindings);
    when(accessControlEntry.host()).thenReturn("11.12.33.456");
    when(accessControlEntry.operation()).thenReturn(AclOperation.CREATE);
    when(accessControlEntry.permissionType()).thenReturn(AclPermissionType.ALLOW);

    Set<Map<String, String>> result =
        apacheKafkaAclService.loadAcls("localhost", KafkaSupportedProtocol.PLAINTEXT, "");
    assertThat(result).isEmpty();
  }

  @Test
  public void loadAcls3() throws Exception {
    when(clusterApiUtils.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    when(adminClient.describeAcls(any())).thenThrow(new RuntimeException("Describe Acls Error"));

    Set<Map<String, String>> result =
        apacheKafkaAclService.loadAcls("localhost", KafkaSupportedProtocol.PLAINTEXT, "");
    assertThat(result).isEmpty();
  }

  @Test
  public void createProducerAcl1() throws Exception {
    ClusterAclRequest clusterAclRequest = utilMethods.getAclRequest(AclType.CONSUMER.value);
    when(clusterApiUtils.getAdminClient(
            anyString(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    when(adminClient.createAcls(any())).thenReturn(createAclsResult);
    when(createAclsResult.all()).thenReturn(kFutureVoid);
    when(adminClient.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
    when(describeAclsResult.values()).thenReturn(kafkaFutureCollection);
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class)))
        .thenReturn(Collections.emptyList());
    String result = apacheKafkaAclService.updateProducerAcl(clusterAclRequest);

    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void createProducerAcl2() throws Exception {
    ClusterAclRequest clusterAclRequest = utilMethods.getAclRequest(AclType.CONSUMER.value);
    when(clusterApiUtils.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    when(adminClient.createAcls(any())).thenReturn(createAclsResult);
    when(createAclsResult.all()).thenReturn(kFutureVoid);
    when(adminClient.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
    when(describeAclsResult.values()).thenReturn(kafkaFutureCollection);
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class)))
        .thenReturn(Collections.emptyList());

    String result = apacheKafkaAclService.updateProducerAcl(clusterAclRequest);
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void createConsumerAcl1() throws Exception {
    ClusterAclRequest clusterAclRequest = utilMethods.getAclRequest(AclType.CONSUMER.value);
    when(clusterApiUtils.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    when(adminClient.createAcls(any())).thenReturn(createAclsResult);
    when(createAclsResult.all()).thenReturn(kFutureVoid);
    when(adminClient.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
    when(describeAclsResult.values()).thenReturn(kafkaFutureCollection);
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class)))
        .thenReturn(Collections.emptyList());
    String result = apacheKafkaAclService.updateConsumerAcl(clusterAclRequest);
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void createConsumerAcl2() throws Exception {
    ClusterAclRequest clusterAclRequest = utilMethods.getAclRequest(AclType.CONSUMER.value);
    when(clusterApiUtils.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    when(adminClient.createAcls(any())).thenReturn(createAclsResult);
    when(createAclsResult.all()).thenReturn(kFutureVoid);
    when(adminClient.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
    when(describeAclsResult.values()).thenReturn(kafkaFutureCollection);
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class)))
        .thenReturn(Collections.emptyList());

    String result = apacheKafkaAclService.updateConsumerAcl(clusterAclRequest);
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void registerNewSchema() {
    ClusterSchemaRequest clusterSchemaRequest = utilMethods.getSchema();
    RegisterSchemaResponse registerSchemaResponse = new RegisterSchemaResponse();
    registerSchemaResponse.setId(1);

    ResponseEntity<Set<Integer>> response2 = new ResponseEntity<>(Set.of(1), HttpStatus.OK);
    when(clusterApiUtils.getRequestDetails(any(), any())).thenReturn(Pair.of("", restTemplate));
    when(clusterApiUtils.createHeaders(anyString(), any())).thenReturn(new HttpHeaders());
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
    when(clusterApiUtils.getRequestDetails(any(), any())).thenReturn(Pair.of("", restTemplate));
    when(clusterApiUtils.createHeaders(anyString(), any())).thenReturn(new HttpHeaders());

    when(restTemplate.postForEntity(anyString(), any(), eq(RegisterSchemaResponse.class)))
        .thenThrow(new RuntimeException("Unable to connect"));
    ApiResponse resultResp = schemaService.registerSchema(clusterSchemaRequest);
    assertThat(resultResp.getMessage())
        .contains("Failure in registering schema.")
        .contains("Unable to connect");
  }

  private Map<String, TopicDescription> getTopicDescs() {
    Node node = new Node(1, "localhost", 1);

    TopicPartitionInfo topicPartitionInfo =
        new TopicPartitionInfo(2, node, List.of(node), List.of(node));
    TopicDescription tDesc =
        new TopicDescription(
            "testtopic", true, Arrays.asList(topicPartitionInfo, topicPartitionInfo));
    Map<String, TopicDescription> mapResults = new HashMap<>();
    mapResults.put("testtopic1", tDesc);

    tDesc =
        new TopicDescription(
            "testtopic2", true, Arrays.asList(topicPartitionInfo, topicPartitionInfo));
    mapResults.put("testtopic2", tDesc);

    return mapResults;
  }
}
